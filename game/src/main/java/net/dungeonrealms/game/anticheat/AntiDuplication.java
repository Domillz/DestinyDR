package net.dungeonrealms.game.anticheat;

import com.google.common.collect.HashMultimap;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.GameAPI;
import net.dungeonrealms.common.Tuple;
import net.dungeonrealms.common.game.database.DatabaseAPI;
import net.dungeonrealms.common.game.database.data.EnumData;
import net.dungeonrealms.common.game.database.player.rank.Rank;
import net.dungeonrealms.common.game.punishment.PunishAPI;
import net.dungeonrealms.common.game.util.AsyncUtils;
import net.dungeonrealms.common.game.util.CooldownProvider;
import net.dungeonrealms.game.mastery.NBTItem;
import net.dungeonrealms.game.mastery.Utils;
import net.dungeonrealms.game.mechanic.ItemManager;
import net.dungeonrealms.game.mechanic.generic.EnumPriority;
import net.dungeonrealms.game.mechanic.generic.GenericMechanic;
import net.dungeonrealms.game.player.banks.BankMechanics;
import net.dungeonrealms.game.player.banks.Storage;
import net.dungeonrealms.game.world.entity.util.MountUtils;
import net.dungeonrealms.game.world.item.repairing.RepairAPI;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by Nick on 10/1/2015.
 */

public class AntiDuplication implements GenericMechanic {

    static AntiDuplication instance = null;

    public static Set<UUID> EXCLUSIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static CooldownProvider WARNING_SUPPRESSOR = new CooldownProvider();

    private final static long CHECK_TICK_FREQUENCY = 10L;

    public static AntiDuplication getInstance() {
        if (instance == null) {
            instance = new AntiDuplication();
        }
        return instance;
    }

    @Override
    public EnumPriority startPriority() {
        return EnumPriority.CATHOLICS;
    }


