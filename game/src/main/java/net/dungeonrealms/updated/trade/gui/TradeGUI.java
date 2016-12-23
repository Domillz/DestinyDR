package net.dungeonrealms.updated.trade.gui;

import com.google.common.collect.Maps;
import lombok.Getter;
import net.dungeonrealms.updated.trade.ItemTrade;
import net.dungeonrealms.updated.trade.gui.action.ClickAction;
import net.dungeonrealms.updated.trade.gui.action.EnumClicker;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Created by Giovanni on 23-12-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class TradeGUI implements ITradeScreen {

    @Getter
    private ItemTrade correspondingTrade;

    @Getter
    private Map<Integer, ItemStack> itemsInScreen;

    public TradeGUI(ItemTrade itemTrade) {
        this.correspondingTrade = itemTrade;
        this.itemsInScreen = Maps.newHashMap();
    }

    /**
     * Update the status of the trade
     *
     * @param clicker   The clicker
     * @param inventory The inventory
     */
    public void updateStatus(EnumClicker clicker, Inventory inventory, boolean status) {
        switch (clicker) {
            case OWNER:
                inventory.setItem(0, this.getButton(status));
                this.correspondingTrade.setOwnerAccepted(status);
                break;
            case PARTICIPATOR:
                inventory.setItem(8, this.getButton(status));
                this.correspondingTrade.setParticipatorAccepted(status);
                break;
        }
    }

    @Override
    public void handleClick(ClickAction clickAction, InventoryClickEvent event) {
        // Clicked slot is 36, or under 36
        event.setCancelled(true);
        if (clickAction.getSlot() <= 36) {
            this.updateStatus(clickAction.getClicker(), event.getInventory(), false);
            Player player = (Player) event.getWhoClicked();
            // Player is clicking a disallowed slot
            if (!this.disallowedSlots().contains(clickAction.getSlot())) {
                // Check if the clicker can actually click on it's side
                if (this.allowedSlots(clickAction.getClicker()).contains(clickAction.getSlot())) {
                    switch (clickAction.getClickAction()) {
                        case ADD:
                            if (clickAction.getItemStack() != null) {
                                // Is the slot already occupied?
                                if (!this.itemsInScreen.containsKey(clickAction.getSlot())) {
                                    this.itemsInScreen.put(clickAction.getSlot(), clickAction.getItemStack());
                                    // Register item
                                    switch (clickAction.getClicker()) {
                                        case OWNER:
                                            // Remove & set item
                                            Bukkit.getPlayer(this.correspondingTrade.getOwner()).getInventory().removeItem(clickAction.getItemStack());
                                            this.correspondingTrade.getOwnerItems().add(clickAction.getItemStack());
                                            break;
                                        case PARTICIPATOR:
                                            // Remove & set item
                                            Bukkit.getPlayer(this.correspondingTrade.getParticipator()).getInventory().removeItem(clickAction.getItemStack());
                                            this.correspondingTrade.getParticipatorItems().add(clickAction.getItemStack());
                                            break;
                                    }
                                } else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                                }
                            }
                            break;
                        case REMOVE:
                            // Verify existence
                            if (this.itemsInScreen.containsKey(clickAction.getSlot()) && this.itemsInScreen.get(clickAction.getSlot()) != null) {
                                switch (clickAction.getClicker()) {
                                    case OWNER:
                                        // Add back
                                        Bukkit.getPlayer(this.correspondingTrade.getOwner()).getInventory().addItem(this.itemsInScreen.get(clickAction.getSlot()));
                                        // Remove after giving back
                                        this.itemsInScreen.remove(clickAction.getSlot());
                                        this.correspondingTrade.getOwnerItems().remove(clickAction.getItemStack());
                                        break;
                                    case PARTICIPATOR:
                                        // Add back
                                        Bukkit.getPlayer(this.correspondingTrade.getParticipator()).getInventory().addItem(this.itemsInScreen.get(clickAction.getSlot()));
                                        // Remove after giving back
                                        this.itemsInScreen.remove(clickAction.getSlot());
                                        this.correspondingTrade.getParticipatorItems().remove(clickAction.getItemStack());
                                        break;
                                }
                            }
                            break;
                    }
                } else {
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                }
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
            }
        }
    }
}
