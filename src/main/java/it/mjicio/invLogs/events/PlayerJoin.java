package it.mjicio.invLogs.events;

import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final InventorySave inventorySave;

    public PlayerJoin(InventorySave inventorySave) {
        this.inventorySave = inventorySave;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        inventorySave.savePlayerInventory(e.getPlayer(), "JOIN");
    }
}
