package com.jokholk.multifeature;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IgnisListener extends DivineWeaponListener {

    static final double MAX_CHARGE  = 8.0;
    static final double MAX_RADIUS  = 12.0;
    static final double MAX_LENGTH  = 60.0;
    static final double MAX_DAMAGE  = 40.0;

    private static final Color C1 = Color.fromRGB(255, 100, 0);
    private static final Color C2 = Color.fromRGB(255, 200, 0);

    // Cac loai block bi pha boi Ignis (stone-tier)
    private static final Set<Material> STONE_BLOCKS = Set.of(
            Material.STONE, Material.COBBLESTONE,
            Material.GRANITE, Material.POLISHED_GRANITE,
            Material.DIORITE, Material.POLISHED_DIORITE,
            Material.ANDESITE, Material.POLISHED_ANDESITE,
            Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
            Material.GRAVEL,
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE
    );

    public IgnisListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE) return false;
        if (!item.hasItemMeta()) return false;
        return IgnisCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
    }

    @Override
    protected boolean isOwner(Player p, ItemStack item) {
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        return lore.get(lore.size() - 1).contains(p.getUniqueId().toString());
    }

    @Override
    protected double getMaxChargeSecs() { return MAX_CHARGE; }

    @Override
    protected double getCdMultiplier()  { return 0.5; }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        double angle = (System.currentTimeMillis() / 250.0) % (2 * Math.PI);
        double r = 1.5 + ratio;
        Location loc = p.getLocation().clone().add(
                Math.cos(angle) * r, 1.0, Math.sin(angle) * r
        );
        spawnFirework(loc, C1, C2, FireworkEffect.Type.BURST, false);
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double radius  = 2 + 10 * ratio;   // 2-12
        double length  = 10 + 50 * ratio;  // 10-60
        double damage  = 10 + 30 * ratio;  // 10-40

        Location eye = p.getEyeLocation();
        Vector   dir = eye.getDirection().normalize();

        Set<LivingEntity> targets = new HashSet<>();
        int steps = (int) length;

        for (int s = 1; s <= steps; s++) {
            Location center = eye.clone().add(dir.clone().multiply(s));

            // Pha cac block stone-tier trong cylinder
            int ri = (int) Math.ceil(radius);
            for (int dx = -ri; dx <= ri; dx++) {
                for (int dy = -ri; dy <= ri; dy++) {
                    for (int dz = -ri; dz <= ri; dz++) {
                        if (dx*dx + dy*dy + dz*dz <= radius*radius) {
                            Block block = center.clone().add(dx, dy, dz).getBlock();
                            if (STONE_BLOCKS.contains(block.getType())) {
                                breakBlockSilent(block);
                            }
                        }
                    }
                }
            }

            // Particle lua
            center.getWorld().spawnParticle(Particle.FLAME, center, 5, 0.3, 0.3, 0.3, 0.05);

            // Thu thap entities
            center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                    .filter(e -> e instanceof LivingEntity && e != p)
                    .map(e -> (LivingEntity) e)
                    .forEach(targets::add);

            // Firework moi 10 buoc
            if (s % 10 == 0) {
                spawnFirework(center, C1, C2, FireworkEffect.Type.BURST, false);
            }
        }

        for (LivingEntity target : targets) {
            target.damage(damage, p);
            target.setFireTicks(100);
        }

        p.sendMessage("§6§lIGNIS §7core discharged!");
    }
}
