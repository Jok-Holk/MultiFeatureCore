package com.jokholk.multifeature;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SpeedFlyListener implements Listener {

    private final MainPlugin plugin;

    public SpeedFlyListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // Chuột phải feather → toggle on/off
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (!SpeedFlyCommand.isSpeedFeather(item)) return;

        e.setCancelled(true);

        Player p = e.getPlayer();
        if (!p.hasPermission("multifeature.speedfly")) return;

        SpeedFlyManager mgr = plugin.getSpeedFlyManager();
        boolean on = mgr.toggle(p);
        if (on) {
            int speed = mgr.getSpeed(p);
            p.sendMessage("§aSpeedFly §2ON §7— speed: §e" + speed + "§7x");
        } else {
            p.sendMessage("§cSpeedFly §4OFF");
        }
    }

    // Không cho thả feather ra khỏi tay
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (SpeedFlyCommand.isSpeedFeather(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    // Reapply sau khi respawn (state bị reset bởi server)
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        plugin.getSpeedFlyManager().reapplyAfterRespawn(e.getPlayer());
    }

    // Tắt speedfly khi đổi gamemode để tránh conflict với vanilla flight
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        SpeedFlyManager mgr = plugin.getSpeedFlyManager();
        Player p = e.getPlayer();
        if (!mgr.isEnabled(p)) return;

        GameMode next = e.getNewGameMode();
        // CREATIVE/SPECTATOR có flight tự nhiên — tắt speedfly để tránh double-state
        if (next == GameMode.CREATIVE || next == GameMode.SPECTATOR) {
            mgr.disable(p);
            mgr.cleanup(p);
        }
    }

    // Cleanup khi quit để tránh trạng thái bay persist vào phiên sau
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        SpeedFlyManager mgr = plugin.getSpeedFlyManager();
        Player p = e.getPlayer();
        if (mgr.isEnabled(p)) mgr.disable(p);
        mgr.cleanup(p);
    }
}
