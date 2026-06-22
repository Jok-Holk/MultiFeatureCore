package com.jokholk.multifeature;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class AbyssalTridentCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§b⚓ §3ABYSSAL SOVEREIGN §b⚓";

    private final MainPlugin plugin;

    public AbyssalTridentCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);

        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage("§cThe abyss rejects you.");
            return true;
        }

        ItemStack trident = new ItemStack(Material.TRIDENT);
        ItemMeta m = trident.getItemMeta();

        m.setDisplayName(DISPLAY_NAME);

        m.setLore(List.of(
                "§3Forged where light dares not reach",
                "§3The crushing deep obeys its sovereign",
                "§bThrow §3to unleash the abyssal storm",
                "§9Lightning strikes all it touches",
                "§8Owner: " + p.getUniqueId()
        ));

        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING, 10, true);
        m.addEnchant(Enchantment.IMPALING, 5, true);
        m.addEnchant(Enchantment.LOYALTY, 3, true);

        trident.setItemMeta(m);

        p.getInventory().addItem(trident);
        p.sendMessage("§3The abyss grants you its sovereign weapon.");

        return true;
    }
}
