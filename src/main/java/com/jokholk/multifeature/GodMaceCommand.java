package com.jokholk.multifeature;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GodMaceCommand implements CommandExecutor {

    private final MainPlugin plugin;

    public GodMaceCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command cmd,
                             String label,
                             String[] args) {

        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);

        if (!rank.equals("OWNER") &&
                !rank.equals("ADMIN") &&
                !rank.equals("DEVELOPER")) {

            p.sendMessage("§cYou are not worthy to wield the God Mace.");
            return true;
        }

        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta m = mace.getItemMeta();

        m.setDisplayName("§x§F§B§D§A§0§0✦ GOD MACE ✦");

        m.setLore(List.of(
                "§7Weapon of divine judgment",
                "§eRight click §7to ascend to heaven",
                "§cStrike from above to erase existence",
                "§8Owner: " + p.getUniqueId()
        ));

        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING, 10, true);

        mace.setItemMeta(m);

        p.getInventory().addItem(mace);
        p.sendMessage("§6The power of GOD has been granted.");

        return true;
    }
}
