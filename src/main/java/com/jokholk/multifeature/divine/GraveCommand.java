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
            p.sendMessage(Msg.GRAVE_NO_PERM.get(p));
            return true;
        }

        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_SHOVEL
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage(Msg.GRAVE_ALREADY_HAS.get(p));
                return true;
            }
        }

        ItemStack shovel = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemMeta m = shovel.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§8Every grave it digs is claimed by something §5older than death.",
                "§5The underworld §8does not differentiate §5the quick from the dead.",
                "§dRight-click §8to charge the depth | §dRight-click §8again to open earth.",
                "§8Owner: §7" + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.EFFICIENCY,  5, true);
        m.addEnchant(Enchantment.SILK_TOUCH,  1, true);
        m.addEnchant(Enchantment.UNBREAKING,  3, true);
        m.addEnchant(Enchantment.MENDING,     1, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/grave_sovereign"));
        shovel.setItemMeta(m);

        p.getInventory().addItem(shovel);
        p.sendMessage(Msg.GRAVE_GIVEN.get(p));
        return true;
    }
}
