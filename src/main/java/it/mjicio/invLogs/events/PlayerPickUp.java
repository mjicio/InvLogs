package it.mjicio.invLogs.events;

import it.mjicio.invLogs.InvLogs;
import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickUp implements Listener {

    private final InventorySave inventorySave;
    private final InvLogs plugin;

    public PlayerPickUp(InvLogs plugin, InventorySave inventorySave) {
        this.plugin = plugin;
        this.inventorySave = inventorySave;
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();


        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {

            if (player.isOnline()) {
                inventorySave.savePlayerInventory(player, "PICKUP");
            }
        }, 40L);
    }
}