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

public class SpearCommand implements CommandExecutor {

    static final String ID = "spear_of_justice";
    // Technical duration for the Consumable component — kept far beyond any
    // realistic hold so vanilla's own "consume complete" never fires; the real
    // charge cap (3s) lives in SpearListener.MAX_CHARGE.
    static final float  CONSUME_SECS = 600.0f;

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
        m.setDisplayName(Msg.SPEAR_NAME.get(p));
        DivineId.set(m, ID);
        m.setLore(List.of(
                Msg.SPEAR_LORE_1.get(p),
                Msg.SPEAR_LORE_2.get(p),
                Msg.SPEAR_LORE_3.get(p),
                Msg.SPEAR_LORE_4.get(p),
                Msg.WEAPON_OWNER_LABEL.get(p) + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING, 10, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/spear_of_justice"));
        spear.setItemMeta(m);
        // Consumable overrides vanilla spear lunge — our listener handles the lunge
        spear.setData(DataComponentTypes.CONSUMABLE,
                Consumable.consumable().consumeSeconds(CONSUME_SECS).animation(ItemUseAnimation.NONE).build());

        p.getInventory().addItem(spear);
        p.sendMessage(Msg.SPEAR_GIVEN.get(p));
        return true;
    }

    static boolean hasSpear(Player p) {
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_SPEAR && DivineId.is(slot, ID)) {
                return true;
            }
        }
        return false;
    }
}
