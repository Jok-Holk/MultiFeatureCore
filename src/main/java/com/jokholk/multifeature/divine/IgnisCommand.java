package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
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
    // Technical duration for the Consumable component — kept far beyond any
    // realistic hold so vanilla's own "consume complete" never fires; the real
    // charge cap (8s) lives in IgnisListener.MAX_CHARGE.
    static final float  CONSUME_SECS = 600.0f;

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
                Msg.IGNIS_LORE_1.get(p),
                Msg.IGNIS_LORE_2.get(p),
                Msg.IGNIS_LORE_3.get(p),
                Msg.IGNIS_LORE_4.get(p),
                Msg.WEAPON_OWNER_LABEL.get(p) + p.getUniqueId()
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
                Consumable.consumable().consumeSeconds(CONSUME_SECS).animation(ItemUseAnimation.NONE).build());

        p.getInventory().addItem(pick);
        p.sendMessage(Msg.IGNIS_GIVEN.get(p));
        return true;
    }
}
