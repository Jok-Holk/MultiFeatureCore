package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MeasureListener implements Listener {

    private final MainPlugin plugin;

    public MeasureListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        MeasureManager mm = plugin.getMeasureManager();

        if (!mm.isActive(p)) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = e.getItem();
        if (!mm.isMeasureCompass(item)) return;

        e.setCancelled(true);

        Action action = e.getAction();

        if (action == Action.LEFT_CLICK_BLOCK && e.getClickedBlock() != null) {
            mm.setPoint1(p, e.getClickedBlock().getLocation());
            return;
        }

        if (action == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            if (!mm.hasPoint1(p)) {
                p.sendMessage(Msg.MEASURE_HINT_P2_FIRST.get(p));
                return;
            }
            mm.calculate(p, e.getClickedBlock().getLocation());
            return;
        }

        if (action == Action.LEFT_CLICK_AIR) {
            p.sendMessage(Msg.MEASURE_HINT_P1.get(p));
        } else if (action == Action.RIGHT_CLICK_AIR) {
            p.sendMessage(Msg.MEASURE_HINT_P2.get(p));
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        MeasureManager mm = plugin.getMeasureManager();

        if (!mm.isActive(p)) return;
        if (!mm.isMeasureCompass(e.getItemDrop().getItemStack())) return;

        e.setCancelled(true);
        mm.cancel(p);
        p.sendMessage(Msg.MEASURE_CANCEL_DROP.get(p));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getMeasureManager().cancel(e.getPlayer());
    }
}
