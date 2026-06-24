package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class MainPlugin extends JavaPlugin implements Listener {

    private YamlConfiguration config;

    private RankSystem         rankSystem;
    private ScoreboardManager  scoreboardManager;
    private NametagManager     nametagManager;
    private CheckpointManager  checkpointManager;
    private ScoreboardSettings scoreSettings;

    // ─── Feature managers v4.0.0 ───
    private HeightLockManager heightLockManager;
    private MeasureManager    measureManager;

    // ─── Feature managers v4.1.0 ───
    private DayLengthManager  dayLengthManager;

    // ─── Feature managers v4.5.0 ───
    private SpeedFlyManager   speedFlyManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));

        // ─── Core systems ───
        rankSystem        = new RankSystem(this);
        scoreboardManager = new ScoreboardManager(rankSystem);
        nametagManager    = new NametagManager(rankSystem);
        scoreSettings     = new ScoreboardSettings(this);
        checkpointManager = new CheckpointManager(this);

        // ─── v4 managers ───
        heightLockManager = new HeightLockManager();
        measureManager    = new MeasureManager();
        dayLengthManager  = new DayLengthManager(this);
        speedFlyManager   = new SpeedFlyManager();

        // ─── Listeners ───
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ChatListener(rankSystem), this);
        getServer().getPluginManager().registerEvents(new TravelListener(this), this);
        getServer().getPluginManager().registerEvents(new GodMaceListener(this), this);
        getServer().getPluginManager().registerEvents(new HeightLockListener(this), this);
        getServer().getPluginManager().registerEvents(new MeasureListener(this), this);
        getServer().getPluginManager().registerEvents(new AbyssalTridentListener(this), this);
        getServer().getPluginManager().registerEvents(new SpeedFlyListener(this), this);
        getServer().getPluginManager().registerEvents(new KitsListener(), this);
        getServer().getPluginManager().registerEvents(new HorseListener(), this);
        getServer().getPluginManager().registerEvents(new ExcaliburListener(this), this);
        getServer().getPluginManager().registerEvents(new RagnarokListener(this), this);
        getServer().getPluginManager().registerEvents(new IgnisListener(this), this);
        getServer().getPluginManager().registerEvents(new GraveListener(this), this);
        getServer().getPluginManager().registerEvents(new VerdantListener(this), this);
        getServer().getPluginManager().registerEvents(new VoidBowListener(this), this);

        // ─── Commands ───
        registerCmd("travel",     new TravelCommand(this));
        registerCmd("rank",       new RankCommand(this));
        registerCmd("heightlock", new HeightLockCommand(this));
        registerCmd("measure",    new MeasureCommand(this));
        registerCmdOnly("scoreboard", new ScoreboardCommand(this));
        registerCmdOnly("godmace",    new GodMaceCommand(this));
        registerCmdOnly("glass",      new GlassCommand(this));
        registerCmd("daylength",      new DayLengthCommand(this));
        registerCmdOnly("trident",    new AbyssalTridentCommand(this));
        registerCmd("speedfly",       new SpeedFlyCommand(this));
        registerCmd("kits",           new KitsCommand());
        registerCmd("horse",          new HorseCommand(this));
        registerCmdOnly("excalibur", new ExcaliburCommand(this));
        registerCmdOnly("ragnarok",  new RagnarokCommand(this));
        registerCmdOnly("ignis",     new IgnisCommand(this));
        registerCmdOnly("grave",     new GraveCommand(this));
        registerCmdOnly("verdant",   new VerdantCommand(this));
        registerCmdOnly("void",      new VoidBowCommand(this));

        // ─── Init online players (hot reload) ───
        for (Player player : getServer().getOnlinePlayers()) {
            rankSystem.updatePermissions(player);
            setDefaultGamemode(player);
            if (scoreSettings.isEnabled(player)) scoreboardManager.updateScoreboard(player);
            nametagManager.updateNametag(player);
        }

        // ─── Scoreboard timer (mỗi 5 giây) ───
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    if (scoreSettings.isEnabled(player)) {
                        scoreboardManager.updateScoreboard(player);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 100L);
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            rankSystem.savePlayerRank(player);
        }
        dayLengthManager.shutdown();
        Bukkit.getScheduler().cancelTasks(this);
    }

    // ─── Command registration helpers ───

    private <T extends org.bukkit.command.CommandExecutor & org.bukkit.command.TabCompleter>
    void registerCmd(String name, T handler) {
        var cmd = getCommand(name);
        if (cmd == null) { getLogger().warning("Command '" + name + "' missing in plugin.yml"); return; }
        cmd.setExecutor(handler);
        cmd.setTabCompleter(handler);
    }

    private void registerCmdOnly(String name, org.bukkit.command.CommandExecutor handler) {
        var cmd = getCommand(name);
        if (cmd == null) { getLogger().warning("Command '" + name + "' missing in plugin.yml"); return; }
        cmd.setExecutor(handler);
    }

    // ─── Events ───

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        rankSystem.updatePermissions(player);
        setDefaultGamemode(player);
        nametagManager.updateNametag(player);

        if (scoreSettings.isEnabled(player)) {
            scoreboardManager.updateScoreboard(player);
            Bukkit.getScheduler().runTaskLater(this, () -> {
                if (player.isOnline() && scoreSettings.isEnabled(player)) {
                    scoreboardManager.updateScoreboard(player);
                }
            }, 20L);
        } else {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        String rank      = rankSystem.getRank(player);
        String rankColor = rankSystem.getRankColor(player);

        if (rank.equals("OWNER")) {
            event.joinMessage(net.kyori.adventure.text.Component.text(rankColor + "GOD HAS COME"));
            playLightningSound();
        } else {
            event.joinMessage(net.kyori.adventure.text.Component.text(
                    rankColor + "[" + rank + "] " + player.getName() + " joined the game"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        scoreSettings.unload(player);

        String rank      = rankSystem.getRank(player);
        String rankColor = rankSystem.getRankColor(player);

        if (rank.equals("OWNER")) {
            event.quitMessage(net.kyori.adventure.text.Component.text(rankColor + "GOD HAS LEFT"));
        } else {
            event.quitMessage(net.kyori.adventure.text.Component.text(
                    rankColor + "[" + rank + "] " + player.getName() + " left the game"));
        }
    }

    // ─── Utilities ───

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
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);
        }
    }

    public void updatePlayerNametag(Player player)    { nametagManager.updateNametag(player); }
    public void updatePlayerScoreboard(Player player) { scoreboardManager.updateScoreboard(player); }

    // ─── Getters ───
    @Override
    public YamlConfiguration getConfig()           { return config; }
    public RankSystem         getRankSystem()       { return rankSystem; }
    public CheckpointManager  getCheckpointManager(){ return checkpointManager; }
    public ScoreboardSettings getScoreSettings()   { return scoreSettings; }
    public ScoreboardManager  getScoreboardManager(){ return scoreboardManager; }
    public HeightLockManager  getHeightLockManager(){ return heightLockManager; }
    public MeasureManager     getMeasureManager()   { return measureManager; }
    public DayLengthManager   getDayLengthManager() { return dayLengthManager; }
    public SpeedFlyManager    getSpeedFlyManager()  { return speedFlyManager; }
}
