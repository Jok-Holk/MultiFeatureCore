package com.jokholk.multifeature;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class TravelListener implements Listener {

    private final MainPlugin plugin;

    public TravelListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!e.getView().getTitle().equals("Fast Travel"))
            return;

        e.setCancelled(true);

        Player p = (Player) e.getWhoClicked();

        int slot = e.getSlot() + 1;

        p.performCommand("travel load checkpoint" + slot);
    }
}
