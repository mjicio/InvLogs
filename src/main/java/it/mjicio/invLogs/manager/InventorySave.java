package it.mjicio.invLogs.manager;

import it.mjicio.invLogs.InvLogs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Base64;

public class InventorySave {

    private final InvLogs plugin;
    private final MySQLManager db;

    public InventorySave(InvLogs plugin, MySQLManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    public void savePlayerInventory(Player player, String evento) {
        String uuid = player.getUniqueId().toString();
        String inventoryData = serializeInventory(player.getInventory());
        long timestamp = System.currentTimeMillis();


        String sqlInsert = "INSERT INTO inventory_logs (uuid, evento, timestamp, inventario) VALUES (?, ?, ?, ?)";
        String sqlCount = "SELECT COUNT(*) FROM inventory_logs WHERE uuid = ?";
        String sqlDeleteOldest = "DELETE FROM inventory_logs WHERE uuid = ? ORDER BY timestamp ASC LIMIT 1";


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = db.getConnection()) {

                try (PreparedStatement countStmt = connection.prepareStatement(sqlCount)) {
                    countStmt.setString(1, uuid);
                    ResultSet rs = countStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) >= 54) {

                        try (PreparedStatement delStmt = connection.prepareStatement(sqlDeleteOldest)) {
                            delStmt.setString(1, uuid);
                            delStmt.executeUpdate();
                        }
                    }
                }


                try (PreparedStatement insertStmt = connection.prepareStatement(sqlInsert)) {
                    insertStmt.setString(1, uuid);
                    insertStmt.setString(2, evento);
                    insertStmt.setLong(3, timestamp);
                    insertStmt.setString(4, inventoryData);
                    insertStmt.executeUpdate();
                }

            } catch (SQLException e) {
                plugin.getLogger().severe("Errore salvataggio inventario: " + e.getMessage());
            }
        });
    }


    private String serializeInventory(PlayerInventory inventory) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            ItemStack[] contents = inventory.getContents();
            dataOutput.writeInt(contents.length);
            for (ItemStack item : contents) {
                dataOutput.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (IOException e) {
            plugin.getLogger().severe("Errore durante la serializzazione dell'inventario: " + e.getMessage());
            return null;
        }
    }
}
