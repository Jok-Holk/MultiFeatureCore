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

public class GodMaceCommand implements CommandExecutor {

    static final String ID = "god_mace";

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

            p.sendMessage(Msg.GODMACE_NO_PERM.get(p));
            return true;
        }

        if (hasMace(p)) {
            p.sendMessage(Msg.GODMACE_ALREADY_HAS.get(p));
            return true;
        }

        ItemStack mace = new ItemStack(Material.MACE);
        ItemMeta m = mace.getItemMeta();

        m.setDisplayName(Msg.GODMACE_NAME.get(p));
        DivineId.set(m, ID);

        m.setLore(List.of(
                Msg.GODMACE_LORE_1.get(p),
                Msg.GODMACE_LORE_2.get(p),
                Msg.GODMACE_LORE_3.get(p),
                Msg.GODMACE_LORE_4.get(p),
                Msg.WEAPON_OWNER_LABEL.get(p) + p.getUniqueId()
        ));

        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING, 10, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/god_mace"));

        mace.setItemMeta(m);

        p.getInventory().addItem(mace);
        p.sendMessage(Msg.GODMACE_GIVEN.get(p));

        return true;
    }

    static boolean hasMace(Player p) {
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.MACE && DivineId.is(slot, ID)) {
                return true;
            }
        }
        return false;
    }
}
