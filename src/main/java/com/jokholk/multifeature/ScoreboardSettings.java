package com.jokholk.multifeature;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class ScoreboardSettings {

    private final MainPlugin plugin;

    // ➤ CACHE RAM
    private final HashMap<UUID, Boolean> cache = new HashMap<>();

    public ScoreboardSettings(MainPlugin plugin) {
        this.plugin = plugin;
    }

    private File file(Player p) {

        File f = new File(plugin.getDataFolder(), "scoreboard");

        if (!f.exists())
            f.mkdirs();

        return new File(f, p.getUniqueId() + ".yml");
    }

    public boolean isEnabled(Player p) {

        UUID id = p.getUniqueId();

        // ➤ nếu đã có trong cache → trả luôn
        if (cache.containsKey(id))
            return cache.get(id);

        // ➤ chưa có → load từ file
        YamlConfiguration c =
                YamlConfiguration.loadConfiguration(file(p));

        boolean value = c.getBoolean("enabled", true);

        cache.put(id, value);

        return value;
    }

    public void set(Player p, boolean value) {

        UUID id = p.getUniqueId();

        cache.put(id, value);

        YamlConfiguration c =
                YamlConfiguration.loadConfiguration(file(p));

        c.set("enabled", value);

        try {
            c.save(file(p));
        } catch (IOException e) {
            plugin.getLogger().warning("Cannot save scoreboard setting for " + p.getName());
        }
    }

    // ➤ khi player quit
    public void unload(Player p) {
        cache.remove(p.getUniqueId());
    }
}
