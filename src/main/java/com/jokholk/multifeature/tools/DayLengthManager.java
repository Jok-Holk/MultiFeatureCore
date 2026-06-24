package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;

public class DayLengthManager {

    private final MainPlugin plugin;

    private double currentMinutes = 20.0;
    private BukkitTask cycleTask;
    private double accumulator = 0.0;

    // 24000 game ticks = 1 full Minecraft day (vanilla default)
    private static final int TICKS_PER_DAY = 24000;
    private static final double DEFAULT_MINUTES = 20.0;

    public DayLengthManager(MainPlugin plugin) {
        this.plugin = plugin;
        currentMinutes = loadSaved();

        // Khôi phục cài đặt đã lưu nếu khác default
        if (currentMinutes != DEFAULT_MINUTES) {
            setVanillaCycle(false);
            startCustomCycle(currentMinutes);
        }
    }

    public double getCurrentMinutes() {
        return currentMinutes;
    }

    public boolean isVanilla() {
        return currentMinutes == DEFAULT_MINUTES;
    }

    public void setDayLength(double minutes) {
        currentMinutes = minutes;
        stopCustomCycle();
        save(minutes);

        if (minutes == DEFAULT_MINUTES) {
            setVanillaCycle(true);
        } else {
            setVanillaCycle(false);
            startCustomCycle(minutes);
        }
    }

    // Gọi khi plugin disable
    public void shutdown() {
        stopCustomCycle();
        setVanillaCycle(true);
    }

    // ────────────────────────────────────────────────
    //  Internal
    // ────────────────────────────────────────────────

    private void startCustomCycle(double minutes) {
        accumulator = 0.0;

        // Số game ticks cần advance mỗi server tick để 1 day = N phút thực
        // 1 day = 24000 game ticks = minutes * 60s * 20 ticks/s
        double rate = (double) TICKS_PER_DAY / (minutes * 60.0 * 20.0);

        cycleTask = new BukkitRunnable() {
            @Override
            public void run() {
                accumulator += rate;
                if (accumulator >= 1.0) {
                    long advance = (long) accumulator;
                    accumulator -= advance;
                    for (World world : Bukkit.getWorlds()) {
                        if (world.getEnvironment() == World.Environment.NORMAL) {
                            world.setFullTime(world.getFullTime() + advance);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void stopCustomCycle() {
        if (cycleTask != null) {
            cycleTask.cancel();
            cycleTask = null;
        }
    }

    // Dùng GameRules.ADVANCE_TIME thay cho deprecated GameRule.DO_DAYLIGHT_CYCLE
    private void setVanillaCycle(boolean enabled) {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setGameRule(GameRules.ADVANCE_TIME, enabled);
            }
        }
    }

    // ────────────────────────────────────────────────
    //  Persistence
    // ────────────────────────────────────────────────

    private void save(double minutes) {
        File file = new File(plugin.getDataFolder(), "daylength.yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("minutes", minutes);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save day length setting.");
        }
    }

    private double loadSaved() {
        File file = new File(plugin.getDataFolder(), "daylength.yml");
        if (!file.exists()) return DEFAULT_MINUTES;
        return YamlConfiguration.loadConfiguration(file).getDouble("minutes", DEFAULT_MINUTES);
    }
}
