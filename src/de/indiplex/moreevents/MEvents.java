/*
 * MoreEvents
 * Copyright (C) 2012 IndiPlex
 * 
 * MoreEvents is free software: you can redistribute it and/or modify
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

import de.indiplex.manager.IPMAPI;
import de.indiplex.manager.IPMPlugin;
import java.lang.reflect.Field;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.NetworkManager;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventoryPlayerClickEvent;
import org.getspout.spoutapi.event.inventory.InventorySlotType;

/**
 *
 * @author IndiPlex <Cartan12@indiplex.de>
 */
public class MEvents extends IPMPlugin {

    @Override
    public void onEnable() {

        Listener l = null;

        if (getServer().getPluginManager().isPluginEnabled("Spout")) {
            l = new Listener() {
                @EventHandler(priority=EventPriority.HIGH)
                public void onSpoutInventoryClickEvent(InventoryClickEvent event) {
                    if (event.getItem()==null) {
                        return;
                    }
                    getServer().getPluginManager().callEvent(createEvent(event));
                }
                
                @EventHandler(priority=EventPriority.HIGH)
                public void onSpoutInventoryPlayerClickEvent(InventoryPlayerClickEvent event) {
                    if (event.getItem()==null) {
                        return;
                    }
                    getServer().getPluginManager().callEvent(createEvent(event));
                }
                
                private Event createEvent(InventoryClickEvent event) {
                    Event e = new de.indiplex.moreevents.event.InventoryClickEvent(event.getInventory(), getSlotTypeBySpoutType(event.getSlotType()), event.getItem(), event.getPlayer(), event.getRawSlot());
                    return e;
                }
            };
        } else {
            l = new Listener() {

                @EventHandler(priority = EventPriority.HIGH)
                public void onJoin(PlayerJoinEvent e) {
                    CraftPlayer p = (CraftPlayer) e.getPlayer();
                    CraftServer cs = (CraftServer) getServer();
                    Location loc = p.getLocation();
                    IMNetHandler imnh = new IMNetHandler(cs.getHandle().server, p.getHandle().netServerHandler.networkManager, p.getHandle());
                    imnh.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                    p.getHandle().netServerHandler = imnh;
                    NetworkManager nm = p.getHandle().netServerHandler.networkManager;
                    setNetServerHandler(nm, imnh);
                    cs.getServer().networkListenThread.a(imnh);
                }
            };
        }

        getServer().getPluginManager().registerEvents(l, this);
    }

    @Override
    public void onDisable() {
    }

    private boolean setNetServerHandler(NetworkManager nm, NetServerHandler nsh) {
        try {
            Field p = nm.getClass().getDeclaredField("packetListener");
            p.setAccessible(true);
            p.set(nm, nsh);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private IMNetHandler.InventorySlotType getSlotTypeBySpoutType(InventorySlotType st) {
        return IMNetHandler.InventorySlotType.valueOf(st.toString());
    }
    
    private static IPMAPI API;

    @Override
    protected void init(IPMAPI API) {
        MEvents.API = API;
    }

    public static IPMAPI getAPI() {
        return API;
    }
    
}
