package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RagnarokListener extends DivineWeaponListener {

    static final double MAX_CHARGE      = 5.0;
    static final double MAX_HALF_WIDTH  = 25.0;
    static final double MAX_DEPTH       = 20.0;
    static final double MAX_DAMAGE      = 40.0;

    private static final Color  C1 = Color.fromRGB(200, 50,  0);
    private static final Color  C2 = Color.fromRGB(255, 100, 0);
    private static final Vector UP  = new Vector(0, 1, 0);

    public RagnarokListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_AXE) return false;
        if (!item.hasItemMeta()) return false;
        return RagnarokCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
    protected double getCdMultiplier()  { return 1.0; }

    @Override
    protected String getTheftKickMessage(Player victim) {
        return Msg.RAGNAROK_KICK_THEFT.get(victim);
    }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World  world = p.getWorld();
        double angle = (System.currentTimeMillis() / 120.0) % (2 * Math.PI);
        Location base = p.getLocation().clone().add(0, 0.5, 0);
        // 4-point FLAME ring spinning fast
        for (int i = 0; i < 4; i++) {
            double a = angle + i * Math.PI / 2;
            double r = 1.2 + ratio * 0.8;
            Location loc = base.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r);
            world.spawnParticle(Particle.FLAME, loc, 3, 0.04, 0.08, 0.04, 0.025);
        }
        if (ratio > 0.4) {
            world.spawnParticle(Particle.END_ROD, p.getLocation().clone().add(0, 1, 0),
                    2, 0.6, 0.6, 0.6, 0.1);
        }
        if (ratio > 0.75) {
            world.spawnParticle(Particle.ELECTRIC_SPARK, p.getLocation().clone().add(0, 1, 0),
                    5, 0.9, 0.9, 0.9, 0.2);
        }
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double halfWidth = 5 + 20 * ratio;
        double depth     = 5 + 15 * ratio;
        double damage    = 15 + 25 * ratio;

        Vector forward = p.getLocation().getDirection();
        forward.setY(0);
        if (forward.lengthSquared() < 0.001) forward = new Vector(1, 0, 0);
        forward.normalize();

        final Vector fwd   = forward;
        final Vector right = fwd.crossProduct(UP).normalize();

        Location feet = p.getLocation();
        World    world = feet.getWorld();
        Set<LivingEntity> targets = new HashSet<>();

        int hw = (int) Math.ceil(halfWidth);
        int d  = (int) Math.ceil(depth);

        // Pha blocks: duyet bbox cua vung quet
        int minX = (int) Math.floor(Math.min(
                feet.getX() - hw * Math.abs(right.getX()) - d * Math.abs(fwd.getX()),
                feet.getX() + hw * Math.abs(right.getX()) + d * Math.abs(fwd.getX()))) - 1;
        int maxX = (int) Math.ceil(Math.max(
                feet.getX() - hw * Math.abs(right.getX()) - d * Math.abs(fwd.getX()),
                feet.getX() + hw * Math.abs(right.getX()) + d * Math.abs(fwd.getX()))) + 1;
        int minZ = (int) Math.floor(Math.min(
                feet.getZ() - hw * Math.abs(right.getZ()) - d * Math.abs(fwd.getZ()),
                feet.getZ() + hw * Math.abs(right.getZ()) + d * Math.abs(fwd.getZ()))) - 1;
        int maxZ = (int) Math.ceil(Math.max(
                feet.getZ() - hw * Math.abs(right.getZ()) - d * Math.abs(fwd.getZ()),
                feet.getZ() + hw * Math.abs(right.getZ()) + d * Math.abs(fwd.getZ()))) + 1;
        int baseY = feet.getBlockY();

        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                Vector toBlock = new Vector(bx - feet.getX(), 0, bz - feet.getZ());
                double fwdDot  = toBlock.dot(fwd);
                double sideDot = Math.abs(toBlock.dot(right));
                if (fwdDot < 0 || fwdDot > depth || sideDot > halfWidth) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    breakBlockSilent(world.getBlockAt(bx, baseY + dy, bz));
                }
            }
        }

        // Entity query: 1 lan duy nhat
        Location sweepCenter = feet.clone().add(fwd.clone().multiply(depth / 2.0));
        double queryHalfLen = depth / 2.0 + 2;
        world.getNearbyEntities(sweepCenter, queryHalfLen, 3, queryHalfLen).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .filter(e -> {
                    Vector rel = e.getLocation().toVector().subtract(feet.toVector());
                    rel.setY(0);
                    double fd = rel.dot(fwd);
                    double sd = Math.abs(rel.dot(right));
                    return fd >= 0 && fd <= depth + 1 && sd <= halfWidth + 1;
                })
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        // ─── SOUNDS ───
        world.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.85f);
        world.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT,  0.8f, 0.7f);

        // ─── SWEEP PARTICLE WALL ───
        int pSteps = (int)(depth / 3);
        for (int s = 0; s <= pSteps; s++) {
            Location centerPt = feet.clone().add(fwd.clone().multiply(s * 3.0));
            for (int si = -(int)halfWidth; si <= (int)halfWidth; si += 3) {
                Location pt = centerPt.clone().add(right.clone().multiply(si));
                world.spawnParticle(Particle.FLAME,       pt, 4, 0.2, 0.6, 0.2, 0.03);
                world.spawnParticle(Particle.LARGE_SMOKE, pt, 2, 0.2, 0.6, 0.2, 0.01);
            }
        }
        // Electric sparks scattered across zone
        for (int i = 0; i < 16; i++) {
            double sideR = (Math.random() * 2 - 1) * halfWidth;
            double fwdR  = Math.random() * depth;
            Location sp = feet.clone().add(fwd.clone().multiply(fwdR))
                    .add(right.clone().multiply(sideR)).add(0, 1, 0);
            world.spawnParticle(Particle.ELECTRIC_SPARK, sp, 3, 0.3, 0.5, 0.3, 0.1);
        }

        // ─── DAMAGE + HIT VFX ───
        for (LivingEntity target : targets) {
            target.damage(damage, p);
            target.getWorld().spawnParticle(Particle.END_ROD,
                    target.getLocation().clone().add(0, 1, 0), 10, 0.3, 0.5, 0.3, 0.12);
            target.getWorld().spawnParticle(Particle.FLAME,
                    target.getLocation().clone().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.06);
        }

        // ─── FIREWORKS along sweep arc ───
        int fwCount = Math.max(3, (int)(halfWidth / 5));
        for (int i = 0; i < fwCount; i++) {
            double sideOff = -halfWidth + (2 * halfWidth * i / (fwCount - 1 + 0.001));
            Location fwLoc = feet.clone()
                    .add(fwd.clone().multiply(depth / 2.0))
                    .add(right.clone().multiply(sideOff))
                    .add(0, 1, 0);
            spawnFirework(fwLoc, C1, C2, FireworkEffect.Type.STAR, false);
        }

        p.sendMessage(Msg.RAGNAROK_CAST.get(p));
    }
}
