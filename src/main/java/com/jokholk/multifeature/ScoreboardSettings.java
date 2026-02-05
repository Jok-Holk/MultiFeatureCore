package com.jokholk.multifeature;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class ScoreboardSettings {

    private final MainPlugin plugin;

    public ScoreboardSettings(MainPlugin plugin) {
        this.plugin = plugin;
    }

    private File file(Player p) {
        File f = new File(plugin.getDataFolder(), "scoreboard");
        if (!f.exists()) f.mkdirs();

        return new File(f, p.getUniqueId() + ".yml");
    }

    public boolean isEnabled(Player p) {

        YamlConfiguration c =
                YamlConfiguration.loadConfiguration(file(p));

        return c.getBoolean("enabled", true);
    }

    public void set(Player p, boolean value) {

        YamlConfiguration c =
                YamlConfiguration.loadConfiguration(file(p));

        c.set("enabled", value);

        try {
            c.save(file(p));
        } catch (IOException e) {}
    }
}
