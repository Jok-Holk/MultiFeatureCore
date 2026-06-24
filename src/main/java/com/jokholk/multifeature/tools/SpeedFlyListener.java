package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SpeedFlyListener implements Listener {

    private final MainPlugin plugin;

    public SpeedFlyListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (!SpeedFlyCommand.isSpeedFeather(item)) return;

        e.setCancelled(true);

        Player p = e.getPlayer();
        if (!p.hasPermission("multifeature.speedfly")) return;

        SpeedFlyManager mgr = plugin.getSpeedFlyManager();
        boolean on = mgr.toggle(p);
        if (on) {
            int speed = mgr.getSpeed(p);
            p.sendMessage(Msg.SF_ON.fmt(p, "speed", speed));
        } else {
            p.sendMessage(Msg.SF_OFF.get(p));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (SpeedFlyCommand.isSpeedFeather(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        plugin.getSpeedFlyManager().reapplyAfterRespawn(e.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        SpeedFlyManager mgr = plugin.getSpeedFlyManager();
        Player p = e.getPlayer();
        if (!mgr.isEnabled(p)) return;

        GameMode next = e.getNewGameMode();
        if (next == GameMode.CREATIVE || next == GameMode.SPECTATOR) {
            mgr.disable(p);
            mgr.cleanup(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        SpeedFlyManager mgr = plugin.getSpeedFlyManager();
        Player p = e.getPlayer();
        if (mgr.isEnabled(p)) mgr.disable(p);
        mgr.cleanup(p);
    }
}
