package it.mjicio.invLogs.manager;

import it.mjicio.invLogs.InvLogs;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class MySQLManager {

    private final InvLogs plugin;
    private Connection connection;

    public MySQLManager(InvLogs plugin) {
        this.plugin = plugin;
        connect();
    }


    public void connect() {
        String host = plugin.getConfig().getString("mysql.host");
        String port = plugin.getConfig().getString("mysql.port");
        String database = plugin.getConfig().getString("mysql.database");
        String username = plugin.getConfig().getString("mysql.username");
        String password = plugin.getConfig().getString("mysql.password");

        try {

            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
            plugin.getLogger().info("Connessione al database MySQL riuscita.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Impossibile connettersi al database MySQL: " + e.getMessage());
        }
    }


    public Connection getConnection() throws SQLException {

        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }


    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS inventory_logs (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "uuid VARCHAR(36)," +
                "evento VARCHAR(16)," +
                "timestamp BIGINT," +
                "inventario LONGTEXT" +
                ")";

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                plugin.getLogger().severe("Errore nella creazione della tabella: " + e.getMessage());
            }
        });
    }


    public String getInventoryById(int id) {
        String sql = "SELECT inventario FROM inventory_logs WHERE id = ?";
        CompletableFuture<String> result = new CompletableFuture<>();


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    result.complete(rs.getString("inventario"));
                } else {
                    result.complete(null);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Errore nel recupero inventario: " + e.getMessage());
                result.completeExceptionally(e);
            }
        });


        try {
            return result.get();
        } catch (Exception e) {
            plugin.getLogger().severe("Errore durante il recupero dell'inventario: " + e.getMessage());
            return null;
        }
    }
}