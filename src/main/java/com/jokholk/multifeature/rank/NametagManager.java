package com.jokholk.multifeature.rank;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagManager {

    private final RankSystem rankSystem;

    public NametagManager(RankSystem rankSystem) {
        this.rankSystem = rankSystem;
    }

    public void updateNametag(Player player) {

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        String rank = rankSystem.getRank(player);
        String color = rankSystem.getRankColor(player);

        String teamName = getPriority(rank) + "_" + rank;

        Team team = board.getTeam(teamName);

        if (team == null) {
            team = board.registerNewTeam(teamName);
        }

        team.setPrefix(color + "[" + rank + "] ");

        // XÓA KHỎI MỌI TEAM CŨ
        for (Team t : board.getTeams()) {
            if (t.hasEntry(player.getName())) {
                t.removeEntry(player.getName());
            }
        }

        team.addEntry(player.getName());

        // ➤ BẮT BUỘC: tất cả phải dùng main scoreboard
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.setScoreboard(board);
        }
    }

    private String getPriority(String rank) {
        switch (rank) {
            case "OWNER": return "01";
            case "ADMIN": return "02";
            case "DEVELOPER": return "03";
            case "BUILDER": return "04";
            case "GUEST": return "05";
            default: return "99";
        }
    }
}

