package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RankCommand implements CommandExecutor, TabCompleter {

    private final MainPlugin plugin;

    public RankCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /rank <player> <rank>");
            return true;
        }

        String targetName = args[0];
        String inputRank = args[1].toUpperCase();

        // ===== PERMISSION CHECK =====
        if (!sender.hasPermission("multifeature.admin")) {
            sender.sendMessage("§cYou do not have permission to manage ranks.");
            return true;
        }

        // ===== RANK VALIDATION =====
        if (!plugin.getRankSystem().isValidRank(inputRank)) {
            sender.sendMessage("§cInvalid rank!");
            sender.sendMessage("§7Valid ranks: "
                    + String.join(", ", plugin.getRankSystem().getRanks()));
            return true;
        }

        // ===== ONLINE PLAYER =====
        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            plugin.getRankSystem().setRank(target, inputRank);
            plugin.updatePlayerNametag(target);
            plugin.updatePlayerScoreboard(target);

            sender.sendMessage("§aSet rank §e" + inputRank + " §afor §e" + target.getName());
            target.sendMessage("§aYour rank has been changed to §e" + inputRank);

            plugin.getLogger().info(
                    sender.getName() + " set rank of " + target.getName() + " -> " + inputRank
            );

            return true;
        }

        // ===== OFFLINE PLAYER =====
        UUID uuid = plugin.getRankSystem().findOfflineUUID(targetName);

        if (uuid == null) {
            sender.sendMessage("§cPlayer §e" + targetName + " §chas never joined this server.");
            return true;
        }

        plugin.getRankSystem().setRankByUUID(uuid, inputRank);

        sender.sendMessage("§aSet rank §e" + inputRank
                + " §afor offline player §e" + targetName);
        sender.sendMessage("§7Rank will apply when they next join.");

        plugin.getLogger().info(
                sender.getName() + " set rank of offline " + targetName
                        + " (" + uuid + ") -> " + inputRank
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String label, String[] args) {

        if (!sender.hasPermission("multifeature.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return plugin.getRankSystem().getRanks().stream()
                    .filter(r -> r.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
