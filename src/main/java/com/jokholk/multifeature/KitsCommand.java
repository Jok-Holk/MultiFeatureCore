package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class KitsCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!p.hasPermission("multifeature.kits")) {
            p.sendMessage("§cYou don't have permission to use kits.");
            return true;
        }

        if (args.length == 1) {
            // Confirm / cancel pending kit
            if (args[0].equalsIgnoreCase("confirm")) {
                KitManager.confirmGive(p);
                return true;
            }
            if (args[0].equalsIgnoreCase("cancel")) {
                KitManager.cancelGive(p);
                return true;
            }

            // Direct kit give by name
            KitManager.Kit kit = KitManager.Kit.fromCmd(args[0]);
            if (kit != null) {
                boolean gave = KitManager.tryGive(p, kit);
                if (gave) {
                    p.sendMessage("§7──────────────────────────────");
                    p.sendMessage("§6  Kit applied: " + kit.displayName);
                    p.sendMessage("§7──────────────────────────────");
                }
                return true;
            }
            // Fall through to open GUI for unrecognized args
        }

        // /kits — open GUI
        p.openInventory(buildGui());
        return true;
    }

    static Inventory buildGui() {
        Inventory gui = Bukkit.createInventory(null, 9, KitManager.GUI_TITLE);
        for (KitManager.Kit kit : KitManager.Kit.values()) {
            gui.setItem(kit.slot, KitManager.buildIcon(kit));
        }
        return gui;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> options = Arrays.stream(KitManager.Kit.values())
                    .map(k -> k.cmdName)
                    .collect(Collectors.toList());
            options.add("confirm");
            options.add("cancel");
            return options.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
