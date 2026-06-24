package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class VoidBowCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§9§l✦ VOID CONSTELLATION ✦";

    private final MainPlugin plugin;

    public VoidBowCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage(Msg.VOID_NO_PERM.get(p));
            return true;
        }

        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.BOW
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage(Msg.VOID_ALREADY_HAS.get(p));
                return true;
            }
        }

        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta m = bow.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§7Do not draw the string. §9Draw the space between stars.",
                "§9The void does not miss. §7It was never aiming §9at you.",
                "§3Right-click §8to charge the constellation | §3Right-click §8again to fire.",
                "§8Owner: §7" + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.POWER,      5, true);
        m.addEnchant(Enchantment.UNBREAKING, 3, true);
        m.addEnchant(Enchantment.MENDING,    1, true);
        m.addEnchant(Enchantment.INFINITY,   1, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/void_constellation"));
        bow.setItemMeta(m);

        p.getInventory().addItem(bow);
        p.sendMessage(Msg.VOID_GIVEN.get(p));
        return true;
    }
}
