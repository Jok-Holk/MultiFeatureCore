package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.time.Duration;

/**
 * Downloads the latest plugin jar from the URL configured at update.jar-url,
 * overwrites the currently-running jar on disk, then shuts the server down.
 * Relies on an external process supervisor (systemd, a restart loop, etc.)
 * to bring the server back up with the new jar — this command does NOT
 * attempt to hot-swap the running plugin classes.
 */
public class CoreUpdateCommand implements CommandExecutor {

    private final MainPlugin plugin;

    public CoreUpdateCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean allowed = sender instanceof ConsoleCommandSender;
        if (sender instanceof Player p) {
            String rank = plugin.getRankSystem().getRank(p);
            allowed = rank.equals("OWNER") || rank.equals("DEVELOPER");
        }
        if (!allowed) {
            sender.sendMessage("§cOnly console, OWNER, or DEVELOPER can run this command.");
            return true;
        }

        String jarUrl = plugin.getConfig().getString("update.jar-url", "");
        if (jarUrl.isEmpty()) {
            sender.sendMessage("§cupdate.jar-url is not set in config.yml.");
            return true;
        }
        if (!jarUrl.startsWith("https://")) {
            sender.sendMessage("§cupdate.jar-url must be an https:// URL.");
            return true;
        }

        File currentJar;
        try {
            CodeSource src = getClass().getProtectionDomain().getCodeSource();
            currentJar = new File(src.getLocation().toURI());
        } catch (Exception e) {
            sender.sendMessage("§cCould not resolve the currently running jar file: " + e.getMessage());
            return true;
        }

        sender.sendMessage("§7[Update] Downloading new jar from " + jarUrl + " ...");
        plugin.getLogger().info("[Update] Downloading new jar from " + jarUrl);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Path tempFile;
            try {
                tempFile = Files.createTempFile(currentJar.getParentFile().toPath(), "multifeaturecore-update-", ".jar");
            } catch (Exception e) {
                logAndTell(sender, "§cFailed to create temp file: " + e.getMessage());
                return;
            }

            try {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .connectTimeout(Duration.ofSeconds(15))
                        .build();
                HttpRequest request = HttpRequest.newBuilder(URI.create(jarUrl))
                        .timeout(Duration.ofSeconds(60))
                        .GET()
                        .build();
                HttpResponse<Path> response = client.send(request,
                        HttpResponse.BodyHandlers.ofFile(tempFile));

                if (response.statusCode() != 200) {
                    logAndTell(sender, "§cDownload failed: HTTP " + response.statusCode());
                    Files.deleteIfExists(tempFile);
                    return;
                }

                long size = Files.size(tempFile);
                if (size < 1024) {
                    logAndTell(sender, "§cDownloaded file is suspiciously small (" + size + " bytes) — aborting.");
                    Files.deleteIfExists(tempFile);
                    return;
                }

                Files.move(tempFile, currentJar.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                logAndTell(sender, "§cUpdate failed: " + e.getMessage());
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
                return;
            }

            Bukkit.getScheduler().runTask(plugin, () -> {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("§d[Update] New plugin jar downloaded — server restarting now.");
                }
                plugin.getLogger().info("[Update] New jar in place, shutting down for restart.");
                Bukkit.shutdown();
            });
        });

        return true;
    }

    private void logAndTell(CommandSender sender, String message) {
        plugin.getLogger().warning(message);
        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(message));
    }
}
