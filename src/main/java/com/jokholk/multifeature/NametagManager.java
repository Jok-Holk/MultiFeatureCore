package com.jokholk.multifeature;

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

        Team team = board.getTeam(rank) == null ? board.registerNewTeam(rank) : board.getTeam(rank);
        team.setPrefix(color + "[" + rank + "] ");
        team.addEntry(player.getName());
    }
}