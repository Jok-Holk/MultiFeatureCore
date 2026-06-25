package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class IgnisCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§6§l🔥 IGNIS CORE §6§l🔥";
    static final float  CONSUME_SECS = 8.0f;

    private final MainPlugin plugin;

    public IgnisCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage(Msg.IGNIS_NO_PERM.get(p));
            return true;
        }

        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_PICKAXE
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage(Msg.IGNIS_ALREADY_HAS.get(p));
                return true;
            }
        }

        ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta m = pick.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§7Struck from the heartfire of the world's §6first §7forge.",
                "§6Nothing stands §7before the core. §6Not stone. §7Not ore. §6Not flesh.",
                "§8Hold §6right-click §8to heat the core — §6release §8to drill.",
                "§6Drills through all blocks §7(except bedrock) and §6ignites §7everything in the tunnel.",
                "§8Owner: §7" + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.EFFICIENCY,  5, true);
        m.addEnchant(Enchantment.FORTUNE,     3, true);
        m.addEnchant(Enchantment.UNBREAKING,  3, true);
        m.addEnchant(Enchantment.MENDING,     1, true);
        m.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/ignis_core"));
        pick.setItemMeta(m);
        pick.setData(DataComponentTypes.CONSUMABLE,
                Consumable.consumable().consumeSeconds(CONSUME_SECS).build());

        p.getInventory().addItem(pick);
        p.sendMessage(Msg.IGNIS_GIVEN.get(p));
        return true;
    }
}
