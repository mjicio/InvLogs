package it.mjicio.invLogs.events;

import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

public class PlayerDeath implements Listener {

    private final InventorySave inventorySave;

    public PlayerDeath(InventorySave inventorySave) {
        this.inventorySave = inventorySave;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        // Verifica che l'entità sia un giocatore
        Player player = e.getEntity() instanceof Player ? (Player) e.getEntity() : null;
        if (player != null) {
            inventorySave.savePlayerInventory(player, "DEATH");
        }
    }
}
