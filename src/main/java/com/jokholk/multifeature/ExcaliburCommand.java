package com.jokholk.multifeature;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ExcaliburCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§4§l≪ Dark Excalibur ≫";

    private final MainPlugin plugin;

    public ExcaliburCommand(MainPlugin plugin) {
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

        // Kiem tra duplicate trong inventory
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_SWORD
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage("§cYou already wield the Dark Excalibur.");
                return true;
            }
        }

        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m = sword.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§8A holy sword drowned in darkness.",
                "§8Right-click to charge | Right-click again to release.",
                "§8Owner: " + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.SHARPNESS,   5, true);
        m.addEnchant(Enchantment.UNBREAKING,  3, true);
        m.addEnchant(Enchantment.MENDING,     1, true);
        sword.setItemMeta(m);

        p.getInventory().addItem(sword);
        p.sendMessage("§4⚔ §cThe darkness answers your call...");
        return true;
    }
}