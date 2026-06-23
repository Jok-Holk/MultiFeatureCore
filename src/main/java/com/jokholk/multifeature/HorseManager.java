package com.jokholk.multifeature;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class HorseManager {

    public record Breed(EntityType entityType, Horse.Color color, String label) {}

    static final Map<String, Breed> BREEDS = new LinkedHashMap<>();
    static final Map<String, Material> ARMORS = new LinkedHashMap<>();

    static {
        BREEDS.put("white",      new Breed(EntityType.HORSE, Horse.Color.WHITE,      "White Horse"));
        BREEDS.put("creamy",     new Breed(EntityType.HORSE, Horse.Color.CREAMY,     "Creamy Horse"));
        BREEDS.put("chestnut",   new Breed(EntityType.HORSE, Horse.Color.CHESTNUT,   "Chestnut Horse"));
        BREEDS.put("brown",      new Breed(EntityType.HORSE, Horse.Color.BROWN,      "Brown Horse"));
        BREEDS.put("black",      new Breed(EntityType.HORSE, Horse.Color.BLACK,      "Black Horse"));
        BREEDS.put("gray",       new Breed(EntityType.HORSE, Horse.Color.GRAY,       "Gray Horse"));
        BREEDS.put("dark_brown", new Breed(EntityType.HORSE, Horse.Color.DARK_BROWN, "Dark Brown Horse"));
        BREEDS.put("skeleton",   new Breed(EntityType.SKELETON_HORSE, null, "Skeleton Horse"));
        BREEDS.put("zombie",     new Breed(EntityType.ZOMBIE_HORSE,   null, "Zombie Horse"));
        BREEDS.put("donkey",     new Breed(EntityType.DONKEY,         null, "Donkey"));
        BREEDS.put("mule",       new Breed(EntityType.MULE,           null, "Mule"));

        ARMORS.put("leather",   Material.LEATHER_HORSE_ARMOR);
        ARMORS.put("iron",      Material.IRON_HORSE_ARMOR);
        ARMORS.put("gold",      Material.GOLDEN_HORSE_ARMOR);
        ARMORS.put("diamond",   Material.DIAMOND_HORSE_ARMOR);
        ARMORS.put("netherite", Material.NETHERITE_HORSE_ARMOR);
    }

    // ownerUUID → horseEntityUUID  /  horseEntityUUID → ownerUUID
    private static final Map<UUID, UUID> ownerToHorse = new HashMap<>();
    private static final Map<UUID, UUID> horseToOwner = new HashMap<>();

    private record PendingSpawn(Breed breed, Material armor, String customName, long expiry) {}
    private static final Map<UUID, PendingSpawn> pending = new HashMap<>();
    private static final long EXPIRY_MS = 30_000L;

    // ── Public query ──────────────────────────────────────────────────────────

    public static UUID getOwner(UUID horseId)       { return horseToOwner.get(horseId); }
    public static UUID getPlayerHorse(UUID ownerId) { return ownerToHorse.get(ownerId); }

    public static void removeHorse(UUID horseId) {
        UUID owner = horseToOwner.remove(horseId);
        if (owner != null) ownerToHorse.remove(owner);
    }

    // ── Spawn flow ────────────────────────────────────────────────────────────

    /**
     * Tries to spawn. Returns false and queues confirmation if player already has an active horse.
     */
    public static boolean trySpawn(Player p, String breedKey, String armorKey, String customName, Server server) {
        Breed breed = BREEDS.get(breedKey.toLowerCase());
        Material armorMat = armorKey != null ? ARMORS.getOrDefault(armorKey.toLowerCase(), Material.NETHERITE_HORSE_ARMOR)
                                             : Material.NETHERITE_HORSE_ARMOR;

        UUID existingId = ownerToHorse.get(p.getUniqueId());
        if (existingId != null) {
            Entity existing = server.getEntity(existingId);
            if (existing != null && existing.isValid()) {
                pending.put(p.getUniqueId(),
                        new PendingSpawn(breed, armorMat, customName, System.currentTimeMillis() + EXPIRY_MS));
                sendConfirmMessage(p, breed, customName);
                return false;
            }
            // Stale entry — clean up and fall through
            horseToOwner.remove(existingId);
            ownerToHorse.remove(p.getUniqueId());
        }

        doSpawn(p, breed, armorMat, customName);
        return true;
    }

    public static void confirmSpawn(Player p, Server server) {
        PendingSpawn ps = pending.get(p.getUniqueId());
        if (ps == null) {
            p.sendMessage("§cNo pending horse spawn. Use §f/horse §cto request one first.");
            return;
        }
        if (System.currentTimeMillis() > ps.expiry()) {
            pending.remove(p.getUniqueId());
            p.sendMessage("§cHorse spawn request expired. Use §f/horse §cagain.");
            return;
        }
        pending.remove(p.getUniqueId());

        // Dismiss existing
        UUID existingId = ownerToHorse.remove(p.getUniqueId());
        if (existingId != null) {
            Entity existing = server.getEntity(existingId);
            if (existing != null) existing.remove();
            horseToOwner.remove(existingId);
        }

        doSpawn(p, ps.breed(), ps.armor(), ps.customName());
    }

    public static void cancelSpawn(Player p) {
        if (pending.remove(p.getUniqueId()) != null) {
            p.sendMessage("§cHorse spawn cancelled.");
        } else {
            p.sendMessage("§cNo pending horse spawn.");
        }
    }

    public static void dismiss(Player p, Server server) {
        UUID horseId = ownerToHorse.remove(p.getUniqueId());
        if (horseId == null) {
            p.sendMessage("§cYou don't have an active horse.");
            return;
        }
        Entity horse = server.getEntity(horseId);
        if (horse != null) horse.remove();
        horseToOwner.remove(horseId);
        p.sendMessage("§aYour horse has been dismissed.");
    }

    // ── Internal spawn ────────────────────────────────────────────────────────

    private static void doSpawn(Player p, Breed breed, Material armorMat, String customName) {
        Location loc = p.getLocation();
        Entity entity = loc.getWorld().spawnEntity(loc, breed.entityType());
        entity.setPersistent(true);

        if (entity instanceof AbstractHorse ah) {
            ah.setTamed(true);
            ah.setOwner(p);

            // Max stats — armor attribute set to netherite level regardless of visual item
            setAttr(ah, Attribute.MAX_HEALTH,       30.0);
            setAttr(ah, Attribute.MOVEMENT_SPEED,   0.3375);
            setAttr(ah, Attribute.ARMOR,            13.0);
            setAttr(ah, Attribute.ARMOR_TOUGHNESS,  3.0);
            setAttr(ah, Attribute.JUMP_STRENGTH,    1.0);
            ah.setHealth(30.0);

            // Saddle for all horse types
            ah.getInventory().setSaddle(new ItemStack(Material.SADDLE));

            // Color + armor only for standard horses
            if (ah instanceof Horse horse) {
                horse.setColor(breed.color() != null ? breed.color() : Horse.Color.WHITE);
                horse.setStyle(Horse.Style.NONE);
                horse.getInventory().setArmor(makeArmorItem(armorMat));
            }
        }

        if (customName != null && !customName.isBlank()) {
            entity.setCustomName("§6" + customName);
            entity.setCustomNameVisible(true);
        }

        ownerToHorse.put(p.getUniqueId(), entity.getUniqueId());
        horseToOwner.put(entity.getUniqueId(), p.getUniqueId());

        p.sendMessage("§a" + breed.label() + " summoned!");
    }

    /** Horse armor item: cosmetic material, protection driven by GENERIC_ARMOR attribute. */
    private static ItemStack makeArmorItem(Material mat) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setUnbreakable(true);
        m.addEnchant(Enchantment.UNBREAKING, 3, true);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(m);
        return item;
    }

    private static void setAttr(LivingEntity e, Attribute attr, double val) {
        AttributeInstance inst = e.getAttribute(attr);
        if (inst != null) inst.setBaseValue(val);
    }

    private static void sendConfirmMessage(Player p, Breed breed, String customName) {
        p.sendMessage("§7──────────────────────────────");
        p.sendMessage("§eYou already have an active horse!");
        p.sendMessage("§7New: §f" + breed.label()
                + (customName != null && !customName.isBlank() ? " §7(\"§f" + customName + "§7\")" : ""));

        Component buttons = Component.text()
                .append(Component.text("  "))
                .append(Component.text("[Confirm]")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/horse confirm"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Dismiss current horse and spawn new one"))))
                .append(Component.text("  |  ").color(NamedTextColor.DARK_GRAY))
                .append(Component.text("[Cancel]")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/horse cancel"))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Keep your current horse"))))
                .build();
        p.sendMessage(buttons);
        p.sendMessage("§7──────────────────────────────");
    }
}
