package net.dungeonrealms.listeners;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.dungeonrealms.duel.DuelMechanics;
import net.dungeonrealms.duel.DuelWager;
import net.dungeonrealms.items.Item;
import net.dungeonrealms.items.Item.ItemTier;
import net.dungeonrealms.mechanics.ItemManager;
import net.dungeonrealms.mongo.DatabaseAPI;
import net.dungeonrealms.mongo.EnumData;
import net.dungeonrealms.shops.Shop;
import net.dungeonrealms.shops.ShopMechanics;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * Created by Nick on 9/18/2015.
 */
public class InventoryListener implements Listener {

	/**
	 * Disables the clicking of items that contain NBTTag `important` in `type`.
	 * 
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getCurrentItem() == null)
			return;
		net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(event.getCurrentItem());
		if (nmsItem == null)
			return;
		NBTTagCompound tag = nmsItem.getTag();
		if (tag == null || !tag.getString("type").equalsIgnoreCase("important"))
			return;
		event.setCancelled(true);
	}
	/**
	 * Handling Shops being clicked.
	 * @param event
	 * @since 1.0
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void shopClicked(InventoryClickEvent event) {
		if (event.getInventory().getTitle().contains("@")) {
			String owner = event.getInventory().getTitle().split("@")[1];
			Player shopOwner = Bukkit.getPlayer(owner);
			Player clicker = (Player) event.getWhoClicked();
			Shop shop = ShopMechanics.shops.get(shopOwner.getUniqueId());
			ItemStack item = event.getCurrentItem();
			if (item != null) {
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
			if (clicker.getUniqueId() == shopOwner.getUniqueId()) {
				if (nms.hasTag()) {
					if (nms.getTag().hasKey("status")) {
						event.setCancelled(true);
						if (nms.getTag().getString("status").equalsIgnoreCase("off")) {
						shop.isopen = true;
						int slot = event.getRawSlot();
						ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.LIME.getDyeData());
						ItemMeta meta = button.getItemMeta();
						meta.setDisplayName(ChatColor.RED.toString() + "Close Shop");
						button.setItemMeta(meta);
						net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
						nmsButton.getTag().setString("status", "on");
						shop.inventory.setItem(slot, CraftItemStack.asBukkitCopy(nmsButton));
						} else {
						shop.isopen = false;
						ItemStack button = new ItemStack(Material.INK_SACK, 1, DyeColor.GRAY.getDyeData());
						ItemMeta meta = button.getItemMeta();
						meta.setDisplayName(ChatColor.YELLOW.toString() + "Open Shop");
						button.setItemMeta(meta);
						net.minecraft.server.v1_8_R3.ItemStack nmsButton = CraftItemStack.asNMSCopy(button);
						nmsButton.getTag().setString("status", "off");
						shop.inventory.setItem(8, CraftItemStack.asBukkitCopy(nmsButton));

						}
					}
				} else {
					if (shop.isopen){
//						clicker.closeInventory();
						clicker.sendMessage(ChatColor.RED + "You must close the shop before you can edit");
						event.setCancelled(true);
					}
				}
			} else {
				event.setCancelled(true);
			}
			}
		}
	}

	/**
	 * @param event
	 * @since 1.0 Handling wager inventory, when a player clicks the inventory.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDuelWagerClick(InventoryClickEvent e) {
		if (e.getInventory().getTitle().contains("vs.")) {
			if (e.isShiftClick()) {
			e.setCancelled(true);
			return;
			}
			Player p = (Player) e.getWhoClicked();
			DuelWager wager = DuelMechanics.getWager(p.getUniqueId());
			int slot = e.getRawSlot();
			ItemStack stack = e.getCurrentItem();
			if (stack == null)
			return;
			if (stack.getType() == Material.BONE) {
			e.setCancelled(true);
			return;
			} else if (slot == 30) {
			e.setCancelled(true);
			wager.cycleArmor();
			} else if (slot == 32) {
			e.setCancelled(true);
			wager.cycleWeapon();
			} else if (slot == 0) {
			if (wager.isLeft(p)) {
				// Left clicked
				e.setCancelled(true);
				if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "ready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Ready");
					wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
					if (CraftItemStack.asNMSCopy(e.getInventory().getItem(8)).getTag().getString("state")
						.equalsIgnoreCase("ready")) {
						wager.startDuel();
					}
				} else {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "notready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Not Ready");
					wager.setItemSlot(0, CraftItemStack.asBukkitCopy(nms));
				}
			} else {
				e.setCancelled(true);
				return;
			}
			} else if (slot == 8) {
			if (!wager.isLeft(p)) {
				// Right Clicked
				e.setCancelled(true);
				if (CraftItemStack.asNMSCopy(stack).getTag().getString("state").equalsIgnoreCase("notready")) {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Ready", null, DyeColor.LIME.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "ready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Ready");
					wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
					if (CraftItemStack.asNMSCopy(e.getInventory().getItem(0)).getTag().getString("state")
						.equalsIgnoreCase("ready")) {
						wager.startDuel();
					}
				} else {
					ItemStack item = ItemManager.createItemWithData(Material.INK_SACK,
						ChatColor.YELLOW.toString() + "Not Ready", null, DyeColor.GRAY.getDyeData());
					net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setString("state", "notready");
					nms.setTag(nbt);
					nms.c(ChatColor.YELLOW.toString() + "Not Ready");
					wager.setItemSlot(8, CraftItemStack.asBukkitCopy(nms));
				}
			} else {
				e.setCancelled(true);
				return;
			}
			} else if (slot < 36) {
			if (e.isLeftClick()) {
				if (isLeftSlot(slot)) {
					if (wager.isLeft(p)) {

					} else {
						e.setCancelled(true);
					}
				} else {
					if (!wager.isLeft(p)) {
					} else {
						e.setCancelled(true);
					}
				}
			} else {
				e.setCancelled(true);
			}
			}
		}
	}

	/**
	 * @param slot
	 *           Check if slot is specified slot
	 */
	private boolean isLeftSlot(int slot) {
		int[] left = new int[] { 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21 };
		for (int i = 0; i < left.length; i++)
			if (left[i] == slot)
			return true;
		return false;
	}

