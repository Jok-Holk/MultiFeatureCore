package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeightLockManager {

    private final Map<UUID, Double> activeLocks = new HashMap<>();
    private final Map<UUID, Double> lastY       = new HashMap<>();

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

    public void lock(Player player, double y) {
        UUID id = player.getUniqueId();
        activeLocks.put(id, y);
        lastY.put(id, y);

        var loc = player.getLocation().clone();
        loc.setY(y);
        player.teleport(loc);

        player.sendMessage(Msg.HL_LOCKED.fmt(player, "y", formatY(y)));
    }

    public void reactivate(Player player) {
        double y = lastY.get(player.getUniqueId());
        activeLocks.put(player.getUniqueId(), y);
        player.sendMessage(Msg.HL_REACTIVATED.fmt(player, "y", formatY(y)));
    }

    public void unlock(Player player) {
        activeLocks.remove(player.getUniqueId());
        player.sendMessage(Msg.HL_UNLOCKED.get(player));
    }

    public void unload(Player player) {
        UUID id = player.getUniqueId();
        activeLocks.remove(id);
        lastY.remove(id);
    }

    private String formatY(double y) {
        return (y == Math.floor(y)) ? String.valueOf((int) y) : String.format("%.2f", y);
    }
}
