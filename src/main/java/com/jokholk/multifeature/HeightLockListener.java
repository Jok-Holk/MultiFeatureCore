package com.jokholk.multifeature;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HeightLockListener implements Listener {

    private final MainPlugin plugin;

    public HeightLockListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        HeightLockManager hlm = plugin.getHeightLockManager();

        if (!hlm.isLocked(p)) return;

        // Bỏ qua khi player đang là spectator thật (không áp dụng lock)
        if (p.getGameMode() == GameMode.SPECTATOR) return;

        Location to = e.getTo();
        if (to == null) return;

        double lockedY = hlm.getLockedY(p);

        // Chỉ correct khi lệch quá 0.05 để tránh jitter ở floating point
        if (Math.abs(to.getY() - lockedY) < 0.05) return;

        Location corrected = to.clone();
        corrected.setY(lockedY);
        e.setTo(corrected);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getHeightLockManager().unload(e.getPlayer());
    }
}
