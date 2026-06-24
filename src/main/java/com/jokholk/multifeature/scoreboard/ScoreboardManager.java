package com.jokholk.multifeature.scoreboard;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final RankSystem rankSystem;
    private final Map<UUID, Scoreboard> cache = new HashMap<>();

    public ScoreboardManager(RankSystem rankSystem) {
        this.rankSystem = rankSystem;
    }

    public void updateScoreboard(Player player) {

        Scoreboard board = cache.get(player.getUniqueId());

        // ===== TẠO BOARD CÁ NHÂN =====
        if (board == null) {

            board = Bukkit.getScoreboardManager().getNewScoreboard();

            Objective obj = board.registerNewObjective(
                    "mfc",
                    "dummy",
                    "§dmc.sillycat.gay"
            );

            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            cache.put(player.getUniqueId(), board);
        }

        // ===== QUAN TRỌNG NHẤT =====
        syncNametagTeams(board);

        Objective obj = board.getObjective("mfc");

        // Xóa nội dung cũ
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        String[] lines = buildLines(player);

        int score = lines.length;
        int i = 0;

        for (String line : lines) {
            obj.getScore(line + "§" + i).setScore(score);
            score--;
            i++;
        }

        if (player.getScoreboard() != board) {
            player.setScoreboard(board);
        }
    }

    // ===== HÀM CỨU MẠNG NAMEPLATE =====
    private void syncNametagTeams(Scoreboard personal) {

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Team mainTeam : main.getTeams()) {

            Team copy = personal.getTeam(mainTeam.getName());

            if (copy == null) {
                copy = personal.registerNewTeam(mainTeam.getName());
            }

            copy.setPrefix(mainTeam.getPrefix());
            copy.setSuffix(mainTeam.getSuffix());

            for (String entry : mainTeam.getEntries()) {
                if (!copy.hasEntry(entry)) {
                    copy.addEntry(entry);
                }
            }
        }
    }

    public void remove(Player p) {
        cache.remove(p.getUniqueId());

        p.setScoreboard(
                Bukkit.getScoreboardManager().getNewScoreboard()
        );
    }

    private String[] buildLines(Player player) {

        String name = player.getName();
        String rank = rankSystem.getRank(player);
        String color = rankSystem.getRankColor(player);

        int ping = player.getPing();

        long time = player.getWorld().getTime();
        int hours = (int) ((time / 1000 + 6) % 24);
        int minutes = (int) ((time % 1000) * 60 / 1000);

        String clock = String.format("%02d:%02d", hours, minutes);

        int online = Bukkit.getOnlinePlayers().size();
        int staff = countStaffOnline();

        return new String[]{
                "§7 ",
                "§f" + name + " §7| " + color + rank,
                "§fPing: §e" + ping + "ms §7| §f" + clock,
                "§7  ",
                "§fOnline: §a" + online + " §7| §fStaff: §a" + staff
        };
    }

    private int countStaffOnline() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> {
                    String r = rankSystem.getRank(p);
                    return r.equals("ADMIN")
                            || r.equals("OWNER")
                            || r.equals("DEVELOPER");
                })
                .count();
    }
}
