package com.jokholk.multifeature.divine;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/**
 * Language-independent identity tag for divine weapons. Display names are
 * now localized per-player, so they can no longer be used to recognize
 * which weapon an ItemStack is (or to detect ownership/theft) — every
 * divine weapon is tagged with a fixed, untranslated id string instead.
 */
final class DivineId {

    private static final NamespacedKey KEY = new NamespacedKey("multifeature", "divine_id");

    static void set(ItemMeta m, String id) {
        m.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, id);
    }

    static boolean is(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return false;
        String tag = item.getItemMeta().getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        return id.equals(tag);
    }

    private DivineId() {}
}
