package com.jokholk.multifeature;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatListener implements Listener {

    private final RankSystem rankSystem;

    public ChatListener(RankSystem rankSystem) {
        this.rankSystem = rankSystem;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {

        Player p = event.getPlayer();

        String rank = rankSystem.getRank(p);
        String color = rankSystem.getRankColor(p);

        // ➤ FORMAT CHUẨN NHƯ BẢN CŨ
        event.setFormat(
                color + "[" + rank + "] %1$s§r: %2$s"
        );
    }
}
