package com.jokholk.multifeature;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CheckpointManager {

    private final MainPlugin plugin;

    public CheckpointManager(MainPlugin plugin) {
        this.plugin = plugin;
    }

    private File getFile(Player p) {
        File folder = new File(plugin.getDataFolder(), "checkpoints");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return new File(folder, p.getUniqueId() + ".yml");
    }

    private YamlConfiguration getConfig(Player p) {
        return YamlConfiguration.loadConfiguration(getFile(p));
    }

    public void saveCheckpoint(Player p, String id, String name) {
        YamlConfiguration c = getConfig(p);
        Location l = p.getLocation();

        c.set(id + ".name", name);
        c.set(id + ".world", l.getWorld().getName());
        c.set(id + ".x", l.getX());
        c.set(id + ".y", l.getY());
        c.set(id + ".z", l.getZ());

        try {
            c.save(getFile(p));
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save checkpoint for " + p.getName());
        }
    }

    public Location loadCheckpoint(Player p, String id) {
        YamlConfiguration c = getConfig(p);

        if (!c.contains(id + ".world")) return null;

        String world = c.getString(id + ".world");
        double x = c.getDouble(id + ".x");
        double y = c.getDouble(id + ".y");
        double z = c.getDouble(id + ".z");

        return new Location(plugin.getServer().getWorld(world), x, y, z);
    }

    public String getName(Player p, String id) {
        return getConfig(p).getString(id + ".name", id);
    }

    public String findIdByName(Player p, String input) {

        // Chuẩn hóa đầu vào
        String normalizedInput = normalize(input);

        YamlConfiguration c = getConfig(p);

        for (String id : c.getKeys(false)) {

            // So khớp với ID gốc: checkpoint1
            if (id.equalsIgnoreCase(normalizedInput)) {
                return id;
            }

            // So khớp với tên hiển thị
            String displayName = getName(p, id);

            if (normalize(displayName).equals(normalizedInput)) {
                return id;
            }

            // So khớp từng từ lẻ (ví dụ gõ "home")
            for (String part : displayName.split(" ")) {
                if (normalize(part).equals(normalizedInput)) {
                    return id;
                }
            }
        }



        return null;
    }

    public void deleteCheckpoint(Player p, String id) {

        YamlConfiguration c = getConfig(p);

        c.set(id, null);

        try {
            c.save(getFile(p));
        } catch (IOException e) {
            plugin.getLogger().warning("Could not delete checkpoint");
        }
    }


    // ➤ HÀM QUAN TRỌNG NHẤT
    private String normalize(String s) {
        return s.toLowerCase()
                .replace(" ", "_")
                .replace("-", "_")
                .trim();
    }

}
