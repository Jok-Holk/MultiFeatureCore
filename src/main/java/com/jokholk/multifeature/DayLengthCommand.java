package com.jokholk.multifeature;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class DayLengthCommand implements CommandExecutor, TabCompleter {

    private final MainPlugin plugin;

    public DayLengthCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("multifeature.daylength")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        DayLengthManager dlm = plugin.getDayLengthManager();

        // /daylength — hiển thị thông số hiện tại
        if (args.length == 0) {
            double current = dlm.getCurrentMinutes();
            String status = dlm.isVanilla() ? "§7(vanilla default)" : "§e(custom)";
            sender.sendMessage("§6[DayLength] §7Current day length: §e" + fmt(current) + " minutes " + status);
            sender.sendMessage("§7Usage: §f/daylength <minutes> §7(1–720) §8| §f/daylength reset");
            return true;
        }

        // /daylength reset
        if (args[0].equalsIgnoreCase("reset")) {
            dlm.setDayLength(20.0);
            sender.sendMessage("§6[DayLength] §aReset to vanilla default §7(20 minutes per day).");
            return true;
        }

        // /daylength <number>
        try {
            double minutes = Double.parseDouble(args[0]);
            if (minutes < 1.0 || minutes > 720.0) {
                sender.sendMessage("§cDay length must be between §e1 §cand §e720 §cminutes.");
                return true;
            }
            dlm.setDayLength(minutes);
            if (minutes == 20.0) {
                sender.sendMessage("§6[DayLength] §aSet to vanilla default §7(20 minutes).");
            } else {
                sender.sendMessage("§6[DayLength] §aDay length set to §e" + fmt(minutes) + " minutes§a per full cycle.");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid number. Usage: §f/daylength <minutes> §cor §f/daylength reset");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("20", "30", "45", "60", "reset");
        }
        return List.of();
    }

    // Format số đẹp: bỏ .0 nếu là số nguyên
    private String fmt(double v) {
        return (v == Math.floor(v) && !Double.isInfinite(v))
                ? String.valueOf((int) v)
                : String.format("%.1f", v);
    }
}
