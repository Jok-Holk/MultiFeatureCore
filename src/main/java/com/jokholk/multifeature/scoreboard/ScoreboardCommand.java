package com.jokholk.multifeature.scoreboard;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class ScoreboardCommand implements CommandExecutor {

    private final MainPlugin plugin;

    public ScoreboardCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {

        if (!(s instanceof Player p)) return true;

        if (a.length == 0) {
            p.sendMessage(Msg.SB_USAGE.get(p));
            return true;
        }

        if (a[0].equalsIgnoreCase("off")) {
            plugin.getScoreSettings().set(p, false);
            plugin.getScoreboardManager().remove(p);
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            p.sendMessage(Msg.SB_OFF.get(p));
            return true;
        }

        if (a[0].equalsIgnoreCase("on")) {
            plugin.getScoreSettings().set(p, true);
            plugin.getScoreboardManager().updateScoreboard(p);
            p.sendMessage(Msg.SB_ON.get(p));
            return true;
        }

        p.sendMessage(Msg.SB_USAGE.get(p));
        return true;
    }
}
