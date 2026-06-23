package com.jokholk.multifeature;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpeedFlyManager {

    // 0.1f per level: 1=vanilla, 10=max (1.0f Bukkit cap)
    private static final float[] SPEED = { 0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f };

    private final Map<UUID, Boolean> enabled          = new HashMap<>();
    private final Map<UUID, Integer> speeds           = new HashMap<>();
    // Lưu trạng thái bay ban đầu để restore khi tắt
    private final Map<UUID, Boolean> originalAllowFly = new HashMap<>();

    public boolean isEnabled(Player p) {
        return enabled.getOrDefault(p.getUniqueId(), false);
    }

    public int getSpeed(Player p) {
        return speeds.getOrDefault(p.getUniqueId(), 1);
    }

    public void setSpeed(Player p, int speed) {
        speeds.put(p.getUniqueId(), Math.max(1, Math.min(10, speed)));
        if (isEnabled(p)) p.setFlySpeed(SPEED[getSpeed(p)]);
    }

    // Toggle: trả về trạng thái mới (true = bật)
    public boolean toggle(Player p) {
        if (isEnabled(p)) { disable(p); return false; }
        else               { enable(p);  return true;  }
    }

    public void enable(Player p) {
        originalAllowFly.put(p.getUniqueId(), p.getAllowFlight());
        enabled.put(p.getUniqueId(), true);
        p.setAllowFlight(true);
        p.setFlying(true);
        p.setFlySpeed(SPEED[getSpeed(p)]);
    }

    public void disable(Player p) {
        enabled.put(p.getUniqueId(), false);
        p.setFlySpeed(0.1f);
        boolean hadFlight = originalAllowFly.getOrDefault(p.getUniqueId(), false);
        if (!hadFlight) {
            p.setFlying(false);
            p.setAllowFlight(false);
        }
        // Nếu ban đầu đã có bay (CREATIVE, SPECTATOR, hoặc GUEST được cấp quyền bay) → giữ nguyên
    }

    // Gọi khi player quit hoặc đổi gamemode — dọn data mà không touch trạng thái server
    public void cleanup(Player p) {
        UUID id = p.getUniqueId();
        enabled.remove(id);
        speeds.remove(id);
        originalAllowFly.remove(id);
    }

    // Reapply sau khi respawn (state bị reset)
    public void reapplyAfterRespawn(Player p) {
        if (!isEnabled(p)) return;
        p.setAllowFlight(true);
        p.setFlying(true);
        p.setFlySpeed(SPEED[getSpeed(p)]);
    }

    public static float getSpeedFloat(int level) {
        return SPEED[Math.max(1, Math.min(10, level))];
    }
}
