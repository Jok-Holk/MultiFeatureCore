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

        // Lấy scoreboard hiện tại hoặc tạo mới
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        Objective obj = board.getObjective("mfc");

        if (obj == null) {
            obj = board.registerNewObjective(
                    "mfc",
                    "dummy",
                    "§dmc.sillycat.gay"
            );
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        // Xóa nội dung cũ
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        String name = player.getName();
        String rank = rankSystem.getRank(player);
        String color = rankSystem.getRankColor(player);

        int ping = player.getPing();

        // Tính giờ world
        long time = player.getWorld().getTime();
        int hours = (int) ((time / 1000 + 6) % 24);
        int minutes = (int) ((time % 1000) * 60 / 1000);
        String clock = String.format("%02d:%02d", hours, minutes);

        int online = Bukkit.getOnlinePlayers().size();
        int staff = countStaffOnline();

        // ===== STYLE A =====
        String[] lines = new String[]{

                "§7",

                "§f" + name + " §7| " + color + rank,

                "§fPing: §e" + ping + "ms §7| §f" + clock,

                "§7",

                "§fOnline: §a" + online + " §7| §fStaff: §a" + staff
        };

        int score = lines.length;

        for (String line : lines) {
            obj.getScore(line).setScore(score);
            score--;
        }

        player.setScoreboard(board);
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
