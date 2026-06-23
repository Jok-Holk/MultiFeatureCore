package com.jokholk.multifeature;

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
            p.sendMessage("§cYou don't have permission to use SpeedFly.");
            return true;
        }

        SpeedFlyManager mgr = plugin.getSpeedFlyManager();

        // /speedfly tool
        if (args.length == 1 && args[0].equalsIgnoreCase("tool")) {
            if (hasFeather(p)) {
                p.sendMessage("§eYou already have a Speed Wing in your inventory.");
                return true;
            }
            p.getInventory().addItem(buildFeather());
            p.sendMessage("§aSpeed Wing added. §7Right-click to toggle SpeedFly.");
            return true;
        }

        // /speedfly <1-5>
        if (args.length == 1) {
            int speed;
            try {
                speed = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sendUsage(p);
                return true;
            }
            if (speed < 1 || speed > 5) {
                p.sendMessage("§cSpeed must be between §e1 §cand §e5§c.");
                return true;
            }
            mgr.setSpeed(p, speed);
            if (!mgr.isEnabled(p)) mgr.enable(p);
            p.sendMessage("§aSpeedFly §2ON §7— speed set to §e" + speed + "§7x  §8(0." + speed + " fly speed)");
            return true;
        }

        // /speedfly — toggle
        if (args.length == 0) {
            boolean on = mgr.toggle(p);
            if (on) {
                int speed = mgr.getSpeed(p);
                p.sendMessage("§aSpeedFly §2ON §7— current speed: §e" + speed + "§7x");
                p.sendMessage("§7Change speed: §f/speedfly <1-5>  §8— examples: 1 (default)  2  3  4  5");
            } else {
                p.sendMessage("§cSpeedFly §4OFF");
            }
            return true;
        }

        sendUsage(p);
        return true;
    }

    private void sendUsage(Player p) {
        p.sendMessage("§cUsage: §f/speedfly §7[1-5 | tool]");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "3", "4", "5", "tool").stream()
                    .filter(s -> s.startsWith(args[0]))
                    .toList();
        }
        return List.of();
    }

    // ── Feather item helpers ──

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
                "§8Use §7/speedfly <1-5> §8to change speed"
        ));
        m.setUnbreakable(true);
        feather.setItemMeta(m);
        return feather;
    }
}
