package com.jokholk.multifeature;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeightLockManager {

    // Player đang bị lock: UUID → Y value đang lock
    private final Map<UUID, Double> activeLocks = new HashMap<>();

    // Y value lần cuối được set (dùng để toggle lại mà không cần nhập lại số)
    private final Map<UUID, Double> lastY = new HashMap<>();

    public boolean isLocked(Player player) {
        return activeLocks.containsKey(player.getUniqueId());
    }

    public double getLockedY(Player player) {
        return activeLocks.getOrDefault(player.getUniqueId(), player.getLocation().getY());
    }

    public boolean hasLastY(Player player) {
        return lastY.containsKey(player.getUniqueId());
    }

    public double getLastY(Player player) {
        return lastY.get(player.getUniqueId());
    }

    // Lock tại Y và teleport player đến đó
    public void lock(Player player, double y) {
        UUID id = player.getUniqueId();
        activeLocks.put(id, y);
        lastY.put(id, y);

        // Teleport đến Y được chỉ định, giữ nguyên X, Z, hướng nhìn
        var loc = player.getLocation().clone();
        loc.setY(y);
        player.teleport(loc);

        player.sendMessage("§7[HeightLock] §aLocked at Y=" + formatY(y) + " — elevation is frozen.");
    }

    // Lock lại tại Y cũ (không teleport)
    public void reactivate(Player player) {
        double y = lastY.get(player.getUniqueId());
        activeLocks.put(player.getUniqueId(), y);
        player.sendMessage("§7[HeightLock] §aReactivated at Y=" + formatY(y));
    }

    public void unlock(Player player) {
        activeLocks.remove(player.getUniqueId());
        player.sendMessage("§7[HeightLock] §cUnlocked.");
    }

    // Gọi khi player quit — xóa hoàn toàn state
    public void unload(Player player) {
        UUID id = player.getUniqueId();
        activeLocks.remove(id);
        lastY.remove(id);
    }

    private String formatY(double y) {
        // Hiển thị không có .0 nếu là số nguyên
        return (y == Math.floor(y)) ? String.valueOf((int) y) : String.format("%.2f", y);
    }
}
