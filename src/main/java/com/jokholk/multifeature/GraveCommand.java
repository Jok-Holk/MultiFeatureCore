package com.jokholk.multifeature;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GraveCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§5§l💀 GRAVE SOVEREIGN §5§l💀";

    private final MainPlugin plugin;

    public GraveCommand(MainPlugin plugin) {
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
            if (slot != null && slot.getType() == Material.NETHERITE_SHOVEL
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage("§cYou already carry the Grave Sovereign.");
                return true;
            }
        }

        ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemMeta m = shovel.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§7Every grave dug with this shovel belongs to the underworld.",
                "§8Right-click to charge | Right-click again to release.",
                "§8Owner: " + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.EFFICIENCY,  5, true);
        m.addEnchant(Enchantment.SILK_TOUCH,  1, true);
        m.addEnchant(Enchantment.UNBREAKING,  3, true);
        m.addEnchant(Enchantment.MENDING,     1, true);
        shovel.setItemMeta(m);

        p.getInventory().addItem(shovel);
        p.sendMessage("§5The underworld opens its arms.");
        return true;
    }
}
