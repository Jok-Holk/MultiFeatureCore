package com.jokholk.multifeature;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RagnarokCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§c§l⚡ RAGNAROK §c§l⚡";

    private final MainPlugin plugin;

    public RagnarokCommand(MainPlugin plugin) {
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
            if (slot != null && slot.getType() == Material.NETHERITE_AXE
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage("§cYou already wield RAGNAROK.");
                return true;
            }
        }

        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta m = axe.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§7Forged from the fury of gods who have nothing left to lose.",
                "§8Right-click to charge | Right-click again to release.",
                "§8Owner: " + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.SHARPNESS,   5, true);
        m.addEnchant(Enchantment.EFFICIENCY,  5, true);
        m.addEnchant(Enchantment.UNBREAKING,  3, true);
        m.addEnchant(Enchantment.MENDING,     1, true);
        axe.setItemMeta(m);

        p.getInventory().addItem(axe);
        p.sendMessage("§cThe end begins with you.");
        return true;
    }
}