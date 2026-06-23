package com.jokholk.multifeature;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class IgnisCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§6§l🔥 IGNIS CORE §6§l🔥";

    private final MainPlugin plugin;

    public IgnisCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage("§cYou are not worthy.");
            return true;
        }

        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_PICKAXE
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage("§cYou already carry the Ignis Core.");
                return true;
            }
        }

        ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta m = pick.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§7Struck from the heart of a volcano by hands that shaped the world.",
                "§8Right-click to charge | Right-click again to release.",
                "§8Owner: " + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.EFFICIENCY,   5, true);
        m.addEnchant(Enchantment.FORTUNE,      3, true);
        m.addEnchant(Enchantment.UNBREAKING,   3, true);
        m.addEnchant(Enchantment.MENDING,      1, true);
        m.addEnchant(Enchantment.FIRE_ASPECT,  2, true);
        pick.setItemMeta(m);

        p.getInventory().addItem(pick);
        p.sendMessage("§6The forge answers.");
        return true;
    }
}