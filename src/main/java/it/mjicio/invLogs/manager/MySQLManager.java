package it.mjicio.invLogs.manager;

import it.mjicio.invLogs.InvLogs;

import java.sql.*;

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
        } catch (SQLException e) {
            plugin.getLogger().severe("Impossibile connettersi al database MySQL: " + e.getMessage());
        }
    }

    public Connection getConnection() {
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
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("Errore nella creazione della tabella: " + e.getMessage());
        }
    }

    public String getInventoryById(int id) {
        String sql = "SELECT inventario FROM inventory_logs WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("inventario");
        } catch (SQLException e) {
            plugin.getLogger().severe("Errore nel recupero inventario: " + e.getMessage());
        }
        return null;
    }
}