package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HeightLockCommand implements CommandExecutor, TabCompleter {

    private final MainPlugin plugin;

    public HeightLockCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage(Msg.ONLY_PLAYERS.get());
            return true;
        }

        if (!p.hasPermission("multifeature.heightlock")) {
            p.sendMessage(Msg.HL_NO_PERM.get(p));
            return true;
        }

        HeightLockManager hlm = plugin.getHeightLockManager();

        if (args.length == 0) {
            if (hlm.isLocked(p)) {
                hlm.unlock(p);
            } else if (hlm.hasLastY(p)) {
                hlm.reactivate(p);
            } else {
                hlm.lock(p, p.getLocation().getY());
            }
            return true;
        }

        String arg = args[0];

        if (arg.equalsIgnoreCase("off")) {
            if (!hlm.isLocked(p)) {
                p.sendMessage(Msg.HL_ALREADY_OFF.get(p));
            } else {
                hlm.unlock(p);
            }
            return true;
        }

        if (arg.equalsIgnoreCase("on")) {
            if (hlm.isLocked(p)) {
                p.sendMessage(Msg.HL_ALREADY_ON.fmt(p, "y", formatY(hlm.getLockedY(p))));
            } else if (hlm.hasLastY(p)) {
                hlm.reactivate(p);
            } else {
                hlm.lock(p, p.getLocation().getY());
            }
            return true;
        }

        try {
            double y = Double.parseDouble(arg);
            int minY = p.getWorld().getMinHeight();
            int maxY = p.getWorld().getMaxHeight();

            if (y < minY || y > maxY) {
                p.sendMessage(Msg.HL_OUT_OF_RANGE.get(p));
                return true;
            }

            hlm.lock(p, y);

        } catch (NumberFormatException ex) {
            p.sendMessage(Msg.HL_USAGE.get(p));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("on", "off").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String formatY(double y) {
        return (y == Math.floor(y)) ? String.valueOf((int) y) : String.format("%.2f", y);
    }
}
