package com.jokholk.multifeature;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MeasureCommand implements CommandExecutor, TabCompleter {

    private final MainPlugin plugin;

    public MeasureCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!p.hasPermission("multifeature.measure")) {
            p.sendMessage("§cYou don't have permission to use measure tools.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUsage: /measure <distance|center>");
            return true;
        }

        MeasureManager mm = plugin.getMeasureManager();

        // Huỷ session cũ nếu đang có
        if (mm.isActive(p)) {
            mm.cancel(p);
            p.sendMessage("§7[Measure] Previous session cancelled.");
        } else if (mm.hasCompass(p)) {
            // Có compass nhưng không có session (ví dụ: server reload) — dọn sạch trước
            mm.cancel(p);
        }

        switch (args[0].toLowerCase()) {
            case "distance" -> mm.start(p, MeasureManager.Mode.DISTANCE);
            case "center"   -> mm.start(p, MeasureManager.Mode.CENTER);
            default         -> p.sendMessage("§cUsage: /measure <distance|center>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("distance", "center").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
