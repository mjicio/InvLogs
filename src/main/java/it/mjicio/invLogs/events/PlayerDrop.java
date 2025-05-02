package it.mjicio.invLogs.events;

import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.http.WebSocket;

public class PlayerDrop implements Listener {

    private final InventorySave inventorySave;

    public PlayerDrop(InventorySave inventorySave) {
        this.inventorySave = inventorySave;
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent e) {
        inventorySave.savePlayerInventory(e.getPlayer(), "DROP");
    }

}
