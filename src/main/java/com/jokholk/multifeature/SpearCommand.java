package com.jokholk.multifeature;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SpearCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§e§l⚖ SPEAR OF JUSTICE ⚖";

    private final MainPlugin plugin;

    public SpearCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage("§eJustice §7does not arm §ethe unworthy.");
            return true;
        }

        if (hasSpear(p)) {
            p.sendMessage("§eJustice §7speaks once. §eYou already carry its verdict.");
            return true;
        }

        ItemStack spear = new ItemStack(Material.TRIDENT);
        ItemMeta m = spear.getItemMeta();

        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§eCast into the world §7at the moment §eorder demanded a champion.",
                "§7It has never §emissed. §7Not once. §eNot in ten thousand years.",
                "§eThrow §7to pierce through — §ejustice does not stop for one offender.",
                "§7Enemies struck §eare slowed and blinded §7— they cannot flee the verdict.",
                "§8Owner: §7" + p.getUniqueId()
        ));

        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING,  10, true);
        m.addEnchant(Enchantment.LOYALTY,      3, true);
        m.addEnchant(Enchantment.IMPALING,     5, true);

        spear.setItemMeta(m);

        p.getInventory().addItem(spear);
        p.sendMessage("§e§l⚖ §7Justice §erecognizes you. §7The spear §eleaps to your hand. §e§l⚖");
        return true;
    }

    static boolean hasSpear(Player p) {
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.TRIDENT
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                return true;
            }
        }
        return false;
    }
}
