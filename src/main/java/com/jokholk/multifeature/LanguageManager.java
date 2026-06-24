package com.jokholk.multifeature;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {

    private static final Map<UUID, Language> langMap = new ConcurrentHashMap<>();
    private final MainPlugin plugin;

    public LanguageManager(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Static access (no reference needed in callers) ───

    public static Language getLang(Player p) {
        return langMap.getOrDefault(p.getUniqueId(), Language.ENGLISH);
    }

    public static Language getLang(UUID uid) {
        return langMap.getOrDefault(uid, Language.ENGLISH);
    }

    // ─── Load / save ───

    public void load(UUID uid) {
        File f = new File(plugin.getDataFolder(), "player_data/" + uid + ".yml");
        if (!f.exists()) {
            langMap.put(uid, Language.ENGLISH);
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        langMap.put(uid, Language.fromString(cfg.getString("language", "ENGLISH")));
    }

    public void save(Player p) {
        File f = new File(plugin.getDataFolder(), "player_data/" + p.getUniqueId() + ".yml");
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        cfg.set("language", getLang(p).name());
        try {
            cfg.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void set(Player p, Language lang) {
        langMap.put(p.getUniqueId(), lang);
        save(p);
    }

    public void unload(UUID uid) {
        langMap.remove(uid);
    }
}
