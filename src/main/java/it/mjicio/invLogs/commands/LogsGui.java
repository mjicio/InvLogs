package it.mjicio.invLogs.commands;

import it.mjicio.invLogs.InvLogs;
import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogsGui implements CommandExecutor, Listener {

    private final InvLogs plugin;
    private final InventorySave inventorySave;

    private final Map<String, int[]> playerLogIds = new HashMap<>();

    public LogsGui(InvLogs plugin, InventorySave inventorySave) {
        this.plugin = plugin;
        this.inventorySave = inventorySave;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "ꜱᴏʟᴏ ɪ ɢɪᴏᴄᴀᴛᴏʀɪ ᴘᴏꜱꜱᴏɴᴏ ᴇꜱᴇɢᴜɪʀᴇ Qᴜᴇꜱᴛᴏ ᴄᴏᴍᴀɴᴅᴏ."));
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cᴜꜱᴏ ᴄᴏʀʀᴇᴛᴛᴏ: /ɪɴᴠʟᴏɢꜱ [ᴘʟᴀʏᴇʀ]"));
            return true;
        }

        Player viewer = (Player) sender;
        String targetName = args[0];

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = plugin.getDb().getConnection()) {
                String uuid = Bukkit.getOfflinePlayer(targetName).getUniqueId().toString();
                String sql = "SELECT id, timestamp, evento FROM inventory_logs WHERE uuid = ? ORDER BY timestamp DESC LIMIT 54";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, uuid);
                ResultSet rs = stmt.executeQuery();

                List<ItemStack> items = new ArrayList<>();
                int[] ids = new int[54]; // Array per memorizzare gli ID per ogni slot
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                int slot = 0;

                while (rs.next() && slot < 54) {
                    int id = rs.getInt("id");
                    long timestamp = rs.getLong("timestamp");
                    String evento = rs.getString("evento");

                    ItemStack icon = new ItemStack(Material.BOOK);
                    ItemMeta meta = icon.getItemMeta();

                    String title = plugin.getConfig().getString("gui.item-title", "&eLog: &f%evento%");
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
                    ids[slot] = id;
                    slot++;
                }


                playerLogIds.put(viewer.getName(), ids);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    String guiTitle = ChatColor.translateAlternateColorCodes('&',
                            plugin.getConfig().getString("gui.title", "&8Logs di Inventario").replace("%player%", targetName));
                    Inventory gui = Bukkit.createInventory(null, 54, guiTitle);

                    for (int i = 0; i < items.size(); i++) {
                        gui.setItem(i, items.get(i));
                    }

                    viewer.openInventory(gui);
                });

            } catch (SQLException e) {
                plugin.getLogger().severe("Errore durante il recupero dei log: " + e.getMessage());
                viewer.sendMessage(ChatColor.RED + "Errore durante il recupero dei log.");
            }
        });

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String guiTitle = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("gui.title", "&8Logs di Inventario").replace("%player%", player.getName()));


        if (!event.getView().getTitle().equals(guiTitle)) {
            return;
        }


        event.setCancelled(true);


        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() != Material.BOOK) {
            return;
        }


        int slot = event.getRawSlot();
        if (slot >= 54) {
            return;
        }


        int[] ids = playerLogIds.get(player.getName());
        if (ids == null || ids[slot] == 0) {
            player.sendMessage(ChatColor.RED + "ɴᴇꜱꜱᴜɴ ʟᴏɢ ᴅɪꜱᴘᴏɴɪʙɪʟᴇ ɪɴ Qᴜᴇꜱᴛᴏ ꜱʟᴏᴛ.");
            return;
        }

        int logId = ids[slot];
        player.sendMessage(ChatColor.GREEN + "ʀᴇᴄᴜᴘᴇʀᴏ ɪɴᴠᴇɴᴛᴀʀɪᴏ ᴄᴏɴ ɪᴅ: " + logId + "...");


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "SELECT inventario FROM inventory_logs WHERE id = ?";
            try (Connection connection = plugin.getDb().getConnection();
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, logId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String inventoryData = rs.getString("inventario");
                    if (inventoryData == null || inventoryData.isEmpty()) {
                        Bukkit.getScheduler().runTask(plugin, () ->
                                player.sendMessage(ChatColor.RED + "Dati dell'inventario non validi."));
                        return;
                    }

                    ItemStack[] restoredItems = deserializeInventory(inventoryData);


                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String invTitle = ChatColor.translateAlternateColorCodes('&',
                                "&8Inventario - Log #" + logId);
                        Inventory displayInv = Bukkit.createInventory(null, 54, invTitle);

                        for (int i = 0; i < restoredItems.length && i < 54; i++) {
                            displayInv.setItem(i, restoredItems[i]);
                        }

                        player.openInventory(displayInv);
                    });
                } else {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.RED + "Inventario non trovato nel database."));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Errore nel recupero dell'inventario: " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () ->
                        player.sendMessage(ChatColor.RED + "Errore nel recupero dell'inventario dal database."));
            }
        });
    }

    private ItemStack[] deserializeInventory(String inventoryData) {
        try {
            byte[] data = Base64.getDecoder().decode(inventoryData);
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                 BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(byteArrayInputStream)) {

                int length = objectInputStream.readInt();
                ItemStack[] items = new ItemStack[length];
                for (int i = 0; i < length; i++) {
                    items[i] = (ItemStack) objectInputStream.readObject();
                }
                return items;
            }
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().severe("Errore durante la deserializzazione dell'inventario: " + e.getMessage());
            e.printStackTrace();
            return new ItemStack[0];
        }
    }
}