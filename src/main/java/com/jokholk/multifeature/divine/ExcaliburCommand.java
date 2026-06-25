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

public class ExcaliburCommand implements CommandExecutor {

    static final String DISPLAY_NAME  = "§4§l≪ Dark Excalibur ≫";
    static final float  CONSUME_SECS  = 10.0f; // matches ExcaliburListener.MAX_CHARGE

    private final MainPlugin plugin;

    public ExcaliburCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage(Msg.EXCALIBUR_NO_PERM.get(p));
            return true;
        }

        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot != null && slot.getType() == Material.NETHERITE_SWORD
                    && slot.hasItemMeta()
                    && DISPLAY_NAME.equals(slot.getItemMeta().getDisplayName())) {
                p.sendMessage(Msg.EXCALIBUR_ALREADY_HAS.get(p));
                return true;
            }
        }

        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta m = sword.getItemMeta();
        m.setDisplayName(DISPLAY_NAME);
        m.setLore(List.of(
                "§8Light abandoned it. Now it drinks shadow for sustenance.",
                "§4Every king who wielded it. §8Fell. §4Eventually.",
                "§8Hold §4right-click §8to raise the blade — §4release §8to slam.",
                "§4Everything §8in the shockwave §4dies. No drops. No mercy.",
                "§8Owner: §7" + p.getUniqueId()
        ));
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.SHARPNESS,  5, true);
        m.addEnchant(Enchantment.UNBREAKING, 3, true);
        m.addEnchant(Enchantment.MENDING,    1, true);
        m.setItemModel(new NamespacedKey("multifeature", "item/dark_excalibur"));
        sword.setItemMeta(m);
        sword.setData(DataComponentTypes.CONSUMABLE,
                Consumable.consumable().consumeSeconds(CONSUME_SECS).build());

        p.getInventory().addItem(sword);
        p.sendMessage(Msg.EXCALIBUR_GIVEN.get(p));
        return true;
    }
}
