package it.mjicio.invLogs.events;

import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickUp implements Listener {

    private final InventorySave inventorySave;

    public PlayerPickUp(InventorySave inventorySave) {
        this.inventorySave = inventorySave;
    }

    @EventHandler
    public void onPlayerPickup(PlayerPickupItemEvent e) {
        inventorySave.savePlayerInventory(e.getPlayer(), "PICKUP");
    }

}
