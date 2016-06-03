package net.dungeonrealms.game.menus;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dungeonrealms.DungeonRealms;
import net.dungeonrealms.game.mechanics.ItemManager;
import net.dungeonrealms.game.mongo.DatabaseAPI;
import net.dungeonrealms.game.mongo.EnumData;
import net.dungeonrealms.game.mongo.EnumOperators;
import net.dungeonrealms.game.player.inventory.PlayerMenus;
import net.dungeonrealms.game.world.entities.types.mounts.Mule;
import net.dungeonrealms.game.world.entities.types.mounts.mule.MuleTier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

import static com.comphenix.protocol.PacketType.Play.Client.CLIENT_COMMAND;
import static com.comphenix.protocol.PacketType.Play.Client.WINDOW_CLICK;

public class Profile implements Listener {

    private static PacketListener listener;

    public void onEnable() {
        listener = new PacketAdapter(DungeonRealms.getInstance(), CLIENT_COMMAND, WINDOW_CLICK) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();
                if (player.getGameMode() == GameMode.CREATIVE) return;
                PacketType type = packet.getType();
                if (type == CLIENT_COMMAND && packet.getClientCommands().read(0) == EnumWrappers.ClientCommand.OPEN_INVENTORY_ACHIEVEMENT) {
                    if (player.getOpenInventory().getTopInventory() instanceof CraftingInventory) {
                        player.getOpenInventory().getTopInventory().setItem(1, getItem(player));
                    }
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(listener);
        Bukkit.getServer().getPluginManager().registerEvents(this, DungeonRealms.getInstance());
    }

    public static ItemStack getItem(Player player) {
        return ItemManager.getPlayerProfile(player, ChatColor.WHITE.toString() + ChatColor.BOLD + "Character Profile", new String[]{ChatColor.GREEN + "Open Profile"});
    }

    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(listener);
        HandlerList.unregisterAll(this);
    }

    private static void callEvent(Player player) {
        player.closeInventory();
        PlayerMenus.openPlayerProfileMenu(player);
    }

    private static void addMountItem(Player player) {
        player.getInventory().addItem(ItemManager.getPlayerMountItem());
    }

    private static void addPetItem(Player player) {
        player.getInventory().addItem(ItemManager.getPlayerPetItem());
    }

    private static void addMuleItem(Player player) {
        if(player.getInventory().contains(Material.LEASH))return;

        Object muleTier = DatabaseAPI.getInstance().getData(EnumData.MULELEVEL, player.getUniqueId());
        if(muleTier == null){
            player.sendMessage(ChatColor.RED + "No mule data found.");
            DatabaseAPI.getInstance().update(player.getUniqueId(), EnumOperators.$SET, EnumData.MULELEVEL, 1, false);
            muleTier = 1;
        }
        MuleTier tier = MuleTier.getTier((int)muleTier);
        if(tier == null)return;
        player.getInventory().addItem(ItemManager.getPlayerMuleItem(tier));
    }

    private static void addTrailItem(Player player) {
        player.getInventory().addItem(ItemManager.getPlayerTrailItem());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getInventory() instanceof CraftingInventory && event.getInventory().getSize() == 5 && event.getRawSlot() == 1) {
            if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);
            callEvent((Player) event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerRequestItem(InventoryClickEvent event) {
        if (event.getInventory().getTitle().equals("Profile")) {
            if (event.getClick() == ClickType.MIDDLE) {
                switch (event.getRawSlot()) {
                    case 6:
                        event.setCancelled(true);
                        addTrailItem((Player) event.getWhoClicked());
                        break;
                    case 7:
                        event.setCancelled(true);
                        addMountItem((Player) event.getWhoClicked());
                        break;
                    case 8:
                        event.setCancelled(true);
                        addPetItem((Player) event.getWhoClicked());
                        break;
                    case 16:
                        event.setCancelled(true);
                        addMuleItem((Player)event.getWhoClicked());
                        break;
                }
            }
        }
    }
}

