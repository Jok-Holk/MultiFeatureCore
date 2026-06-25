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
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.List;

public class NothanCommand implements CommandExecutor {

    static final String DISPLAY_NAME = "§6§l✦ DIVINE CROSSBOW ✦";
    static final float  CONSUME_SECS = 4.0f;

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
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§6Strung with the thread §7of celestial law. §6Every bolt a decree from above.",
                "§7The divine forces §6do not negotiate — §7they §6declare.",
                "§8Hold §6right-click §8to channel divine force — §6release §8to unleash the cone.",
                "§6Staggers and weakens §7everything in range. §6No target escapes §7the golden wave.",
                "§8Owner: §7" + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING,   10, true);
        m.addEnchant(Enchantment.QUICK_CHARGE,  3, true);
        m.addEnchant(Enchantment.MULTISHOT,     1, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/no_than"));
        crossbow.setItemMeta(m);
        // Consumable overrides vanilla crossbow loading mechanic
        crossbow.setData(DataComponentTypes.CONSUMABLE,
                Consumable.consumable().consumeSeconds(CONSUME_SECS).build());

        p.getInventory().addItem(crossbow);
        p.sendMessage(Msg.NOTHAN_GIVEN.get(p));
        return true;
    }

    static boolean hasNothan(Player p) {
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.CROSSBOW
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                return true;
            }
        }
        return false;
    }
}
