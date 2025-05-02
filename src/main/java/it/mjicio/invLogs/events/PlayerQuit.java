package it.mjicio.invLogs.events;

import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

    private final InventorySave inventorySave;

    public PlayerQuit(InventorySave inventorySave) {
        this.inventorySave = inventorySave;
    }


    @EventHandler
    public void onPlayerJoin(PlayerQuitEvent p) {

        inventorySave.savePlayerInventory(p.getPlayer(), "QUIT");

    }

}
