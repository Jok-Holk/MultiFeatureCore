package com.jokholk.multifeature;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MeasureListener implements Listener {

    private final MainPlugin plugin;

    public MeasureListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        MeasureManager mm = plugin.getMeasureManager();

        if (!mm.isActive(p)) return;

        // Chỉ xử lý main hand để tránh fire 2 lần
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = e.getItem();
        if (!mm.isMeasureCompass(item)) return;

        // Cancel mọi interaction mặc định của compass
        e.setCancelled(true);

        Action action = e.getAction();

        // Left-click block → Point 1
        if (action == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
            mm.setPoint1(p, e.getClickedBlock().getLocation());
            return;
        }

        // Right-click block → Point 2 → tính toán
        if (action == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            if (!mm.hasPoint1(p)) {
                p.sendMessage("§c[Measure] Set Point 1 first (left-click a block).");
                return;
            }
            mm.calculate(p, e.getClickedBlock().getLocation());
            return;
        }

        // Click vào không khí — nhắc nhở
        if (action == Action.LEFT_CLICK_AIR) {
            p.sendMessage("§7[Measure] §fLeft-click §7on a §fblock §7to set §aPoint 1§7.");
        } else if (action == Action.RIGHT_CLICK_AIR) {
            p.sendMessage("§7[Measure] §fRight-click §7on a §fblock §7to set §aPoint 2§7.");
        }
    }

    // Drop compass → huỷ đo lường
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        MeasureManager mm = plugin.getMeasureManager();

        if (!mm.isActive(p)) return;
        if (!mm.isMeasureCompass(e.getItemDrop().getItemStack())) return;

        e.setCancelled(true);
        mm.cancel(p);
        p.sendMessage("§7[Measure] §cSession cancelled.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getMeasureManager().cancel(e.getPlayer());
    }
}
