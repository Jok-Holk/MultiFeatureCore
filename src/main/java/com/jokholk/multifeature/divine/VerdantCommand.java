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

public class VerdantCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§2§l🌿 VERDANT CIPHER §2§l🌿";

    private final MainPlugin plugin;

    public VerdantCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage(Msg.VERDANT_NO_PERM.get(p));
            return true;
        }

        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_HOE
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage(Msg.VERDANT_ALREADY_HAS.get(p));
                return true;
            }
        }

        ItemStack hoe = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta m = hoe.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§7The first seed, the first field — this relic remembers §2all of it.",
                "§2The land has not forgotten. §7Neither has this.",
                "§aShift+Right-click §7to expand area | §aRight-click §7to till and ripen.",
                "§8Owner: §7" + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.EFFICIENCY,  5, true);
        m.addEnchant(Enchantment.FORTUNE,     3, true);
        m.addEnchant(Enchantment.UNBREAKING,  3, true);
        m.addEnchant(Enchantment.MENDING,     1, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/verdant_cipher"));
        hoe.setItemMeta(m);

        p.getInventory().addItem(hoe);
        p.sendMessage(Msg.VERDANT_GIVEN.get(p));
        return true;
    }
}
