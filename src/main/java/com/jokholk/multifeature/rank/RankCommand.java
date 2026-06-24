package com.jokholk.multifeature.rank;
import com.jokholk.multifeature.*;

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
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length != 2) {
            sender.sendMessage(Msg.RANK_USAGE.get());
            return true;
        }

        if (!sender.hasPermission("multifeature.admin")) {
            sender.sendMessage(Msg.RANK_NO_PERM.get());
            return true;
        }

        String targetName = args[0];
        String inputRank  = args[1].toUpperCase();

        if (!plugin.getRankSystem().isValidRank(inputRank)) {
            sender.sendMessage(Msg.RANK_INVALID.get());
            sender.sendMessage(Msg.RANK_VALID_LIST.fmt(Language.ENGLISH,
                    "ranks", String.join(", ", plugin.getRankSystem().getRanks())));
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            plugin.getRankSystem().setRank(target, inputRank);
            plugin.updatePlayerNametag(target);
            plugin.updatePlayerScoreboard(target);

            sender.sendMessage(Msg.RANK_SET_TO_SENDER.fmt(Language.ENGLISH,
                    "rank", inputRank, "name", target.getName()));
            target.sendMessage(Msg.RANK_SET_NOTIFY.fmt(target, "rank", inputRank));

            plugin.getLogger().info(
                    sender.getName() + " set rank of " + target.getName() + " -> " + inputRank);
            return true;
        }

        UUID uuid = plugin.getRankSystem().findOfflineUUID(targetName);

        if (uuid == null) {
            sender.sendMessage(Msg.RANK_NOT_FOUND.fmt(Language.ENGLISH, "name", targetName));
            return true;
        }

        plugin.getRankSystem().setRankByUUID(uuid, inputRank);
        sender.sendMessage(Msg.RANK_OFFLINE_SET.fmt(Language.ENGLISH,
                "rank", inputRank, "name", targetName));
        sender.sendMessage(Msg.RANK_OFFLINE_WILL_APPLY.get());

        plugin.getLogger().info(
                sender.getName() + " set rank of offline " + targetName
                        + " (" + uuid + ") -> " + inputRank);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

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
