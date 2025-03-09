package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GUIManager {
    public static Inventory createMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, "§6Menu");

        // Add items to the GUI (Example: Compass for teleportation)
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§aFast Travel");
        meta.setLore(Arrays.asList("§7Click to teleport to spawn."));
        compass.setItemMeta(meta);
        gui.setItem(4, compass);

        return gui;
    }
}