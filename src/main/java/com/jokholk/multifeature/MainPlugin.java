package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class MainPlugin extends JavaPlugin implements Listener {
    private YamlConfiguration config;
    private HashMap<UUID, PermissionAttachment> playerPermissions = new HashMap<>();
    private RankSystem rankSystem; // Declare rank system
    private ScoreboardManager scoreboardManager;
    private NametagManager nametagManager;

    @Override
    public void onEnable() {
        // Load config.yml
        saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        // Initialize rank system
        rankSystem = new RankSystem(this);
        scoreboardManager = new ScoreboardManager(rankSystem);
        nametagManager = new NametagManager(rankSystem);

        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ChatListener(rankSystem), this);

        // Register commands
        this.getCommand("menu").setExecutor(new MenuCommand(this));
        this.getCommand("rank").setExecutor(new RankCommand(this));

        // Load and assign ranks for online players and set their permissions
        for (Player player : getServer().getOnlinePlayers()) {
            rankSystem.updatePermissions(player);  // Set permissions
            setDefaultGamemode(player);  // Set default gamemode
            scoreboardManager.updateScoreboard(player);  // Update scoreboard
            nametagManager.updateNametag(player);  // Update nametag
        }

        getLogger().info("MultiFeatureCore has been enabled!");

        // Periodically update scoreboard and nametags
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    scoreboardManager.updateScoreboard(player);
                    nametagManager.updateNametag(player);
                }
            }
        }.runTaskTimer(this, 0L, 100L);  // Every 5 seconds
    }

    @Override
    public void onDisable() {
        // Save player ranks when the server shuts down
        for (Player player : getServer().getOnlinePlayers()) {
            rankSystem.savePlayerRank(player);  // Save the rank to file
        }
        getLogger().info("MultiFeatureCore has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        rankSystem.updatePermissions(player);  // Update permissions
        setDefaultGamemode(player);  // Set default gamemode
        scoreboardManager.updateScoreboard(player);  // Update scoreboard
        nametagManager.updateNametag(player);  // Update nametag

        // Continuously replenish hunger for Guests
        new BukkitRunnable() {
            @Override
            public void run() {
                if ("GUEST".equals(rankSystem.getRank(player))) {
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                }
            }
        }.runTaskTimer(this, 0L, 100L);  // Every 5 seconds

        // Custom join message
        String rank = rankSystem.getRank(player);
        String rankColor = rankSystem.getRankColor(player);
        getLogger().info("Player " + player.getName() + " joined with rank: " + rank);

        if (rank.equals("OWNER")) {
            event.setJoinMessage(rankColor + "GOD HAS COME");
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        } else {
            event.setJoinMessage(rankColor + "[" + rank + "] " + player.getName() + " joined the game");        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String rank = rankSystem.getRank(player);
        String rankColor = rankSystem.getRankColor(player);

        // Custom leave message
        if (rank.equals("OWNER")) {
            event.setQuitMessage(rankColor + "GOD HAS LEFT");
            playLightningSound();
        } else {
            event.setQuitMessage(rankColor + "[" + rank + "] " + player.getName() + " left the game");
        }
    }

    public void setDefaultGamemode(Player player) {
        String rank = rankSystem.getRank(player);

        if ("GUEST".equals(rank)) {
            // Set gamemode to ADVENTURE (2)
            player.setGameMode(GameMode.ADVENTURE);

            // Disable hunger system
            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.setExhaustion(0f);

            // Enable flight
            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    private void playLightningSound() {
        // Play lightning sound for all players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public RankSystem getRankSystem() {
        return rankSystem;
    }

    public void updatePlayerNametag(Player player) {
        nametagManager.updateNametag(player);
    }

    public void updatePlayerScoreboard(Player player) {
        scoreboardManager.updateScoreboard(player);
    }
}