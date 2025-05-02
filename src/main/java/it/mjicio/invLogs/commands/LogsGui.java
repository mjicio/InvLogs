package it.mjicio.invLogs.commands;

import it.mjicio.invLogs.InvLogs;
import it.mjicio.invLogs.manager.MySQLManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogsGui implements CommandExecutor {

    private final InvLogs plugin;

    public LogsGui(InvLogs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo i giocatori possono eseguire questo comando.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Uso corretto: /invlog <player>");
            return true;
        }

        Player viewer = (Player) sender;
        String targetName = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = plugin.getDb().getConnection()) {
                String uuid = Bukkit.getOfflinePlayer(targetName).getUniqueId().toString();
                String sql = "SELECT id, timestamp, evento FROM inventory_logs WHERE uuid = ? ORDER BY timestamp DESC LIMIT 57";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, uuid);
                ResultSet rs = stmt.executeQuery();

                List<ItemStack> items = new ArrayList<>();
                List<Integer> ids = new ArrayList<>();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                while (rs.next()) {
                    int id = rs.getInt("id");
                    long timestamp = rs.getLong("timestamp");
                    String evento = rs.getString("evento");

                    ItemStack icon = new ItemStack(Objects.requireNonNull(org.bukkit.Material.getMaterial(
                            plugin.getConfig().getString("gui.item"))));
                    ItemMeta meta = icon.getItemMeta();

                    String title = plugin.getConfig().getString("gui.item-title");
                    title = title.replace("%evento%", evento)
                            .replace("%data%", sdf.format(new Date(timestamp)));
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));

                    List<String> loreCfg = plugin.getConfig().getStringList("gui.item-lore");
                    List<String> lore = new ArrayList<>();
                    for (String line : loreCfg) {
                        lore.add(ChatColor.translateAlternateColorCodes('&',
                                line.replace("%evento%", evento).replace("%data%", sdf.format(new Date(timestamp)))));
                    }
                    meta.setLore(lore);
                    icon.setItemMeta(meta);
                    items.add(icon);
                    ids.add(id);
                }

                Bukkit.getScheduler().runTask(plugin, () -> {
                    Inventory gui = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("gui.title")));
                    for (int i = 0; i < items.size() && i < 54; i++) {
                        gui.setItem(i, items.get(i));
                    }
                    viewer.openInventory(gui);
                    plugin.getLogGuiCache().put(viewer.getName(), ids.get(0)); // salva l'id per eventuali azioni future
                });

            } catch (SQLException e) {
                plugin.getLogger().severe("Errore durante il recupero dei log: " + e.getMessage());
            }
        });

        return true;
    }
}