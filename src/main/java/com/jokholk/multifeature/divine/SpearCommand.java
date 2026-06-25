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

public class SpearCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§e§l⚖ SPEAR OF JUSTICE ⚖";
    static final float  CONSUME_SECS = 3.0f;

    private final MainPlugin plugin;

    public SpearCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage(Msg.SPEAR_NO_PERM.get(p));
            return true;
        }

        if (hasSpear(p)) {
            p.sendMessage(Msg.SPEAR_ALREADY_HAS.get(p));
            return true;
        }

        ItemStack spear = new ItemStack(Material.NETHERITE_SPEAR);
        ItemMeta m = spear.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§eCast by the will of order §7at the moment the world demanded a champion.",
                "§7It has never §emissed. §7Not once. §eNot in ten thousand years.",
                "§8Hold §eright-click §8to aim — §erelease §8to lunge forward at full force.",
                "§7Hits §eevery entity §7along the path. §eSlowness + Blindness. §7Kicks Survival players.",
                "§8Owner: §7" + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING, 10, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/spear_of_justice"));
        spear.setItemMeta(m);
        // Consumable overrides vanilla spear lunge — our listener handles the lunge
        spear.setData(DataComponentTypes.CONSUMABLE,
                Consumable.consumable().consumeSeconds(CONSUME_SECS).build());

        p.getInventory().addItem(spear);
        p.sendMessage(Msg.SPEAR_GIVEN.get(p));
        return true;
    }

    static boolean hasSpear(Player p) {
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_SPEAR
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                return true;
            }
        }
        return false;
    }
}
