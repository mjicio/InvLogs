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

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = db.getConnection()) {
                // Disabilita l'autocommit per garantire l'integrità della transazione
                connection.setAutoCommit(false);

                try {
                    // Conta i salvataggi esistenti per questo giocatore
                    String sqlCount = "SELECT COUNT(*) FROM inventory_logs WHERE uuid = ?";
                    try (PreparedStatement countStmt = connection.prepareStatement(sqlCount)) {
                        countStmt.setString(1, uuid);
                        ResultSet rs = countStmt.executeQuery();

                        if (rs.next() && rs.getInt(1) >= 54) {
                            // Trova l'ID del salvataggio più vecchio
                            String sqlFindOldest = "SELECT id FROM inventory_logs WHERE uuid = ? ORDER BY timestamp ASC LIMIT 1";
                            try (PreparedStatement findStmt = connection.prepareStatement(sqlFindOldest)) {
                                findStmt.setString(1, uuid);
                                ResultSet oldestRs = findStmt.executeQuery();

                                if (oldestRs.next()) {
                                    int oldestId = oldestRs.getInt("id");

                                    // Aggiorna il record esistente invece di eliminarlo e inserirne uno nuovo
                                    // Questo mantiene l'ID ma aggiorna tutti i dati
                                    String sqlUpdate = "UPDATE inventory_logs SET evento = ?, timestamp = ?, inventario = ? WHERE id = ?";
                                    try (PreparedStatement updateStmt = connection.prepareStatement(sqlUpdate)) {
                                        updateStmt.setString(1, evento);
                                        updateStmt.setLong(2, timestamp);
                                        updateStmt.setString(3, inventoryData);
                                        updateStmt.setInt(4, oldestId);
                                        updateStmt.executeUpdate();
                                    }
                                }
                            }
                        } else {
                            // Se non abbiamo ancora 54 salvataggi, inseriamo normalmente
                            String sqlInsert = "INSERT INTO inventory_logs (uuid, evento, timestamp, inventario) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement insertStmt = connection.prepareStatement(sqlInsert)) {
                                insertStmt.setString(1, uuid);
                                insertStmt.setString(2, evento);
                                insertStmt.setLong(3, timestamp);
                                insertStmt.setString(4, inventoryData);
                                insertStmt.executeUpdate();
                            }
                        }
                    }

                    // Commit della transazione
                    connection.commit();

                } catch (SQLException e) {
                    // Rollback in caso di errore
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackEx) {
                        plugin.getLogger().severe("Errore durante il rollback: " + rollbackEx.getMessage());
                    }
                    plugin.getLogger().severe("Errore durante il salvataggio dell'inventario: " + e.getMessage());

                } finally {
                    // Ripristina l'autocommit
                    try {
                        connection.setAutoCommit(true);
                    } catch (SQLException autoCommitEx) {
                        plugin.getLogger().severe("Errore ripristino autocommit: " + autoCommitEx.getMessage());
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Errore connessione database: " + e.getMessage());
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