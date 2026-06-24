package com.jokholk.multifeature.kits;
import com.jokholk.multifeature.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class KitsListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!KitManager.GUI_TITLE.equals(e.getView().getTitle())) return;

        e.setCancelled(true);

        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        if (!(e.getWhoClicked() instanceof Player p)) return;

        KitManager.Kit kit = KitManager.Kit.fromSlot(e.getSlot());
        if (kit == null) return;

        p.closeInventory();
        boolean gave = KitManager.tryGive(p, kit);
        if (gave) {
            p.sendMessage("§7──────────────────────────────");
            p.sendMessage("§6  Kit applied: " + kit.displayName);
            p.sendMessage("§7──────────────────────────────");
        }
    }
}