	/**
	 * @param event
	 * @since 1.0 Dragging is naughty.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDragItemInDuelWager(InventoryDragEvent event) {
		if (event.getInventory().getTitle().contains("vs.") || event.getInventory().getTitle().contains("Bank"))
			event.setCancelled(true);
	}

	/**
	 * @param event
	 * @since 1.0 Called when a player swithced
	 */

	@EventHandler(priority = EventPriority.HIGH)
	public void playerSwitchItem(PlayerItemHeldEvent ev) {
		if (ev.getPlayer().isOp() || ev.getPlayer().getGameMode() == GameMode.CREATIVE)
			return;
		int slot = ev.getNewSlot();
		if (ev.getPlayer().getInventory().getItem(slot) != null) {
			net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack
				.asNMSCopy(ev.getPlayer().getInventory().getItem(slot));
			if (nms.hasTag()) {
			if (nms.getTag().hasKey("type") && nms.getTag().getString("type").equalsIgnoreCase("weapon")) {
				ItemTier tier = Item.ItemTier.getById(nms.getTag().getInt("itemTier"));
				int minLevel = tier.getRangeValues()[0];
				Player p = ev.getPlayer();
				int pLevel = (int) DatabaseAPI.getInstance().getData(EnumData.LEVEL, p.getUniqueId());
				if (pLevel < minLevel) {
					p.sendMessage(ChatColor.RED + "You must be level " + ChatColor.YELLOW.toString() + minLevel
						+ ChatColor.RED.toString() + " to wield this weapon!");
					ev.setCancelled(true);
				}
			}
			}
		}
	}

	/**
	 * @param event
	 * @since 1.0 Closes bother players wager inventory.
	 */

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDuelWagerClosed(InventoryCloseEvent event) {
		if (event.getInventory().getTitle().contains("vs.")) {
			Player p = (Player) event.getPlayer();
			DuelWager wager = DuelMechanics.getWager(p.getUniqueId());
			if (wager != null) {
			if (!wager.completed) {
				wager.giveItemsBack();
				DuelMechanics.removeWager(wager);
				wager.p1.closeInventory();
				wager.p2.closeInventory();
			}
			}
		}
	}
}
