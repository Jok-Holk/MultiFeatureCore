package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class MainPlugin extends JavaPlugin implements Listener {

    private YamlConfiguration config;

    private RankSystem rankSystem;
    private ScoreboardManager scoreboardManager;
    private NametagManager nametagManager;

    private CheckpointManager checkpointManager;
    private ScoreboardSettings scoreSettings;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(
                new File(getDataFolder(), "config.yml")
        );

        rankSystem = new RankSystem(this);
        scoreboardManager = new ScoreboardManager(rankSystem);
        nametagManager = new NametagManager(rankSystem);
        scoreSettings = new ScoreboardSettings(this);
        // ➤ GIỜ DÒNG NÀY MỚI HỢP LỆ
        checkpointManager = new CheckpointManager(this);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ChatListener(rankSystem), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // ➤ Chống NPE khi chưa khai báo command trong plugin.yml
        if (getCommand("menu") != null)
            getCommand("menu").setExecutor(new MenuCommand(this));

        if (getCommand("rank") != null)
            getCommand("rank").setExecutor(new RankCommand(this));

        for (Player player : getServer().getOnlinePlayers()) {
            rankSystem.updatePermissions(player);
            setDefaultGamemode(player);
            if (scoreSettings.isEnabled(player))
                scoreboardManager.updateScoreboard(player);
            nametagManager.updateNametag(player);
        }
        getCommand("scoreboard")
                .setExecutor(new ScoreboardCommand(this));

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (scoreSettings.isEnabled(player))
                        scoreboardManager.updateScoreboard(player);
                    nametagManager.updateNametag(player);
                }
            }
        }.runTaskTimer(this, 0L, 100L);
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            rankSystem.savePlayerRank(player);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        rankSystem.updatePermissions(player);
        setDefaultGamemode(player);
        scoreboardManager.updateScoreboard(player);
        nametagManager.updateNametag(player);

        String rank = rankSystem.getRank(player);
        String rankColor = rankSystem.getRankColor(player);

        if (rank.equals("OWNER")) {
            event.joinMessage(
                    net.kyori.adventure.text.Component.text(rankColor + "GOD HAS COME")
            );
            playLightningSound();
        } else {
            event.joinMessage(
                    net.kyori.adventure.text.Component.text(
                            rankColor + "[" + rank + "] " + player.getName() + " joined the game"
                    )
            );
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        String rank = rankSystem.getRank(player);
        String rankColor = rankSystem.getRankColor(player);

        if (rank.equals("OWNER")) {
            event.quitMessage(
                    net.kyori.adventure.text.Component.text(rankColor + "GOD HAS LEFT")
            );
        } else {
            event.quitMessage(
                    net.kyori.adventure.text.Component.text(
                            rankColor + "[" + rank + "] " + player.getName() + " left the game"
                    )
            );
        }
    }

    public void setDefaultGamemode(Player player) {

        if ("GUEST".equals(rankSystem.getRank(player))) {

            player.setGameMode(GameMode.ADVENTURE);

            player.setFoodLevel(20);
            player.setSaturation(20f);
            player.setExhaustion(0f);

            player.setAllowFlight(true);
            player.setFlying(true);
        }
    }

    private void playLightningSound() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(
                    onlinePlayer.getLocation(),
                    Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                    1.0f,
                    1.0f
            );
        }
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public RankSystem getRankSystem() {
        return rankSystem;
    }

    public CheckpointManager getCheckpointManager() {
        return checkpointManager;
    }

    public void updatePlayerNametag(Player player) {
        nametagManager.updateNametag(player);
    }

    public void updatePlayerScoreboard(Player player) {
        scoreboardManager.updateScoreboard(player);
    }
    public ScoreboardSettings getScoreSettings() {
        return scoreSettings;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

}
