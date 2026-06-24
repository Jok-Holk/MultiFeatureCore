package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class DayLengthCommand implements CommandExecutor, TabCompleter {

    private final MainPlugin plugin;

    public DayLengthCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("multifeature.daylength")) {
            sender.sendMessage(Msg.DL_NO_PERM.get());
            return true;
        }

        DayLengthManager dlm = plugin.getDayLengthManager();

        // Lấy language của người gửi (chỉ hoạt động nếu là Player)
        Language lang = (sender instanceof Player p)
                ? LanguageManager.getLang(p)
                : Language.ENGLISH;

        if (args.length == 0) {
            double current = dlm.getCurrentMinutes();
            String statusTag = dlm.isVanilla()
                    ? Msg.DL_VANILLA_TAG.get(lang)
                    : Msg.DL_CUSTOM_TAG.get(lang);
            sender.sendMessage(Msg.DL_CURRENT.fmt(lang, "min", fmt(current), "status", statusTag));
            sender.sendMessage(Msg.DL_USAGE.get(lang));
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            dlm.setDayLength(20.0);
            sender.sendMessage(Msg.DL_RESET.get(lang));
            return true;
        }

        try {
            double minutes = Double.parseDouble(args[0]);
            if (minutes < 1.0 || minutes > 720.0) {
                sender.sendMessage(Msg.DL_RANGE.get(lang));
                return true;
            }
            dlm.setDayLength(minutes);
            if (minutes == 20.0) {
                sender.sendMessage(Msg.DL_SET_VANILLA.get(lang));
            } else {
                sender.sendMessage(Msg.DL_SET.fmt(lang, "min", fmt(minutes)));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(Msg.DL_INVALID.get(lang));
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

    private String fmt(double v) {
        return (v == Math.floor(v) && !Double.isInfinite(v))
                ? String.valueOf((int) v)
                : String.format("%.1f", v);
    }
}
