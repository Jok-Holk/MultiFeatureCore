package com.jokholk.multifeature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitManager {

    static final String GUI_TITLE = "Kits";

    public enum Kit {
        WARRIOR     (0, "§fWarrior",     "warrior"),
        JUGGERNAUT  (1, "§cJuggernaut",  "juggernaut"),
        SPEAR_MASTER(2, "§bSpear Master","spear"),
        ARCHER      (3, "§aArcher",      "archer"),
        SURVIVOR    (4, "§6Survivor",    "survivor"),
        BERSERKER   (5, "§4Berserker",   "berserker"),
        GHOST       (6, "§dGhost",       "ghost"),
        ALCHEMIST   (7, "§eAlchemist",   "alchemist"),
        PANTHEON    (8, "§5§lPANTHEON",  "pantheon");

        public final int    slot;
        public final String displayName;
        public final String cmdName;

        Kit(int slot, String displayName, String cmdName) {
            this.slot        = slot;
            this.displayName = displayName;
            this.cmdName     = cmdName;
        }

        public static Kit fromSlot(int slot) {
            for (Kit k : values()) if (k.slot == slot) return k;
            return null;
        }

        public static Kit fromCmd(String name) {
            for (Kit k : values()) if (k.cmdName.equalsIgnoreCase(name)) return k;
            return null;
        }
    }

    // ────────────────────────────────────────────────────────────
    //  GUI icons
    // ────────────────────────────────────────────────────────────

    public static ItemStack buildIcon(Kit kit) {
        return switch (kit) {
            case WARRIOR      -> icon(Material.IRON_SWORD,        kit.displayName,
                    "§7Netherite armor + sword.",
                    "§7Sharpness V, Fire Aspect II.",
                    "§8Click to receive kit.");
            case JUGGERNAUT   -> icon(Material.MACE,              kit.displayName,
                    "§7Netherite armor + mace.",
                    "§7Density V, Wind Burst III.",
                    "§8Click to receive kit.");
            case SPEAR_MASTER -> icon(Material.TRIDENT,           kit.displayName,
                    "§7Chainmail armor + trident.",
                    "§7Loyalty III, Channeling.",
                    "§8Click to receive kit.");
            case ARCHER       -> icon(Material.BOW,               kit.displayName,
                    "§7Leather armor + bow.",
                    "§7Power V, Infinity + crossbow.",
                    "§8Click to receive kit.");
            case SURVIVOR     -> buildGlowIcon(Material.GOLDEN_HELMET,  kit.displayName,
                    "§7Iron armor + full tool set.",
                    "§7Fortune III, Efficiency V.",
                    "§8Click to receive kit.");
            case BERSERKER    -> buildGlowIcon(Material.IRON_HELMET,    kit.displayName,
                    "§7Netherite armor + axe.",
                    "§7Sharpness V + strength.",
                    "§8Click to receive kit.");
            case GHOST        -> icon(Material.ENDER_PEARL,       kit.displayName,
                    "§7Leather + elytra. Stealth,",
                    "§7invisibility + pearls.",
                    "§8Click to receive kit.");
            case ALCHEMIST    -> buildAlchemistIcon(kit.displayName);
            case PANTHEON     -> buildPantheonIcon(kit.displayName);
        };
    }

    private static ItemStack icon(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        m.setLore(Arrays.asList(lore));
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack buildGlowIcon(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        m.setLore(Arrays.asList(lore));
        m.addEnchant(Enchantment.UNBREAKING, 1, true);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack buildAlchemistIcon(String name) {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta m = (PotionMeta) potion.getItemMeta();
        m.setDisplayName(name);
        m.setBasePotionType(PotionType.HARMING);
        m.setLore(List.of(
                "§7Leather armor + potion arsenal.",
                "§7Harming II, Poison, Weakness.",
                "§8Click to receive kit."));
        potion.setItemMeta(m);
        return potion;
    }

    private static ItemStack buildPantheonIcon(String name) {
        ItemStack block = new ItemStack(Material.NETHERITE_BLOCK);
        ItemMeta m = block.getItemMeta();
        m.setDisplayName(name);
        m.setLore(List.of(
                "§7All weapons — named & enhanced.",
                "§7Wings of the Chaos God elytra.",
                "§6§lChaos God armor set.",
                "§8Click to receive ultimate kit."));
        m.addEnchant(Enchantment.UNBREAKING, 1, true);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        block.setItemMeta(m);
        return block;
    }

    // ────────────────────────────────────────────────────────────
    //  Inventory check + pending confirmation
    // ────────────────────────────────────────────────────────────

    private static final Map<UUID, Kit>  pendingKits   = new HashMap<>();
    private static final Map<UUID, Long> pendingExpiry = new HashMap<>();
    private static final long EXPIRY_MS = 30_000L;

    /**
     * Nếu inventory trống → give thẳng (trả về true).
     * Nếu có item → lưu pending, gửi message confirm (trả về false).
     * Gọi lại khi đã có pending → refresh pending với kit mới.
     */
    public static boolean tryGive(Player p, Kit kit) {
        if (isInventoryEmpty(p)) {
            give(p, kit);
            return true;
        }
        pendingKits.put(p.getUniqueId(), kit);
        pendingExpiry.put(p.getUniqueId(), System.currentTimeMillis() + EXPIRY_MS);
        sendConfirmMessage(p, kit);
        return false;
    }

    public static void confirmGive(Player p) {
        UUID uuid = p.getUniqueId();
        Kit kit = pendingKits.get(uuid);
        if (kit == null) {
            p.sendMessage("§cNo pending kit. Use §f/kits §cto select one first.");
            return;
        }
        if (System.currentTimeMillis() > pendingExpiry.getOrDefault(uuid, 0L)) {
            pendingKits.remove(uuid);
            pendingExpiry.remove(uuid);
            p.sendMessage("§cKit selection expired (30s). Use §f/kits §cagain.");
            return;
        }
        pendingKits.remove(uuid);
        pendingExpiry.remove(uuid);
        give(p, kit);
        p.sendMessage("§7──────────────────────────────");
        p.sendMessage("§6  Kit applied: " + kit.displayName);
        p.sendMessage("§7──────────────────────────────");
    }

    public static void cancelGive(Player p) {
        if (pendingKits.remove(p.getUniqueId()) != null) {
            pendingExpiry.remove(p.getUniqueId());
            p.sendMessage("§cKit selection cancelled.");
        } else {
            p.sendMessage("§cNo pending kit.");
        }
    }

    private static boolean isInventoryEmpty(Player p) {
        for (ItemStack i : p.getInventory().getStorageContents())
            if (i != null && i.getType() != Material.AIR) return false;
        for (ItemStack i : p.getInventory().getArmorContents())
            if (i != null && i.getType() != Material.AIR) return false;
        for (ItemStack i : p.getInventory().getExtraContents())
            if (i != null && i.getType() != Material.AIR) return false;
        return true;
    }

    private static void sendConfirmMessage(Player p, Kit kit) {
        p.sendMessage("§7──────────────────────────────");
        p.sendMessage("§eInventory not empty! §7Items will be cleared.");
        p.sendMessage("§7Applying: " + kit.displayName);

        Component buttons = Component.text()
                .append(Component.text("  "))
                .append(Component.text("[Confirm]")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/kits confirm"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Clear inventory and apply kit"))))
                .append(Component.text("  |  ")
                        .color(NamedTextColor.DARK_GRAY))
                .append(Component.text("[Cancel]")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/kits cancel"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Cancel kit selection"))))
                .build();
        p.sendMessage(buttons);
        p.sendMessage("§7──────────────────────────────");
    }

    // ────────────────────────────────────────────────────────────
    //  Kit dispatcher
    // ────────────────────────────────────────────────────────────

    public static void give(Player p, Kit kit) {
        p.getInventory().clear();
        switch (kit) {
            case WARRIOR      -> giveWarrior(p);
            case JUGGERNAUT   -> giveJuggernaut(p);
            case SPEAR_MASTER -> giveSpearMaster(p);
            case ARCHER       -> giveArcher(p);
            case SURVIVOR     -> giveSurvivor(p);
            case BERSERKER    -> giveBerserker(p);
            case GHOST        -> giveGhost(p);
            case ALCHEMIST    -> giveAlchemist(p);
            case PANTHEON     -> givePantheon(p);
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Armor helpers
    // ────────────────────────────────────────────────────────────

    private static void wearNetherite(Player p) {
        p.getInventory().setHelmet(enc(new ItemStack(Material.NETHERITE_HELMET),
                Enchantment.PROTECTION, 4, Enchantment.RESPIRATION, 3,
                Enchantment.AQUA_AFFINITY, 1, Enchantment.THORNS, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setChestplate(enc(new ItemStack(Material.NETHERITE_CHESTPLATE),
                Enchantment.PROTECTION, 4, Enchantment.THORNS, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setLeggings(enc(new ItemStack(Material.NETHERITE_LEGGINGS),
                Enchantment.PROTECTION, 4, Enchantment.THORNS, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setBoots(enc(new ItemStack(Material.NETHERITE_BOOTS),
                Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4,
                Enchantment.DEPTH_STRIDER, 3, Enchantment.THORNS, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
    }

    private static void wearChainmail(Player p) {
        p.getInventory().setHelmet(enc(new ItemStack(Material.CHAINMAIL_HELMET),
                Enchantment.PROTECTION, 3, Enchantment.RESPIRATION, 3,
                Enchantment.AQUA_AFFINITY, 1, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setChestplate(enc(new ItemStack(Material.CHAINMAIL_CHESTPLATE),
                Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setLeggings(enc(new ItemStack(Material.CHAINMAIL_LEGGINGS),
                Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setBoots(enc(new ItemStack(Material.CHAINMAIL_BOOTS),
                Enchantment.PROTECTION, 3, Enchantment.FEATHER_FALLING, 4,
                Enchantment.DEPTH_STRIDER, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
    }

    private static void wearIron(Player p) {
        p.getInventory().setHelmet(enc(new ItemStack(Material.IRON_HELMET),
                Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setChestplate(enc(new ItemStack(Material.IRON_CHESTPLATE),
                Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setLeggings(enc(new ItemStack(Material.IRON_LEGGINGS),
                Enchantment.PROTECTION, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setBoots(enc(new ItemStack(Material.IRON_BOOTS),
                Enchantment.PROTECTION, 3, Enchantment.FEATHER_FALLING, 4,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
    }

    private static void wearLeather(Player p, Color color) {
        p.getInventory().setHelmet(leather(Material.LEATHER_HELMET, color));
        p.getInventory().setChestplate(leather(Material.LEATHER_CHESTPLATE, color));
        p.getInventory().setLeggings(leather(Material.LEATHER_LEGGINGS, color));
        p.getInventory().setBoots(leather(Material.LEATHER_BOOTS, color,
                Enchantment.FEATHER_FALLING, 4));
    }

    private static ItemStack leather(Material mat, Color color, Object... extraEnchants) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta m = (LeatherArmorMeta) item.getItemMeta();
        m.setColor(color);
        m.addEnchant(Enchantment.PROTECTION, 2, true);
        m.addEnchant(Enchantment.UNBREAKING, 3, true);
        m.addEnchant(Enchantment.MENDING, 1, true);
        for (int i = 0; i < extraEnchants.length; i += 2) {
            m.addEnchant((Enchantment) extraEnchants[i], (Integer) extraEnchants[i + 1], true);
        }
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack shield() {
        return enc(new ItemStack(Material.SHIELD),
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1);
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 1 — Warrior  (netherite, sword + shield, slots 0–26)
    // ────────────────────────────────────────────────────────────

    private static void giveWarrior(Player p) {
        wearNetherite(p);
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar
        set(p, 0, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2,
                Enchantment.LOOTING, 3, Enchantment.KNOCKBACK, 2,
                Enchantment.SWEEPING_EDGE, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, shield());
        set(p, 2, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 3, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 4, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 5, new ItemStack(Material.GOLDEN_APPLE, 4));
        set(p, 6, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
        set(p, 7, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 8, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        // Row 1
        set(p,  9, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 10, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 11, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 12, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 13, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 14, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 15, new ItemStack(Material.OBSIDIAN, 4));
        set(p, 16, new ItemStack(Material.WATER_BUCKET));
        set(p, 17, new ItemStack(Material.MILK_BUCKET));
        // Row 2
        set(p, 18, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 19, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 20, new ItemStack(Material.GOLDEN_APPLE, 4));
        set(p, 21, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 22, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 23, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 24, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 25, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 26, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 2 — Juggernaut  (netherite, mace, slow falling for drops)
    // ────────────────────────────────────────────────────────────

    private static void giveJuggernaut(Player p) {
        wearNetherite(p);
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar — Wind Charge trong tầm tay để tự phóng lên rồi rơi đánh mace
        set(p, 0, enc(new ItemStack(Material.MACE),
                Enchantment.DENSITY, 5, Enchantment.BREACH, 4,
                Enchantment.WIND_BURST, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 2, shield());
        set(p, 3, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 4, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 5, new ItemStack(Material.WIND_CHARGE, 16));
        set(p, 6, new ItemStack(Material.WIND_CHARGE, 16));
        set(p, 7, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 8, new ItemStack(Material.GOLDEN_APPLE, 4));
        // Row 1
        set(p,  9, new ItemStack(Material.WIND_CHARGE, 16));
        set(p, 10, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 11, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 12, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 13, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 14, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 15, new ItemStack(Material.OBSIDIAN, 4));
        set(p, 16, new ItemStack(Material.WATER_BUCKET));
        set(p, 17, new ItemStack(Material.MILK_BUCKET));
        // Row 2
        set(p, 18, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 19, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 20, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
        set(p, 21, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 22, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 23, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 24, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 25, new ItemStack(Material.WIND_CHARGE, 8));
        set(p, 26, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 3 — Spear Master  (chainmail, trident + water)
    // ────────────────────────────────────────────────────────────

    private static void giveSpearMaster(Player p) {
        wearChainmail(p);
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar
        set(p, 0, enc(new ItemStack(Material.TRIDENT),
                Enchantment.LOYALTY, 3, Enchantment.CHANNELING, 1,
                Enchantment.IMPALING, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 2, new ItemStack(Material.WATER_BUCKET));
        set(p, 3, shield());
        set(p, 4, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 5, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 6, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 7, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 8, new ItemStack(Material.GOLDEN_APPLE, 4));
        // Row 1
        set(p,  9, new ItemStack(Material.COOKED_SALMON, 16));
        set(p, 10, new ItemStack(Material.COOKED_SALMON, 16));
        set(p, 11, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 12, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 13, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 14, pot(Material.POTION, PotionType.LONG_INVISIBILITY));
        set(p, 15, new ItemStack(Material.WATER_BUCKET));
        set(p, 16, new ItemStack(Material.MILK_BUCKET));
        set(p, 17, new ItemStack(Material.OBSIDIAN, 4));
        // Row 2
        set(p, 18, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 19, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 20, new ItemStack(Material.GOLDEN_APPLE, 4));
        set(p, 21, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 22, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 23, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 24, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 25, new ItemStack(Material.COOKED_SALMON, 16));
        set(p, 26, pot(Material.POTION, PotionType.STRONG_REGENERATION));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 4 — Archer  (leather forest-green, bow + crossbow)
    // ────────────────────────────────────────────────────────────

    private static void giveArcher(Player p) {
        wearLeather(p, Color.fromRGB(34, 85, 34));
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar
        set(p, 0, enc(new ItemStack(Material.BOW),
                Enchantment.POWER, 5, Enchantment.FLAME, 1,
                Enchantment.INFINITY, 1, Enchantment.PUNCH, 2, Enchantment.UNBREAKING, 3));
        set(p, 1, enc(new ItemStack(Material.CROSSBOW),
                Enchantment.QUICK_CHARGE, 3, Enchantment.PIERCING, 4,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 2, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 3, new ItemStack(Material.ARROW, 1));
        set(p, 4, new ItemStack(Material.ARROW, 32));
        set(p, 5, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 6, new ItemStack(Material.ENDER_PEARL, 16));
        set(p, 7, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 8, new ItemStack(Material.GOLDEN_APPLE, 4));
        // Row 1
        set(p,  9, new ItemStack(Material.BREAD, 16));
        set(p, 10, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 11, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 12, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 13, pot(Material.POTION, PotionType.LONG_INVISIBILITY));
        set(p, 14, new ItemStack(Material.SPECTRAL_ARROW, 16));
        set(p, 15, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 16, new ItemStack(Material.ARROW, 32));
        set(p, 17, new ItemStack(Material.MILK_BUCKET));
        // Row 2
        set(p, 18, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 19, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 20, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
        set(p, 21, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 22, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 23, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 24, new ItemStack(Material.WATER_BUCKET));
        set(p, 25, new ItemStack(Material.GOLDEN_APPLE, 4));
        set(p, 26, new ItemStack(Material.BREAD, 16));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 5 — Survivor  (iron, full tool set + utility)
    // ────────────────────────────────────────────────────────────

    private static void giveSurvivor(Player p) {
        wearIron(p);
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar
        set(p, 0, enc(new ItemStack(Material.NETHERITE_PICKAXE),
                Enchantment.EFFICIENCY, 5, Enchantment.FORTUNE, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, enc(new ItemStack(Material.NETHERITE_SHOVEL),
                Enchantment.EFFICIENCY, 5, Enchantment.FORTUNE, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 2, enc(new ItemStack(Material.NETHERITE_AXE),
                Enchantment.EFFICIENCY, 5, Enchantment.FORTUNE, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 3, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.LOOTING, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 4, shield());
        set(p, 5, new ItemStack(Material.TORCH, 64));
        set(p, 6, new ItemStack(Material.COOKED_BEEF, 32));
        set(p, 7, new ItemStack(Material.ENDER_PEARL, 4));
        set(p, 8, new ItemStack(Material.GOLDEN_APPLE, 2));
        // Row 1
        set(p,  9, new ItemStack(Material.COOKED_BEEF, 32));
        set(p, 10, new ItemStack(Material.BREAD, 32));
        set(p, 11, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 12, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 13, new ItemStack(Material.WATER_BUCKET));
        set(p, 14, new ItemStack(Material.MILK_BUCKET));
        set(p, 15, new ItemStack(Material.OBSIDIAN, 8));
        set(p, 16, new ItemStack(Material.COBBLESTONE, 64));
        set(p, 17, new ItemStack(Material.TNT, 4));
        // Row 2
        set(p, 18, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 19, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 20, new ItemStack(Material.TORCH, 64));
        set(p, 21, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 22, new ItemStack(Material.ENDER_PEARL, 4));
        set(p, 23, new ItemStack(Material.GOLDEN_APPLE, 2));
        set(p, 24, new ItemStack(Material.IRON_BLOCK, 8));
        set(p, 25, new ItemStack(Material.LADDER, 32));
        set(p, 26, new ItemStack(Material.GRAVEL, 32));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 6 — Berserker  (netherite, axe, pure aggression)
    // ────────────────────────────────────────────────────────────

    private static void giveBerserker(Player p) {
        wearNetherite(p);
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar
        set(p, 0, enc(new ItemStack(Material.NETHERITE_AXE),
                Enchantment.SHARPNESS, 5, Enchantment.EFFICIENCY, 5,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2,
                Enchantment.LOOTING, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 2, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 3, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 4, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 5, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 6, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 7, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
        set(p, 8, new ItemStack(Material.GOLDEN_APPLE, 4));
        // Row 1
        set(p,  9, new ItemStack(Material.COOKED_PORKCHOP, 16));
        set(p, 10, new ItemStack(Material.COOKED_PORKCHOP, 16));
        set(p, 11, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 12, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 13, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 14, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 15, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 16, new ItemStack(Material.MILK_BUCKET));
        set(p, 17, new ItemStack(Material.WATER_BUCKET));
        // Row 2
        set(p, 18, new ItemStack(Material.OBSIDIAN, 4));
        set(p, 19, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 20, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 21, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
        set(p, 22, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 23, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 24, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 25, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 26, new ItemStack(Material.COOKED_PORKCHOP, 16));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 7 — Ghost  (leather dark + elytra, stealth)
    // ────────────────────────────────────────────────────────────

    private static void giveGhost(Player p) {
        Color darkGray = Color.fromRGB(25, 25, 25);
        p.getInventory().setHelmet(leather(Material.LEATHER_HELMET, darkGray));
        p.getInventory().setChestplate(noBreak(enc(new ItemStack(Material.ELYTRA),
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)));
        p.getInventory().setLeggings(leather(Material.LEATHER_LEGGINGS, darkGray));
        p.getInventory().setBoots(leather(Material.LEATHER_BOOTS, darkGray,
                Enchantment.FEATHER_FALLING, 4, Enchantment.DEPTH_STRIDER, 3));
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar
        set(p, 0, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.LOOTING, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, new ItemStack(Material.FIREWORK_ROCKET, 32));
        set(p, 2, new ItemStack(Material.ENDER_PEARL, 16));
        set(p, 3, pot(Material.POTION, PotionType.LONG_INVISIBILITY));
        set(p, 4, pot(Material.POTION, PotionType.LONG_INVISIBILITY));
        set(p, 5, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 6, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 7, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 8, new ItemStack(Material.GOLDEN_APPLE, 2));
        // Row 1
        set(p,  9, new ItemStack(Material.ENDER_PEARL, 16));
        set(p, 10, new ItemStack(Material.FIREWORK_ROCKET, 16));
        set(p, 11, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 12, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 13, pot(Material.POTION, PotionType.LONG_SLOW_FALLING));
        set(p, 14, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 15, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 16, new ItemStack(Material.MILK_BUCKET));
        set(p, 17, new ItemStack(Material.OBSIDIAN, 4));
        // Row 2
        set(p, 18, new ItemStack(Material.WATER_BUCKET));
        set(p, 19, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 20, new ItemStack(Material.GOLDEN_APPLE, 2));
        set(p, 21, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
        set(p, 22, pot(Material.POTION, PotionType.LONG_INVISIBILITY));
        set(p, 23, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 24, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 25, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 26, new ItemStack(Material.ENDER_PEARL, 8));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 8 — Alchemist  (leather purple, potion thrower)
    // ────────────────────────────────────────────────────────────

    private static void giveAlchemist(Player p) {
        wearLeather(p, Color.fromRGB(100, 0, 128));
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        // Hotbar — offensive splash potions
        set(p, 0, enc(new ItemStack(Material.NETHERITE_SWORD),
                Enchantment.SHARPNESS, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 2, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 3, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 4, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 5, pot(Material.SPLASH_POTION, PotionType.STRONG_POISON));
        set(p, 6, pot(Material.SPLASH_POTION, PotionType.STRONG_POISON));
        set(p, 7, pot(Material.SPLASH_POTION, PotionType.WEAKNESS));
        set(p, 8, pot(Material.SPLASH_POTION, PotionType.STRONG_SLOWNESS));
        // Row 1 — more offense
        set(p,  9, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 10, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 11, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 12, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 13, pot(Material.SPLASH_POTION, PotionType.STRONG_POISON));
        set(p, 14, pot(Material.SPLASH_POTION, PotionType.STRONG_POISON));
        set(p, 15, pot(Material.SPLASH_POTION, PotionType.WEAKNESS));
        set(p, 16, pot(Material.SPLASH_POTION, PotionType.STRONG_SLOWNESS));
        set(p, 17, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        // Row 2 — defense + utility
        set(p, 18, new ItemStack(Material.GOLDEN_APPLE, 4));
        set(p, 19, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 20, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(p, 21, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 22, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 23, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 24, new ItemStack(Material.MILK_BUCKET));
        set(p, 25, shield());
        set(p, 26, new ItemStack(Material.COOKED_BEEF, 16));
    }

    // ────────────────────────────────────────────────────────────
    //  Kit 9 — PANTHEON  (everything, full 36 slots)
    // ────────────────────────────────────────────────────────────

    private static void givePantheon(Player p) {
        // Named Chaos God armor
        p.getInventory().setHelmet(named(Material.NETHERITE_HELMET,
                "§5§lHelm of the Chaos God",
                List.of("§7Protection IV  §7Respiration III  §7Aqua Affinity",
                        "§7Thorns III  §7Mending"),
                Enchantment.PROTECTION, 4, Enchantment.RESPIRATION, 3,
                Enchantment.AQUA_AFFINITY, 1, Enchantment.THORNS, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setChestplate(named(Material.NETHERITE_CHESTPLATE,
                "§5§lCuirass of the Chaos God",
                List.of("§7Protection IV  §7Thorns III  §7Mending"),
                Enchantment.PROTECTION, 4, Enchantment.THORNS, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setLeggings(named(Material.NETHERITE_LEGGINGS,
                "§5§lGreaves of the Chaos God",
                List.of("§7Protection IV  §7Swift Sneak III  §7Thorns III  §7Mending"),
                Enchantment.PROTECTION, 4, Enchantment.SWIFT_SNEAK, 3,
                Enchantment.THORNS, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setBoots(named(Material.NETHERITE_BOOTS,
                "§5§lSabatons of the Chaos God",
                List.of("§7Protection IV  §7Feather Falling IV  §7Depth Strider III",
                        "§7Thorns III  §7Mending"),
                Enchantment.PROTECTION, 4, Enchantment.FEATHER_FALLING, 4,
                Enchantment.DEPTH_STRIDER, 3, Enchantment.THORNS, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        p.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));

        // Hotbar — named weapons
        set(p, 0, named(Material.NETHERITE_SWORD, "§5§lChaos Blade",
                List.of("§7Sharpness V  §7Fire Aspect II  §7Looting III",
                        "§7Knockback II  §7Sweeping Edge III  §7Mending"),
                Enchantment.SHARPNESS, 5, Enchantment.FIRE_ASPECT, 2,
                Enchantment.LOOTING, 3, Enchantment.KNOCKBACK, 2,
                Enchantment.SWEEPING_EDGE, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 1, named(Material.MACE, "§c§lOdin's Hammer",
                List.of("§7Density V  §7Breach IV  §7Wind Burst III  §7Mending"),
                Enchantment.DENSITY, 5, Enchantment.BREACH, 4,
                Enchantment.WIND_BURST, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 2, named(Material.TRIDENT, "§b§lSpear of Justice",
                List.of("§7Loyalty III  §7Channeling  §7Impaling V  §7Mending"),
                Enchantment.LOYALTY, 3, Enchantment.CHANNELING, 1,
                Enchantment.IMPALING, 5, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 3, named(Material.BOW, "§e§lHeaven's Arrow",
                List.of("§7Power V  §7Flame I  §7Infinity  §7Punch II"),
                Enchantment.POWER, 5, Enchantment.FLAME, 1,
                Enchantment.INFINITY, 1, Enchantment.PUNCH, 2, Enchantment.UNBREAKING, 3));
        set(p, 4, named(Material.NETHERITE_AXE, "§6§lBlade of Reckoning",
                List.of("§7Sharpness V  §7Efficiency V  §7Fortune III  §7Mending"),
                Enchantment.SHARPNESS, 5, Enchantment.EFFICIENCY, 5,
                Enchantment.FORTUNE, 3, Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 5, named(Material.NETHERITE_PICKAXE, "§7§lChaos Pickaxe",
                List.of("§7Efficiency V  §7Fortune III  §7Mending"),
                Enchantment.EFFICIENCY, 5, Enchantment.FORTUNE, 3,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 6, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 4));
        set(p, 7, new ItemStack(Material.TOTEM_OF_UNDYING, 4));
        set(p, 8, new ItemStack(Material.ENDER_PEARL, 16));

        // Row 1 — secondary weapons + key support
        set(p,  9, new ItemStack(Material.ARROW, 1));
        set(p, 10, named(Material.CROSSBOW, "§d§lCrossbow of Chaos",
                List.of("§7Quick Charge III  §7Piercing IV  §7Mending"),
                Enchantment.QUICK_CHARGE, 3, Enchantment.PIERCING, 4,
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 11, named(Material.SHIELD, "§5§lShield of the Chaos God",
                List.of("§7Unbreaking III  §7Mending"),
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1));
        set(p, 12, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 13, pot(Material.POTION, PotionType.STRONG_SWIFTNESS));
        set(p, 14, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 15, pot(Material.POTION, PotionType.LONG_INVISIBILITY));
        set(p, 16, pot(Material.POTION, PotionType.LONG_SLOW_FALLING));
        set(p, 17, new ItemStack(Material.COOKED_BEEF, 16));

        // Row 2 — elytra swap + fireworks + utility
        set(p, 18, noBreak(named(Material.ELYTRA, "§5§lWings of the Chaos God",
                List.of("§7Equip to replace chestplate.", "§7Unbreakable  §7Mending"),
                Enchantment.UNBREAKING, 3, Enchantment.MENDING, 1)));
        set(p, 19, chaosFireworks());
        set(p, 20, new ItemStack(Material.WATER_BUCKET));
        set(p, 21, new ItemStack(Material.MILK_BUCKET));
        set(p, 22, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 23, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 24, new ItemStack(Material.OBSIDIAN, 8));
        set(p, 25, new ItemStack(Material.ENDER_PEARL, 8));
        set(p, 26, new ItemStack(Material.GOLDEN_APPLE, 4));

        // Row 3 — deep backup
        set(p, 27, new ItemStack(Material.TOTEM_OF_UNDYING, 2));
        set(p, 28, pot(Material.POTION, PotionType.STRONG_STRENGTH));
        set(p, 29, pot(Material.POTION, PotionType.STRONG_REGENERATION));
        set(p, 30, new ItemStack(Material.WIND_CHARGE, 16));
        set(p, 31, new ItemStack(Material.WIND_CHARGE, 16));
        set(p, 32, pot(Material.POTION, PotionType.LONG_FIRE_RESISTANCE));
        set(p, 33, pot(Material.SPLASH_POTION, PotionType.STRONG_HARMING));
        set(p, 34, new ItemStack(Material.COOKED_BEEF, 16));
        set(p, 35, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
    }

    // ────────────────────────────────────────────────────────────
    //  Special items
    // ────────────────────────────────────────────────────────────

    private static ItemStack chaosFireworks() {
        ItemStack fw = new ItemStack(Material.FIREWORK_ROCKET, 16);
        FireworkMeta m = (FireworkMeta) fw.getItemMeta();
        m.addEffect(FireworkEffect.builder()
                .withColor(Color.PURPLE, Color.FUCHSIA, Color.YELLOW)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BURST)
                .trail(true)
                .flicker(true)
                .build());
        m.setPower(3);
        fw.setItemMeta(m);
        return fw;
    }

    // ────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────

    private static void set(Player p, int slot, ItemStack item) {
        p.getInventory().setItem(slot, item);
    }

    private static ItemStack enc(ItemStack item, Object... pairs) {
        ItemMeta m = item.getItemMeta();
        for (int i = 0; i < pairs.length; i += 2) {
            m.addEnchant((Enchantment) pairs[i], (Integer) pairs[i + 1], true);
        }
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack named(Material mat, String name, List<String> lore, Object... pairs) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        m.setLore(lore);
        for (int i = 0; i < pairs.length; i += 2) {
            m.addEnchant((Enchantment) pairs[i], (Integer) pairs[i + 1], true);
        }
        item.setItemMeta(m);
        return item;
    }

    private static ItemStack pot(Material mat, PotionType type) {
        ItemStack item = new ItemStack(mat);
        PotionMeta m = (PotionMeta) item.getItemMeta();
        m.setBasePotionType(type);
        item.setItemMeta(m);
        return item;
    }

    /** Đánh dấu item không bao giờ mất durability (dùng cho elytra). */
    private static ItemStack noBreak(ItemStack item) {
        ItemMeta m = item.getItemMeta();
        m.setUnbreakable(true);
        item.setItemMeta(m);
        return item;
    }
}
