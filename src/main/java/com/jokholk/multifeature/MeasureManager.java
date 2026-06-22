package com.jokholk.multifeature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MeasureManager {

    public enum Mode { DISTANCE, CENTER }

    // Display name đặc biệt để nhận dạng compass đo lường
    public static final String COMPASS_NAME = "§6✦ Measure Tool ✦";

    private final Map<UUID, Mode>     activeModes = new HashMap<>();
    private final Map<UUID, Location> point1      = new HashMap<>();

    // ────────────────────────────────────────────────
    //  State checks
    // ────────────────────────────────────────────────

    public boolean isActive(Player player) {
        return activeModes.containsKey(player.getUniqueId());
    }

    public boolean hasPoint1(Player player) {
        return point1.containsKey(player.getUniqueId());
    }

    public Mode getMode(Player player) {
        return activeModes.get(player.getUniqueId());
    }

    // ────────────────────────────────────────────────
    //  Session lifecycle
    // ────────────────────────────────────────────────

    public void start(Player player, Mode mode) {
        UUID id = player.getUniqueId();
        activeModes.put(id, mode);
        point1.remove(id);

        giveCompass(player, mode, false);

        String modeColor = (mode == Mode.DISTANCE) ? "§b" : "§d";
        String modeName  = (mode == Mode.DISTANCE) ? "Distance" : "Center";

        player.sendMessage("§7[Measure] " + modeColor + modeName
                + " §7mode started — §fleft-click §7a block for §aPoint 1§7.");
    }

    public void setPoint1(Player player, Location loc) {
        point1.put(player.getUniqueId(), loc.clone());
        refreshCompassLore(player, true);

        player.sendMessage("§7[Measure] §aPoint 1 §7set at §f" + formatCoords(loc)
                + " §7— §fright-click §7a block for §aPoint 2§7.");
    }

    public void calculate(Player player, Location loc2) {
        Location loc1 = point1.get(player.getUniqueId());
        Mode mode     = activeModes.get(player.getUniqueId());

        if (loc1 == null) {
            player.sendMessage("§c[Measure] Set Point 1 first (left-click a block).");
            return;
        }

        if (!loc1.getWorld().equals(loc2.getWorld())) {
            player.sendMessage("§c[Measure] Both points must be in the same world.");
            cancel(player);
            return;
        }

        // Xóa session trước khi output (compass bị thu hồi ở đây)
        cancel(player);

        if (mode == Mode.DISTANCE) outputDistance(player, loc1, loc2);
        else                       outputCenter(player, loc1, loc2);
    }

    public void cancel(Player player) {
        UUID id = player.getUniqueId();
        activeModes.remove(id);
        point1.remove(id);
        removeCompass(player);
    }

    // ────────────────────────────────────────────────
    //  Distance output
    // ────────────────────────────────────────────────

    private void outputDistance(Player player, Location a, Location b) {
        int dx = Math.abs(b.getBlockX() - a.getBlockX());
        int dy = Math.abs(b.getBlockY() - a.getBlockY());
        int dz = Math.abs(b.getBlockZ() - a.getBlockZ());

        double dist3D = Math.sqrt((double)(dx*dx + dy*dy + dz*dz));
        double dist2D = Math.sqrt((double)(dx*dx + dz*dz));

        player.sendMessage("§7§m──────────────────────────────");
        player.sendMessage("§6§l  MEASURE — DISTANCE");
        player.sendMessage("§7  Point 1: §f" + formatCoords(a));
        player.sendMessage("§7  Point 2: §f" + formatCoords(b));
        player.sendMessage("§7§m──────────────────────");
        player.sendMessage("§f  Width  (X): §e" + dx + " block" + (dx != 1 ? "s" : ""));
        player.sendMessage("§f  Height (Y): §e" + dy + " block" + (dy != 1 ? "s" : ""));
        player.sendMessage("§f  Length (Z): §e" + dz + " block" + (dz != 1 ? "s" : ""));
        player.sendMessage("§f  Flat distance  (2D): §a" + String.format("%.2f", dist2D));
        player.sendMessage("§f  Total distance (3D): §a" + String.format("%.2f", dist3D));
        player.sendMessage("§7§m──────────────────────────────");
    }

    // ────────────────────────────────────────────────
    //  Center output
    // ────────────────────────────────────────────────

    private void outputCenter(Player player, Location a, Location b) {
        int x1 = a.getBlockX(), z1 = a.getBlockZ();
        int x2 = b.getBlockX(), z2 = b.getBlockZ();

        // Công thức: center của vùng bao gồm cả 2 block đầu cuối
        // (x1 + x2 + 1) / 2.0 — đúng cho cả tọa độ âm
        double cx = (x1 + x2 + 1) / 2.0;
        double cz = (z1 + z2 + 1) / 2.0;

        // Số block theo mỗi chiều (inclusive)
        int dx = Math.abs(x2 - x1) + 1;
        int dz = Math.abs(z2 - z1) + 1;

        // Odd count → center đúng 1 block; Even count → center giữa 2 block
        boolean exactX = (dx % 2 == 1);
        boolean exactZ = (dz % 2 == 1);

        String centerNote;
        if      ( exactX &&  exactZ) centerNote = "§aExact center (odd × odd area)";
        else if (!exactX && !exactZ) centerNote = "§eCenter between 4 blocks (even × even)";
        else if (!exactX)            centerNote = "§eCenter between 2 blocks on X axis";
        else                         centerNote = "§eCenter between 2 blocks on Z axis";

        // Format tọa độ TP: dùng 1 decimal nếu là .5, không decimal nếu nguyên
        String tpXStr = formatTpCoord(cx);
        String tpZStr = formatTpCoord(cz);
        String tpCmd  = "/tp " + tpXStr + " ~ " + tpZStr;

        player.sendMessage("§7§m──────────────────────────────");
        player.sendMessage("§d§l  MEASURE — CENTER");
        player.sendMessage("§7  Point 1: §f" + formatCoords(a));
        player.sendMessage("§7  Point 2: §f" + formatCoords(b));
        player.sendMessage("§7§m──────────────────────");
        player.sendMessage("§f  Area: §e" + dx + " × " + dz + " blocks");
        player.sendMessage("§f  Center X: §e" + tpXStr + "  §fZ: §e" + tpZStr);
        player.sendMessage("§f  " + centerNote);
        player.sendMessage("§7§m──────────────────────────────");

        // Dòng clickable để TP đến center, giữ nguyên Y hiện tại
        Component tpButton = Component.text()
                .append(Component.text("  "))
                .append(Component.text("[✦ Teleport to Center]")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand(tpCmd))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Click to teleport to:\n")
                                        .color(NamedTextColor.GRAY)
                                        .append(Component.text("X=" + tpXStr + "  Z=" + tpZStr + "  Y=<current>")
                                                .color(NamedTextColor.WHITE))
                        )))
                .build();

        player.sendMessage(tpButton);
        player.sendMessage("§7§m──────────────────────────────");
    }

    // ────────────────────────────────────────────────
    //  Compass item helpers
    // ────────────────────────────────────────────────

    public boolean isMeasureCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!item.hasItemMeta()) return false;
        return COMPASS_NAME.equals(item.getItemMeta().getDisplayName());
    }

    private void giveCompass(Player player, Mode mode, boolean p1Set) {
        player.getInventory().addItem(buildCompass(mode, p1Set));
    }

    private void refreshCompassLore(Player player, boolean p1Set) {
        Mode mode = activeModes.get(player.getUniqueId());
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (!isMeasureCompass(item)) continue;
            item.setItemMeta(buildCompass(mode, p1Set).getItemMeta());
            player.getInventory().setItem(i, item);
            return;
        }
    }

    private void removeCompass(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (isMeasureCompass(player.getInventory().getItem(i))) {
                player.getInventory().setItem(i, null);
                return;
            }
        }
    }

    private ItemStack buildCompass(Mode mode, boolean p1Set) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(COMPASS_NAME);

        String modeLine = (mode == Mode.DISTANCE) ? "§bMode: Distance" : "§dMode: Center";
        String p1Line   = "§fLeft-click  §7→ Point 1 " + (p1Set ? "§a✔" : "§8○");
        String p2Line   = "§fRight-click §7→ Point 2 §8○";

        meta.setLore(List.of(modeLine, "", p1Line, p2Line,
                "", "§7Drop compass to cancel."));
        compass.setItemMeta(meta);
        return compass;
    }

    // ────────────────────────────────────────────────
    //  Format helpers
    // ────────────────────────────────────────────────

    private String formatCoords(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }

    private String formatTpCoord(double val) {
        // 100.5 → "100.5"  |  100.0 → "100"
        return (val == Math.floor(val))
                ? String.valueOf((int) val)
                : String.format("%.1f", val);
    }
}
