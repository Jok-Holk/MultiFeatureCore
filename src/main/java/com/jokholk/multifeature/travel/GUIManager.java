package com.jokholk.multifeature.travel;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    public static Inventory createMenu(Player p, CheckpointManager cm) {

        int maxSlots = cm.getMaxSlots(p);
        int guiRows  = Math.max(1, (int) Math.ceil(maxSlots / 9.0));
        int guiSize  = guiRows * 9;

        Inventory gui = Bukkit.createInventory(null, guiSize, "Fast Travel");

        for (int i = 1; i <= maxSlots; i++) {
            String id = "checkpoint" + i;

            ItemStack item = new ItemStack(cm.getIcon(p, id));
            ItemMeta m = item.getItemMeta();

            m.setDisplayName("§a" + cm.getName(p, id));

            List<String> lore = new ArrayList<>();
            lore.add("§7ID: " + id);

            org.bukkit.Location l = cm.loadCheckpoint(p, id);
            if (l != null) {
                lore.add("§f" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ());
            } else {
                lore.add("§cNot set");
            }

            m.setLore(lore);
            item.setItemMeta(m);
            gui.setItem(i - 1, item);
        }

        // Filler cho các ô thừa trong row cuối
        if (maxSlots < guiSize) {
            ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta fm = filler.getItemMeta();
            fm.setDisplayName(" ");
            filler.setItemMeta(fm);
            for (int i = maxSlots; i < guiSize; i++) {
                gui.setItem(i, filler);
            }
        }

        return gui;
    }
}
