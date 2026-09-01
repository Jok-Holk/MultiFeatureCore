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
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.List;

public class NothanCommand implements CommandExecutor {

    static final String ID = "no_than";
    // Technical duration for the Consumable component — kept far beyond any
    // realistic hold so vanilla's own "consume complete" never fires; the real
    // charge cap (4s) lives in NothanListener.MAX_CHARGE.
    static final float  CONSUME_SECS = 600.0f;

    private final MainPlugin plugin;

    public NothanCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage(Msg.NOTHAN_NO_PERM.get(p));
            return true;
        }

        if (hasNothan(p)) {
            p.sendMessage(Msg.NOTHAN_ALREADY_HAS.get(p));
            return true;
        }

        ItemStack crossbow = new ItemStack(Material.CROSSBOW);
        CrossbowMeta m = (CrossbowMeta) crossbow.getItemMeta();
        m.setDisplayName(Msg.NOTHAN_NAME.get(p));
        DivineId.set(m, ID);
        m.setLore(List.of(
                Msg.NOTHAN_LORE_1.get(p),
                Msg.NOTHAN_LORE_2.get(p),
                Msg.NOTHAN_LORE_3.get(p),
                Msg.NOTHAN_LORE_4.get(p),
                Msg.WEAPON_OWNER_LABEL.get(p) + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING,   10, true);
        m.addEnchant(Enchantment.QUICK_CHARGE,  3, true);
        m.addEnchant(Enchantment.MULTISHOT,     1, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/no_than"));
        crossbow.setItemMeta(m);
        // Consumable overrides vanilla crossbow loading mechanic
        crossbow.setData(DataComponentTypes.CONSUMABLE,
                Consumable.consumable().consumeSeconds(CONSUME_SECS).animation(ItemUseAnimation.NONE).build());

        p.getInventory().addItem(crossbow);
        p.sendMessage(Msg.NOTHAN_GIVEN.get(p));
        return true;
    }

    static boolean hasNothan(Player p) {
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.CROSSBOW && DivineId.is(slot, ID)) {
                return true;
            }
        }
        return false;
    }
}
