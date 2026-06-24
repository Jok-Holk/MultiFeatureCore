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

        m.setDisplayName("§x§F§B§D§A§0§0✦ GOD MACE ✦");

        m.setLore(List.of(
                "§6Weapon of divine judgment. §eNot a gift — §6a verdict.",
                "§eTo strike from above §7is to speak the final word.",
                "§eRight-click §7to be cast into heaven | §eFall §7to pass judgment.",
                "§8Forged by: §7The Hand of God",
                "§8Owner: §7" + p.getUniqueId()
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
            if (slot != null && slot.getType() == Material.MACE
                    && slot.hasItemMeta()
                    && "§x§F§B§D§A§0§0✦ GOD MACE ✦".equals(slot.getItemMeta().getDisplayName())) {
                return true;
            }
        }
        return false;
    }
}
