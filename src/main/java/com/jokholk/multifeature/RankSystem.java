package com.jokholk.multifeature;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RankSystem {
    private static HashMap<UUID, String> playerRanks = new HashMap<>();
    private static HashMap<UUID, PermissionAttachment> playerPermissions = new HashMap<>();
    private MainPlugin plugin;

    public RankSystem(MainPlugin plugin) {
        this.plugin = plugin;
        loadPlayerRanks();  // Load saved player ranks on plugin startup
    }

    // Check if a rank is valid
    public boolean isValidRank(String rank) {
        return plugin.getConfig().getConfigurationSection("rank.permissions").contains(rank);
    }

    // Get a list of all valid ranks
    public List<String> getRanks() {
        return new ArrayList<>(plugin.getConfig().getConfigurationSection("rank.permissions").getKeys(false));
    }

    // Load ranks from the player's data files
    public void loadPlayerRanks() {
        File dataFolder = new File(plugin.getDataFolder(), "player_data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
                UUID playerUUID = UUID.fromString(file.getName().replace(".yml", ""));
                String rank = playerConfig.getString("rank", "GUEST");
                playerRanks.put(playerUUID, rank);
            }
        }
    }

    // Save the player's rank to their data file
    public void savePlayerRank(Player player) {
        File playerFile = new File(plugin.getDataFolder(), "player_data/" + player.getUniqueId() + ".yml");
        YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);

        // Save both UUID and username
        playerConfig.set("uuid", player.getUniqueId().toString());
        playerConfig.set("name", player.getName()); // Add username
        playerConfig.set("rank", getRank(player));

        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRank(Player player, String rank) {
        playerRanks.put(player.getUniqueId(), rank);
        updatePermissions(player);
        savePlayerRank(player);  // Save the rank after changing it

        plugin.setDefaultGamemode(player);
    }

    public String getRank(Player player) {
        return playerRanks.getOrDefault(player.getUniqueId(), "GUEST");
    }

    // Add color getter
    public String getRankColor(Player player) {
        String rank = getRank(player);
        return plugin.getConfig().getString("rank.colors." + rank, "§7");
    }

    public void updatePermissions(Player player) {
        UUID playerId = player.getUniqueId();

        // Remove old attachment if it exists and is still attached
        if (playerPermissions.containsKey(playerId)) {
            PermissionAttachment attachment = playerPermissions.get(playerId);
            if (attachment != null && player.getEffectivePermissions().stream()
                    .anyMatch(info -> info.getAttachment() == attachment)) {
                player.removeAttachment(attachment);
            }
            playerPermissions.remove(playerId);
        }

        // Add new permissions
        PermissionAttachment newAttachment = player.addAttachment(plugin);
        playerPermissions.put(playerId, newAttachment);

        String rank = getRank(player);
        List<String> permissions = plugin.getConfig().getStringList("rank.permissions." + rank);

        for (String permission : permissions) {
            newAttachment.setPermission(permission, true);
        }

        // Set OP status based on rank
        boolean isOp = rank.equals("ADMIN") || rank.equals("OWNER") || rank.equals("DEVELOPER");
        player.setOp(isOp);
    }
}