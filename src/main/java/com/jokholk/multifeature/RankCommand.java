package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {
    private final MainPlugin plugin;

    public RankCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("multifeature.admin")) {
            sender.sendMessage("§cNo permission!");
            return false;
        }

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /rank <player> <rank>");
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found.");
            return false;
        }

        String rank = args[1].toUpperCase();
        if (!plugin.getRankSystem().isValidRank(rank)) {
            sender.sendMessage("§cInvalid rank! Valid ranks: " + String.join(", ", plugin.getRankSystem().getRanks()));
            return false;
        }

        plugin.getRankSystem().setRank(target, rank);
        plugin.updatePlayerNametag(target);      // Add this line
        plugin.updatePlayerScoreboard(target);   // Add this line
        sender.sendMessage("§aSet rank " + rank + " for " + target.getName());
        return true;
    }
}