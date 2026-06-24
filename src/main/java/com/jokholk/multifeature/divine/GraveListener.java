package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraveListener extends DivineWeaponListener {

    static final double MAX_CHARGE  = 8.0;
    static final double MAX_RADIUS  = 20.0;
    static final double MAX_DEPTH   = 12.0;
    static final double MAX_DAMAGE  = 20.0;

    private static final Color C1 = Color.fromRGB(100, 0, 150);
    private static final Color C2 = Color.fromRGB(30,  0,  60);

    private static final Set<Material> SHOVEL_BLOCKS = Set.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT,
            Material.PODZOL, Material.MYCELIUM,
            Material.SAND, Material.RED_SAND,
            Material.GRAVEL, Material.CLAY,
            Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.SNOW, Material.SNOW_BLOCK,
            Material.MUD, Material.MUDDY_MANGROVE_ROOTS
    );

    public GraveListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SHOVEL) return false;
        if (!item.hasItemMeta()) return false;
        return GraveCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
    protected String getTheftKickMessage(Player victim) {
        return Msg.GRAVE_KICK_THEFT.get(victim);
    }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World  world = p.getWorld();
        double angle = (System.currentTimeMillis() / 250.0) % (2 * Math.PI);
        double r = 1.2 + ratio;
        // 3-point SOUL spiral rising
        for (int i = 0; i < 3; i++) {
            double a = angle + i * 2 * Math.PI / 3;
            Location loc = p.getLocation().clone().add(Math.cos(a) * r, 0.4 + ratio * 0.8, Math.sin(a) * r);
            world.spawnParticle(Particle.SOUL, loc, 2, 0.05, 0.1, 0.05, 0.01);
        }
        // ASH raining from above
        world.spawnParticle(Particle.ASH, p.getLocation().clone().add(0, 2.5, 0), 6, 0.9, 0.3, 0.9, 0.02);
        // Soul fire at high charge
        if (ratio > 0.5) {
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, p.getLocation().clone().add(0, 0.3, 0),
                    2, 0.3, 0.2, 0.3, 0.01);
        }
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double radius     = 3 + 17 * ratio;
        double depth      = 3 + 9 * ratio;
        int    witherTicks = (int)(60 + 100 * ratio);
        double damage     = 5 + 15 * ratio;

        Location center = p.getLocation();
        World    world  = center.getWorld();

        // ─── BREAK BLOCKS (circle downward) ───
        int ri = (int) Math.ceil(radius);
        int d  = (int) Math.ceil(depth);
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dz = -ri; dz <= ri; dz++) {
                if (dx*dx + dz*dz <= radius*radius) {
                    for (int dy = 0; dy >= -d; dy--) {
                        var block = center.clone().add(dx, dy, dz).getBlock();
                        if (SHOVEL_BLOCKS.contains(block.getType())) {
                            breakBlockSilent(block);
                        }
                    }
                }
            }
        }

        // ─── ENTITY QUERY ───
        Set<LivingEntity> targets = new HashSet<>();
        world.getNearbyEntities(center, radius, 3, radius).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        for (LivingEntity target : targets) {
            target.damage(damage, p);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherTicks, 2));
            // Per-entity hit: soul burst
            target.getWorld().spawnParticle(Particle.SOUL,
                    target.getLocation().clone().add(0, 1, 0), 12, 0.4, 0.5, 0.4, 0.04);
            target.getWorld().spawnParticle(Particle.SCULK_SOUL,
                    target.getLocation().clone().add(0, 1.5, 0), 6, 0.3, 0.3, 0.3, 0.06);
        }

        // ─── SOUNDS ───
        world.playSound(center, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 1.0f, 0.6f);
        world.playSound(center, Sound.ENTITY_WITHER_AMBIENT,         0.8f, 0.5f);

        // ─── AREA PARTICLES ───
        // SOUL scatter across circle
        for (int i = 0; i < 60; i++) {
            double angle  = Math.random() * 2 * Math.PI;
            double r2     = Math.random() * radius;
            Location loc  = center.clone().add(Math.cos(angle) * r2, 0.5, Math.sin(angle) * r2);
            world.spawnParticle(Particle.SOUL, loc, 2, 0.2, 0.4, 0.2, 0.02);
        }
        // ASH raining across circle
        for (int i = 0; i < 30; i++) {
            double angle  = Math.random() * 2 * Math.PI;
            double r2     = Math.random() * radius;
            Location loc  = center.clone().add(Math.cos(angle) * r2, 2.0, Math.sin(angle) * r2);
            world.spawnParticle(Particle.ASH, loc, 3, 0.4, 0.5, 0.4, 0.03);
        }
        // SCULK_SOUL wisps rising at edge
        int edgeCount = Math.max(6, (int)(radius));
        for (int i = 0; i < edgeCount; i++) {
            double angle = i * 2 * Math.PI / edgeCount;
            Location loc = center.clone().add(Math.cos(angle) * radius * 0.85, 0.2, Math.sin(angle) * radius * 0.85);
            world.spawnParticle(Particle.SCULK_SOUL, loc, 3, 0.2, 0.3, 0.2, 0.05);
        }

        // ─── FIREWORKS ring ───
        int fwCount = Math.max(4, (int)(4 + 4 * ratio));
        for (int i = 0; i < fwCount; i++) {
            double angle = i * 2 * Math.PI / fwCount;
            Location fwLoc = center.clone().add(
                    Math.cos(angle) * radius * 0.9, 1.0, Math.sin(angle) * radius * 0.9);
            spawnFirework(fwLoc, C1, C2, FireworkEffect.Type.BURST, false);
        }

        p.sendMessage(Msg.GRAVE_CAST.get(p));
    }
}
