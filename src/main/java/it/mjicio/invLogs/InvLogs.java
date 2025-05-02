package it.mjicio.invLogs;

import it.mjicio.invLogs.commands.LogsGui;
import it.mjicio.invLogs.events.*;
import it.mjicio.invLogs.manager.InventorySave;
import it.mjicio.invLogs.manager.MySQLManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class InvLogs extends JavaPlugin {

    private MySQLManager db;
    private InventorySave inventorySave;
    private final Map<String, Integer> logGuiCache = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.db = new MySQLManager(this);
        this.db.createTable();
        this.inventorySave = new InventorySave(this, db);

        // Eventi
        getServer().getPluginManager().registerEvents(new PlayerJoin(inventorySave), this);
        getServer().getPluginManager().registerEvents(new PlayerQuit(inventorySave), this);
        getServer().getPluginManager().registerEvents(new PlayerDrop(inventorySave), this);
        getServer().getPluginManager().registerEvents(new PlayerDeath(inventorySave), this);
        getServer().getPluginManager().registerEvents(new PlayerPickUp(this, inventorySave), this);

        // Comandi
        getCommand("invlogs").setExecutor(new LogsGui(this, inventorySave));
    }

    public Map<String, Integer> getLogGuiCache() {
        return logGuiCache;
    }

    public MySQLManager getDb() {
        return db;
    }
}