    @Override
    public void startInitialization() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(DungeonRealms.getInstance(),
                () -> Bukkit.getOnlinePlayers().stream().forEach(p -> checkForSuspiciousDupedItems(p, new HashSet<>(Collections.singletonList(p.getInventory())))), 0, CHECK_TICK_FREQUENCY);
    }

    @Override
    public void stopInvocation() {

    }

    public void handleLogin(Player p) {
        Inventory muleInv = MountUtils.inventories.get(p.getUniqueId());
        Storage storage = BankMechanics.getInstance().getStorage(p.getUniqueId());
        AsyncUtils.pool.submit(() -> checkForSuspiciousDupedItems(p, new HashSet<>(Arrays.asList(p.getInventory(), storage.inv, storage.collection_bin, muleInv))));
    }


    /**
     * Checks and removes duplicated items
     * when detected.
     *
     * @author APOLLOSOFTWARE
     */
    private static void checkForDuplications(Player p, HashMultimap<Inventory, Tuple<ItemStack, String>> map) {
        Set<String> duplicates = Utils.findDuplicates(map.values().stream().map(Tuple::b).collect(Collectors.toList()));
        Map<String, Integer> itemDesc = new HashMap<>();
        if (!duplicates.isEmpty()) { // caught red handed
            for (Map.Entry<Inventory, Tuple<ItemStack, String>> e : map.entries()) {
                String uniqueEpochIdentifier = e.getValue().b();
                if (duplicates.contains(uniqueEpochIdentifier)) {
                    String name = "";
                    ItemStack item = e.getValue().a();
                    ItemMeta meta = item.getItemMeta();
                    if (meta.hasDisplayName()) name += meta.getDisplayName();
                    else {
                        Material material = e.getValue().a().getType();
                        name += material.toString().replace("_", " ");
                    }
                    if (itemDesc.containsKey(name)) itemDesc.put(name, itemDesc.get(name) + 1);
                    else itemDesc.put(name, 1);
                    // GIVE THEM AN ORIGINAL //
                    if (RepairAPI.isItemArmorOrWeapon(e.getValue().a())) {
                        remove(e.getKey(), e.getValue().b());
                        // THIS WILL REMOVED THE DUPLICATE ITEMS //
                        if (traceCount(e.getKey(), e.getValue().b()) == 0)
                            e.getKey().addItem(e.getValue().a());
                    } else if (traceCount(e.getKey(), e.getValue().b()) == 0) {
                        e.getValue().a().setAmount(1);
                        e.getKey().addItem(e.getValue().a());
                    } else {
                        itemDesc.put(name, itemDesc.get(name) + (e.getValue().a().getAmount() - 2));
                        remove(e.getKey(), e.getValue().b());
                    }
                }
            }
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for (Map.Entry<String, Integer> e : itemDesc.entrySet()) {
                int amount = e.getValue() - 1;
                String name = e.getKey();

                if (i == 0)
                    builder.append(amount).append(" count(s) of ").append(ChatColor.AQUA).append(name).append(ChatColor.WHITE);
                else
                    builder.append(", ").append(amount).append(" count(s) of ").append(ChatColor.AQUA).append(name).append(ChatColor.WHITE);
                i++;
            }
            p.sendMessage(ChatColor.GOLD + "Found a dupe? Don't " + ChatColor.RED + "abuse" + ChatColor.GOLD + " it! Report it and you're eligible for " + ChatColor.YELLOW + ChatColor.BOLD + "SUB++" + ChatColor.GOLD + "!");
            GameAPI.sendNetworkMessage("GMMessage", ChatColor.RED.toString() + "[ANTI CHEAT] " +
                    ChatColor.WHITE + "Player " + p.getName() + " has attempted to duplicate items. Removed: " + builder.toString() + " on shard " + ChatColor.GOLD + ChatColor.UNDERLINE + DungeonRealms.getInstance().shardid);
        }
    }

    /**
     * Checks for suspiciously duped items
     *
     * @param player      Player target
     * @param inventories Inventories to check
     * @author APOLLOSOFTWARE
     * @author EtherealTemplar
     *
     * Oh nice job lads, yes amazing, this is the most retarded thing I've ever seen. congrats.
     */
    public static void checkForSuspiciousDupedItems(Player player, final Set<Inventory> inventories) {
        if (Rank.isTrialGM(player)) return;
        if (EXCLUSIONS.contains(player.getUniqueId())) return;

        HashMultimap<Inventory, Tuple<ItemStack, String>> gearUids = HashMultimap.create();

        for (Inventory inv : inventories) {
            if (inv == null) continue;

            for (ItemStack i : inv.getContents()) {
                if (i == null || CraftItemStack.asNMSCopy(i) == null) continue;

                if (i.getAmount() <= 0) continue;
                if (ItemManager.isScrap(i) || ItemManager.isPotion(i) || ItemManager.isTeleportBook(i)) continue;

                String uniqueEpochIdentifier = AntiDuplication.getInstance().getUniqueEpochIdentifier(i);
                if (uniqueEpochIdentifier != null) for (int ii = 0; ii < i.getAmount(); ii++)
                    gearUids.put(inv, new Tuple<>(i, uniqueEpochIdentifier));
            }
        }

        checkForDuplications(player, gearUids);
    }

    private static void remove(Inventory inventory, String uniqueEpochIdentifier) {
        for (ItemStack i : inventory) {
            if (i == null || CraftItemStack.asNMSCopy(i) == null) continue;
            if (i.getAmount() <= 0) continue;
            if (isRegistered(i))
                if (AntiDuplication.getInstance().getUniqueEpochIdentifier(i).equals(uniqueEpochIdentifier))
                    inventory.remove(i);
        }
    }


    private static int traceCount(Inventory inventory, String uniqueEpochIdentifier) {
        int amount = 0;
        for (ItemStack i : inventory) {
            if (i == null || CraftItemStack.asNMSCopy(i) == null) continue;
            if (i.getAmount() <= 0) continue;
            if (isRegistered(i))
                if (AntiDuplication.getInstance().getUniqueEpochIdentifier(i).equals(uniqueEpochIdentifier))
                    amount += i.getAmount();
        }
        return amount;
    }

    /**
     * Returns the actual Epoch Unix String Identifier
     *
     * @param item
     * @return
     * @since 1.0
     */
    public String getUniqueEpochIdentifier(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        if (nmsStack == null) return null;
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || !tag.hasKey("u")) return null;
        return tag.getString("u");
    }

    /**
     * Check to see if item contains 'u' field.
     *
     * @param item
     * @return
     * @since 1.0
     */
    public static boolean isRegistered(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        return !(nmsStack == null || nmsStack.getTag() == null) && nmsStack.getTag().hasKey("u");
    }

    /**
     * Adds a (u) to the item. (u) -> UNIQUE IDENTIFIER
     *
     * @param item
     * @return
     * @since 1.0
     */
    public ItemStack applyAntiDupe(ItemStack item) {
        net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nmsStack.getTag();
        if (tag == null || tag.hasKey("u")) return item;
        tag.set("u", new NBTTagString(System.currentTimeMillis() + item.getType().toString() + item.getType().getMaxStackSize() + item.getType().getMaxDurability() + item.getDurability() + new Random().nextInt(99999) + "R"));
        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public ItemStack applyNewUID(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("u", System.currentTimeMillis() + item.getType().toString() + item.getType().getMaxStackSize() + item.getType().getMaxDurability() + item.getDurability() + new Random().nextInt(999) + "R");
        return nbtItem.getItem();
    }


}
