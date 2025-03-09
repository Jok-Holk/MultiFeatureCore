package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {
    private final RankSystem rankSystem;

    public ScoreboardManager(RankSystem rankSystem) {
        this.rankSystem = rankSystem;
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("MultiFeatureCore", "dummy", "§dmc.sillycat.gay");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Clear any existing entries
        board.getEntries().forEach(board::resetScores);

        // Define all lines in order (top to bottom)
        String[] lines = {
                "§9------------------",
                getCenteredTime(player),
                " ",
                "§c" + player.getName(),  // Player name in red
                "§aPersonal Info:",
                "§f- Rank: " + rankSystem.getRankColor(player) + rankSystem.getRank(player),
                "§f- Ping: §e" + player.getPing() + "ms",
                "  ",
                "§aServer Info:",
                "§f- Online: §a" + Bukkit.getOnlinePlayers().size() + " players",
                "§f- Staff: §a" + countStaffOnline(),
                "   ",
                "§9------------------"
        };

        // Add lines with teams and scores
        for (int i = 0; i < lines.length; i++) {
            int score = lines.length - i;  // Highest score at top (13 to 1)
            Team team = board.getTeam("line_" + score);
            if (team == null) {
                team = board.registerNewTeam("line_" + score);
            }
            String entry = "§" + (char)('a' + i);  // Unique invisible entries: §a, §b, §c, etc.
            team.addEntry(entry);
            team.setPrefix(lines[i]);
            obj.getScore(entry).setScore(score);  // Set score for ordering
        }

        player.setScoreboard(board);
    }

    private String getCenteredTime(Player player) {
        long time = player.getWorld().getTime();
        int hours = (int) ((time / 1000 + 6) % 24);
        int minutes = (int) ((time % 1000) * 60 / 1000);
        return "§7        " + String.format("%02d:%02d", hours, minutes) + "        ";
    }

    private int countStaffOnline() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> {
                    String rank = rankSystem.getRank(p);
                    return rank.equals("ADMIN") || rank.equals("OWNER") || rank.equals("DEVELOPER");
                })
                .count();
    }
}