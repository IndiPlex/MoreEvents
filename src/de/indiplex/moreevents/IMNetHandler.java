/*
 * InventoryManager
 * Copyright (C) 2012 IndiPlex
 * 
 * InventoryManager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.indiplex.moreevents;

import de.indiplex.moreevents.event.InventoryClickEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import net.minecraft.server.Container;
import net.minecraft.server.ContainerBrewingStand;
import net.minecraft.server.ContainerChest;
import net.minecraft.server.ContainerDispenser;
import net.minecraft.server.ContainerEnchantTable;
import net.minecraft.server.ContainerFurnace;
import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.ContainerWorkbench;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IInventory;
import net.minecraft.server.IntHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import net.minecraft.server.Packet102WindowClick;
import net.minecraft.server.Packet106Transaction;
import net.minecraft.server.Slot;
import net.minecraft.server.TileEntityBrewingStand;
import net.minecraft.server.TileEntityDispenser;
import net.minecraft.server.TileEntityFurnace;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class IMNetHandler extends NetServerHandler {

    protected Field entityListField = null;

    public IMNetHandler(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);

        try {
            entityListField = NetServerHandler.class.getDeclaredField("r");
            entityListField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void a(Packet102WindowClick pack) {
        ItemStack is = new CraftItemStack(pack.e);
        InventoryClickEvent event = null;
        boolean can = false;
        if (!is.getType().equals(Material.AIR)) {
            Inventory inv = getActiveInventory();
            InventorySlotType ist = getActiveInventorySlotType(pack.b, inv);

            event = new InventoryClickEvent(inv, ist, is, new CraftItemStack(this.player.inventory.l()), getPlayer(), pack.b);
            Bukkit.getPluginManager().callEvent(event);
            can = event.isCancelled();
        }
        if (!can) {
            super.a(pack);
        } else {
            CraftItemStack slot = (CraftItemStack) event.getItemStack();
            CraftItemStack cursor = (CraftItemStack) event.getCursor();
            net.minecraft.server.ItemStack itemstack = slot != null ? slot.getHandle() : null;
            net.minecraft.server.ItemStack cursorstack = cursor != null ? cursor.getHandle() : null;

            if (pack.b != -999) { // Only swap if target is not OUTSIDE
                if (itemstack != null) {
                    player.activeContainer.b(pack.b).c(itemstack);
                    player.inventory.b((net.minecraft.server.ItemStack) null);
                }
                if (event.getCursor() != null) {
                    player.activeContainer.b(pack.b).c(itemstack);
                    player.inventory.b(cursorstack);
                }
            }

            getEntityList().a(Integer.valueOf(this.player.activeContainer.windowId), Short.valueOf(pack.d));
            player.netServerHandler.sendPacket(new Packet106Transaction(pack.a, pack.d, false));
            this.player.activeContainer.a(this.player, false);
            ArrayList<net.minecraft.server.ItemStack> arraylist = new ArrayList<net.minecraft.server.ItemStack>();

            for (int i = 0; i < this.player.activeContainer.e.size(); ++i) {
                arraylist.add(((Slot) this.player.activeContainer.e.get(i)).getItem());
            }

            this.player.a(this.player.activeContainer, arraylist);
        }
    }

    public IntHashMap getEntityList() {
        try {
            return (IntHashMap) entityListField.get(this);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public InventorySlotType getActiveInventorySlotType(int clicked, Inventory inv) {
        if (clicked == -999) {
            return InventorySlotType.OUTSIDE;
        }
        Inventory active = inv;
        int size = active.getSize();
        if (this.player.activeContainer instanceof ContainerChest) {
            return InventorySlotType.CONTAINER;
        } else if (this.player.activeContainer instanceof ContainerPlayer) {
            if (clicked == 0) {
                return InventorySlotType.RESULT;
            }
            if (clicked < 5) {
                return InventorySlotType.CRAFTING;
            }
            if (clicked == 5) {
                return InventorySlotType.HELMET;
            }
            if (clicked == 6) {
                return InventorySlotType.ARMOR;
            }
            if (clicked == 7) {
                return InventorySlotType.LEGGINGS;
            }
            if (clicked == 8) {
                return InventorySlotType.BOOTS;
            }
            if (clicked < size) {
                return InventorySlotType.CONTAINER;
            }
            return InventorySlotType.QUICKBAR;
        } else if (this.player.activeContainer instanceof ContainerFurnace) {
            if (clicked == 0) {
                return InventorySlotType.SMELTING;
            }
            if (clicked == 1) {
                return InventorySlotType.FUEL;
            }
            return InventorySlotType.RESULT;
        } else if (this.player.activeContainer instanceof ContainerDispenser) {
            return InventorySlotType.CONTAINER;
        } else if (this.player.activeContainer instanceof ContainerWorkbench) {
            if (clicked == 0) {
                return InventorySlotType.RESULT;
            } else if (clicked < size) {
                return InventorySlotType.CRAFTING;
            }
            return InventorySlotType.CONTAINER;
        }
        if (clicked >= size + 27) {
            return InventorySlotType.QUICKBAR;
        }
        if (clicked >= size) {
            return InventorySlotType.PACK;
        }
        return InventorySlotType.CONTAINER;
    }

    private Inventory getActiveInventory() {
        Container container = player.activeContainer;
        try {
            if (container instanceof ContainerChest) {
                Field a = ContainerChest.class.getDeclaredField("a");
                a.setAccessible(true);
                return new CraftInventory((IInventory) a.get(container));
            }
            if (container instanceof ContainerPlayer) {
                return new CraftInventoryPlayer(this.player.inventory);
            }
            if (container instanceof ContainerFurnace) {
                Field a = ContainerFurnace.class.getDeclaredField("a");
                a.setAccessible(true);
                return new CraftInventory((TileEntityFurnace) a.get(container));
            }
            if (container instanceof ContainerDispenser) {
                Field a = ContainerDispenser.class.getDeclaredField("a");
                a.setAccessible(true);
                return new CraftInventory((TileEntityDispenser) a.get(container));
            }
            if (container instanceof ContainerWorkbench) {
                return new CraftInventory(((ContainerWorkbench) container).craftInventory);
            }
            if (container instanceof ContainerBrewingStand) {
                Field a = ContainerBrewingStand.class.getDeclaredField("a");
                a.setAccessible(true);
                return new CraftInventory((TileEntityBrewingStand) a.get(container));
            }
            if (container instanceof ContainerEnchantTable) {
                return new CraftInventory(((ContainerEnchantTable) container).a);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CraftInventory(this.player.inventory);
        }
        return null;
    }

    public enum InventorySlotType {

        CONTAINER,
        PACK,
        QUICKBAR,
        RESULT,
        CRAFTING,
        FUEL,
        SMELTING,
        HELMET,
        BOOTS,
        LEGGINGS,
        ARMOR,
        OUTSIDE;
    }
}
