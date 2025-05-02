package it.mjicio.invLogs.commands;

import it.mjicio.invLogs.InvLogs;
import it.mjicio.invLogs.manager.InventorySave;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ForceSave implements CommandExecutor {

    private final InvLogs plugin;
    private final InventorySave inventorySave;

    public ForceSave(InvLogs plugin, InventorySave inventorySave) {
        this.plugin = plugin;
        this.inventorySave = inventorySave;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo i giocatori possono eseguire questo comando.");
            return true;
        }

        Player executor = (Player) sender;


        if (args.length != 1) {
            executor.sendMessage("Uso corretto: /invlogs forcesave <player>");
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);


        if (targetPlayer == null) {
            executor.sendMessage(ChatColor.RED + "Il giocatore " + targetName + " non è online.");
            return true;
        }


        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            inventorySave.savePlayerInventory(targetPlayer, "FORCE_SAVE");
            executor.sendMessage(ChatColor.GREEN + "Inventario di " + targetName + " salvato forzatamente.");
        });

        return true;
    }
}
