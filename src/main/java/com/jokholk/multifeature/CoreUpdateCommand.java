package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
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
 *
 * Usage: /coreupdate <now|0|1-10>
 * "now"/"0" applies the update immediately; 1-10 schedules a countdown (in
 * minutes) with broadcast warnings before the server restarts. There is no
 * default — a delay must always be specified explicitly.
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

        if (args.length < 1) {
            sender.sendMessage(Msg.CORE_UPDATE_USAGE.get(senderLang(sender)));
            return true;
        }

        String arg = args[0].toLowerCase();
        int delayMinutes;
        if (arg.equals("now") || arg.equals("0")) {
            delayMinutes = 0;
        } else {
            try {
                delayMinutes = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                sender.sendMessage(Msg.CORE_UPDATE_USAGE.get(senderLang(sender)));
                return true;
            }
            if (delayMinutes < 1 || delayMinutes > 10) {
                sender.sendMessage(Msg.CORE_UPDATE_USAGE.get(senderLang(sender)));
                return true;
            }
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

        sender.sendMessage(Msg.CORE_UPDATE_DOWNLOADING.get(senderLang(sender)));
        plugin.getLogger().info("[Update] Downloading new jar from " + jarUrl);

        final int finalDelayMinutes = delayMinutes;
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
            } catch (Exception e) {
                logAndTell(sender, "§cDownload failed: " + e.getMessage());
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
                return;
            }

            // Download is staged and verified — only now start warning players
            // and counting down, so a failed download never disrupts anyone.
            Bukkit.getScheduler().runTask(plugin, () ->
                    startCountdown(finalDelayMinutes, tempFile, currentJar.toPath()));
        });

        return true;
    }

    private void startCountdown(int delayMinutes, Path tempFile, Path currentJar) {
        if (delayMinutes <= 0) {
            applyUpdateAndShutdown(tempFile, currentJar);
            return;
        }

        broadcastMinutes(delayMinutes);
        final int[] secondsLeft = { delayMinutes * 60 };

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            secondsLeft[0]--;
            int s = secondsLeft[0];

            if (s <= 0) {
                task.cancel();
                applyUpdateAndShutdown(tempFile, currentJar);
                return;
            }

            if (s > 30 && s % 60 == 0) {
                broadcastMinutes(s / 60);
            } else if (s == 30 || s == 10) {
                broadcastSeconds(s);
            } else if (s <= 9) {
                broadcastSeconds(s);
            }
        }, 20L, 20L);
    }

    private void broadcastMinutes(int minutes) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Msg.CORE_UPDATE_COUNTDOWN_MIN.fmt(p, "n", minutes));
        }
        plugin.getLogger().info("[Update] Restarting in " + minutes + " minute(s).");
    }

    private void broadcastSeconds(int seconds) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Msg.CORE_UPDATE_COUNTDOWN_SEC.fmt(p, "n", seconds));
        }
        plugin.getLogger().info("[Update] Restarting in " + seconds + " second(s).");
    }

    private void applyUpdateAndShutdown(Path tempFile, Path currentJar) {
        boolean replacedNow = true;
        try {
            Files.move(tempFile, currentJar, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException lockEx) {
            // Windows refuses to replace a jar that the running JVM still has
            // open (no FILE_SHARE_DELETE) — Linux allows this fine (rename()
            // doesn't care that a process has the old inode open), so this
            // only bites local Windows test servers.
            if (isWindows()) {
                try {
                    scheduleWindowsDelayedReplace(tempFile, currentJar);
                    replacedNow = false;
                } catch (IOException e) {
                    plugin.getLogger().warning("[Update] Windows fallback failed: " + e.getMessage());
                    return;
                }
            } else {
                plugin.getLogger().warning("[Update] Failed to replace jar: " + lockEx.getMessage());
                return;
            }
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Msg.CORE_UPDATE_RESTARTING.get(p));
        }
        if (replacedNow) {
            plugin.getLogger().info("[Update] New jar in place, shutting down for restart.");
        } else {
            plugin.getLogger().info("[Update] Jar is locked (Windows) — a helper will replace it "
                    + "once this process exits. Shutting down for restart.");
        }
        Bukkit.shutdown();
    }

    private Language senderLang(CommandSender sender) {
        return sender instanceof Player p ? LanguageManager.getLang(p) : Language.ENGLISH;
    }

    private void logAndTell(CommandSender sender, String message) {
        plugin.getLogger().warning(message);
        Bukkit.getScheduler().runTask(plugin, () -> sender.sendMessage(message));
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /**
     * Windows-only fallback: spawn a detached cmd.exe helper that waits for
     * this JVM to fully release its file lock on the jar, then moves the
     * downloaded file into place itself. The child process is not tied to
     * this JVM's lifetime, so it survives Bukkit.shutdown().
     */
    private void scheduleWindowsDelayedReplace(Path newJar, Path targetJar) throws IOException {
        String script = "for /L %i in (1,1,30) do ("
                + "move /Y \"" + newJar + "\" \"" + targetJar + "\" >nul 2>&1 && exit /b 0"
                + " & timeout /t 1 /nobreak >nul"
                + ")";
        ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", script);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.start();
    }
}
