package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SpeedFlyCommand implements CommandExecutor, TabCompleter {

    static final String FEATHER_NAME = "§6⚡ §eSpeed Wing §6⚡";

    private final MainPlugin plugin;

    public SpeedFlyCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        if (!p.hasPermission("multifeature.speedfly")) {
            p.sendMessage(Msg.SF_NO_PERM.get(p));
            return true;
        }

        SpeedFlyManager mgr = plugin.getSpeedFlyManager();

        if (args.length == 1 && args[0].equalsIgnoreCase("tool")) {
            if (hasFeather(p)) {
                p.sendMessage(Msg.SF_ALREADY_HAS_WING.get(p));
                return true;
            }
            p.getInventory().addItem(buildFeather());
            p.sendMessage(Msg.SF_WING_ADDED.get(p));
            return true;
        }

        if (args.length == 1) {
            int speed;
            try {
                speed = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                p.sendMessage(Msg.SF_USAGE.get(p));
                return true;
            }
            if (speed < 1 || speed > 10) {
                p.sendMessage(Msg.SF_SPEED_RANGE.get(p));
                return true;
            }
            mgr.setSpeed(p, speed);
            if (!mgr.isEnabled(p)) mgr.enable(p);
            p.sendMessage(Msg.SF_ON.fmt(p, "speed", speed));
            return true;
        }

        if (args.length == 0) {
            boolean on = mgr.toggle(p);
            if (on) {
                int speed = mgr.getSpeed(p);
                p.sendMessage(Msg.SF_ON.fmt(p, "speed", speed));
                p.sendMessage(Msg.SF_STATUS.fmt(p, "speed", speed));
            } else {
                p.sendMessage(Msg.SF_OFF.get(p));
            }
            return true;
        }

        p.sendMessage(Msg.SF_USAGE.get(p));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "tool").stream()
                    .filter(s -> s.startsWith(args[0]))
                    .toList();
        }
        return List.of();
    }

    static boolean hasFeather(Player p) {
        for (ItemStack slot : p.getInventory().getContents()) {
            if (isSpeedFeather(slot)) return true;
        }
        return false;
    }

    static boolean isSpeedFeather(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER) return false;
        if (!item.hasItemMeta()) return false;
        return FEATHER_NAME.equals(item.getItemMeta().getDisplayName());
    }

    static ItemStack buildFeather() {
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta m = feather.getItemMeta();
        m.setDisplayName(FEATHER_NAME);
        m.setLore(List.of(
                "§7Right-click §8— toggle SpeedFly on/off",
                "§8Use §7/speedfly <1-10> §8to change speed"
        ));
        m.setUnbreakable(true);
        feather.setItemMeta(m);
        return feather;
    }
}
