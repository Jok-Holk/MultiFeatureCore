package com.jokholk.multifeature;

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

        Inventory gui = Bukkit.createInventory(null, 9, "Fast Travel");

        for (int i = 1; i <= 9; i++) {

            String id = "checkpoint" + i;

            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta m = item.getItemMeta();

            String name = cm.getName(p, id);
            m.setDisplayName("§a" + name);

            List<String> lore = new ArrayList<>();
            lore.add("§7ID: " + id);

            org.bukkit.Location l = cm.loadCheckpoint(p, id);

            if (l != null) {
                lore.add("§f" + l.getBlockX() + ", "
                        + l.getBlockY() + ", "
                        + l.getBlockZ());
            } else {
                lore.add("§cNot set");
            }

            m.setLore(lore);
            item.setItemMeta(m);

            gui.setItem(i - 1, item);
        }

        return gui;
    }
}
