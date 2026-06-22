package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ScoreboardCommand implements CommandExecutor {

    private final MainPlugin plugin;

    public ScoreboardCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender s,
                             Command c, String l, String[] a) {

        if (!(s instanceof Player p)) return true;

        if (a.length == 0) {
            p.sendMessage("§cUsage: /scoreboard on|off");
            return true;
        }

        if (a[0].equalsIgnoreCase("off")) {

            plugin.getScoreSettings().set(p, false);

            plugin.getScoreboardManager().remove(p);

            p.setScoreboard(
                    Bukkit.getScoreboardManager().getNewScoreboard()
            );

            p.sendMessage("§eScoreboard disabled");
            return true;
        }


        if (a[0].equalsIgnoreCase("on")) {

            plugin.getScoreSettings().set(p, true);

            // update ngay 1 lần cho đỡ phải đợi 5s
            plugin.getScoreboardManager().updateScoreboard(p);

            p.sendMessage("§aScoreboard enabled");
            return true;
        }

        p.sendMessage("§cUsage: /scoreboard on|off");
        return true;
    }
}
