/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.indiplex.moreevents.event;

import de.indiplex.moreevents.IMNetHandler.InventorySlotType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Webteam
 */
public class InventoryClickEvent extends InventoryEvent {
    private InventorySlotType inventorySlotType;
    private ItemStack itemStack;
    private Player player;
    private int clicked;
    private static final HandlerList handlers = new HandlerList();

    public InventoryClickEvent(Inventory inventory, InventorySlotType inventorySlotType, ItemStack itemStack, Player player, int clicked) {
        super(inventory);
        this.inventorySlotType = inventorySlotType;
        this.itemStack = itemStack;
        this.player = player;
        this.clicked = clicked;
    }

    public InventorySlotType getInventorySlotType() {
        return inventorySlotType;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getClicked() {
        return clicked;
    }

    public Player getPlayer() {
        return player;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public String toString() {
        if (inventorySlotType==null) {
            return "inventorySlotType is null!";
        }
        if (itemStack==null) {
            return "itemStack is null!";
        }
        if (inventory==null) {
            return "inventory is null!";
        }
        return "ist: "+inventorySlotType.toString()+"; it: "+itemStack.toString()+"; inv: "+inventory.toString();
    }
    
}
