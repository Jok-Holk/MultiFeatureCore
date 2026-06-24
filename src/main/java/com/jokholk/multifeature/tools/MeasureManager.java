package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

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

    public static final String COMPASS_NAME = "§6✦ Measure Tool ✦";

    private final Map<UUID, Mode>     activeModes = new HashMap<>();
    private final Map<UUID, Location> point1      = new HashMap<>();

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

    public void start(Player player, Mode mode) {
        UUID id = player.getUniqueId();
        activeModes.put(id, mode);
        point1.remove(id);

        giveCompass(player, mode, false);

        if (mode == Mode.DISTANCE) {
            player.sendMessage(Msg.MEASURE_START_DISTANCE.get(player));
        } else {
            player.sendMessage(Msg.MEASURE_START_CENTER.get(player));
        }
    }

    public void setPoint1(Player player, Location loc) {
        point1.put(player.getUniqueId(), loc.clone());
        refreshCompassLore(player, true);
        player.sendMessage(Msg.MEASURE_P1_SET.fmt(player, "coords", formatCoords(loc)));
    }

    public void calculate(Player player, Location loc2) {
        Location loc1 = point1.get(player.getUniqueId());
        Mode mode     = activeModes.get(player.getUniqueId());

        if (loc1 == null) {
            player.sendMessage(Msg.MEASURE_P1_FIRST.get(player));
            return;
        }

        if (!loc1.getWorld().equals(loc2.getWorld())) {
            player.sendMessage(Msg.MEASURE_DIFF_WORLD.get(player));
            cancel(player);
            return;
        }

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

        Language lang = LanguageManager.getLang(player);

        player.sendMessage("§7§m──────────────────────────────");
        player.sendMessage(Msg.MEASURE_DIST_TITLE.get(lang));
        player.sendMessage(Msg.MEASURE_POINT1.fmt(lang, "coords", formatCoords(a)));
        player.sendMessage(Msg.MEASURE_POINT2.fmt(lang, "coords", formatCoords(b)));
        player.sendMessage("§7§m──────────────────────");
        player.sendMessage(Msg.MEASURE_WIDTH.fmt(lang,   "val", blockUnit(dx, lang)));
        player.sendMessage(Msg.MEASURE_HEIGHT_Y.fmt(lang,"val", blockUnit(dy, lang)));
        player.sendMessage(Msg.MEASURE_LENGTH.fmt(lang,  "val", blockUnit(dz, lang)));
        player.sendMessage(Msg.MEASURE_FLAT_DIST.fmt(lang,  "val", String.format("%.2f", dist2D)));
        player.sendMessage(Msg.MEASURE_TOTAL_DIST.fmt(lang, "val", String.format("%.2f", dist3D)));
        player.sendMessage("§7§m──────────────────────────────");
    }

    private String blockUnit(int n, Language lang) {
        if (lang == Language.VIETNAMESE) {
            return Msg.MEASURE_BLOCK_UNIT.fmt(lang, "n", n);
        }
        return (n == 1)
                ? Msg.MEASURE_BLOCK_UNIT.fmt(lang, "n", n)
                : Msg.MEASURE_BLOCK_UNIT_PLURAL.fmt(lang, "n", n);
    }

    // ────────────────────────────────────────────────
    //  Center output
    // ────────────────────────────────────────────────

    private void outputCenter(Player player, Location a, Location b) {
        int x1 = a.getBlockX(), z1 = a.getBlockZ();
        int x2 = b.getBlockX(), z2 = b.getBlockZ();

        double cx = (x1 + x2 + 1) / 2.0;
        double cz = (z1 + z2 + 1) / 2.0;

        int dx = Math.abs(x2 - x1) + 1;
        int dz = Math.abs(z2 - z1) + 1;

        boolean exactX = (dx % 2 == 1);
        boolean exactZ = (dz % 2 == 1);

        Language lang = LanguageManager.getLang(player);

        String centerNote;
        if      ( exactX &&  exactZ) centerNote = Msg.MEASURE_EXACT_ODD.get(lang);
        else if (!exactX && !exactZ) centerNote = Msg.MEASURE_EVEN_4.get(lang);
        else if (!exactX)            centerNote = Msg.MEASURE_EVEN_X.get(lang);
        else                         centerNote = Msg.MEASURE_EVEN_Z.get(lang);

        String tpXStr = formatTpCoord(cx);
        String tpZStr = formatTpCoord(cz);
        String tpCmd  = "/tp " + tpXStr + " ~ " + tpZStr;

        player.sendMessage("§7§m──────────────────────────────");
        player.sendMessage(Msg.MEASURE_CENTER_TITLE.get(lang));
        player.sendMessage(Msg.MEASURE_POINT1.fmt(lang, "coords", formatCoords(a)));
        player.sendMessage(Msg.MEASURE_POINT2.fmt(lang, "coords", formatCoords(b)));
        player.sendMessage("§7§m──────────────────────");
        player.sendMessage(Msg.MEASURE_AREA.fmt(lang, "dx", dx, "dz", dz));
        player.sendMessage(Msg.MEASURE_CENTER_XZ.fmt(lang, "cx", tpXStr, "cz", tpZStr));
        player.sendMessage("§f  " + centerNote);
        player.sendMessage("§7§m──────────────────────────────");

        Component tpButton = Component.text()
                .append(Component.text("  "))
                .append(Component.text(Msg.MEASURE_TP_LABEL.get(lang))
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand(tpCmd))
                        .hoverEvent(HoverEvent.showText(
                                Component.text(Msg.MEASURE_TP_HOVER_LINE1.get(lang) + "\n")
                                        .color(NamedTextColor.GRAY)
                                        .append(Component.text(
                                                Msg.MEASURE_TP_HOVER_LINE2.fmt(lang, "cx", tpXStr, "cz", tpZStr))
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

    public boolean hasCompass(Player player) {
        for (ItemStack slot : player.getInventory().getContents()) {
            if (isMeasureCompass(slot)) return true;
        }
        return false;
    }

    private void giveCompass(Player player, Mode mode, boolean p1Set) {
        if (hasCompass(player)) {
            refreshCompassLore(player, p1Set);
        } else {
            player.getInventory().addItem(buildCompass(player, mode, p1Set));
        }
    }

    private void refreshCompassLore(Player player, boolean p1Set) {
        Mode mode = activeModes.get(player.getUniqueId());
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (!isMeasureCompass(item)) continue;
            item.setItemMeta(buildCompass(player, mode, p1Set).getItemMeta());
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

    private ItemStack buildCompass(Player player, Mode mode, boolean p1Set) {
        Language lang = LanguageManager.getLang(player);
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName(COMPASS_NAME);

        String modeLine = (mode == Mode.DISTANCE)
                ? Msg.MEASURE_COMPASS_MODE_DISTANCE.get(lang)
                : Msg.MEASURE_COMPASS_MODE_CENTER.get(lang);
        String p1Line = Msg.MEASURE_COMPASS_P1.get(lang) + (p1Set ? "§a✔" : "§8○");
        String p2Line = Msg.MEASURE_COMPASS_P2.get(lang);

        meta.setLore(List.of(modeLine, "", p1Line, p2Line,
                "", Msg.MEASURE_COMPASS_LORE_CANCEL.get(lang)));
        compass.setItemMeta(meta);
        return compass;
    }

    // ────────────────────────────────────────────────

    private String formatCoords(Location loc) {
        return "(" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")";
    }

    private String formatTpCoord(double val) {
        return (val == Math.floor(val))
                ? String.valueOf((int) val)
                : String.format("%.1f", val);
    }
}
