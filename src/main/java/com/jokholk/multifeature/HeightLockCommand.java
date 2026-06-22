package com.jokholk.multifeature;

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
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!p.hasPermission("multifeature.heightlock")) {
            p.sendMessage("§cYou don't have permission to use height lock.");
            return true;
        }

        HeightLockManager hlm = plugin.getHeightLockManager();

        // /heightlock — toggle
        if (args.length == 0) {
            if (hlm.isLocked(p)) {
                hlm.unlock(p);
            } else if (hlm.hasLastY(p)) {
                // Bật lại tại Y cũ, không teleport
                hlm.reactivate(p);
            } else {
                // Lần đầu, lock tại Y hiện tại
                hlm.lock(p, p.getLocation().getY());
            }
            return true;
        }

        String arg = args[0];

        // /heightlock off
        if (arg.equalsIgnoreCase("off")) {
            if (!hlm.isLocked(p)) {
                p.sendMessage("§7[HeightLock] §eHeight lock is already OFF.");
            } else {
                hlm.unlock(p);
            }
            return true;
        }

        // /heightlock on
        if (arg.equalsIgnoreCase("on")) {
            if (hlm.isLocked(p)) {
                p.sendMessage("§7[HeightLock] §eAlready locked at Y=" + (int) hlm.getLockedY(p));
            } else if (hlm.hasLastY(p)) {
                hlm.reactivate(p);
            } else {
                hlm.lock(p, p.getLocation().getY());
            }
            return true;
        }

        // /heightlock <number>
        try {
            double y = Double.parseDouble(arg);
            int minY = p.getWorld().getMinHeight();
            int maxY = p.getWorld().getMaxHeight();

            if (y < minY || y > maxY) {
                p.sendMessage("§cY must be between " + minY + " and " + maxY + " in this world.");
                return true;
            }

            hlm.lock(p, y);

        } catch (NumberFormatException ex) {
            p.sendMessage("§cUsage: /heightlock [<y>|on|off]");
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
}
