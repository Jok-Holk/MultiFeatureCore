package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;

public class RagnarokListener extends DivineWeaponListener {

    static final double MAX_CHARGE      = 5.0;
    static final double MAX_HALF_WIDTH  = 8.0;  // full width 8 -> 16
    static final double MAX_DEPTH       = 8.0;  // depth 3 -> 8
    static final double MAX_DAMAGE      = 40.0;
    private static final double MIN_COOLDOWN = 6.0;

    // Box height: baseY + MIN_DY .. baseY + MAX_DY, shared by block
    // destruction, the entity hitbox and the particle fill so the zone is an
    // actual 3D box, not a flat rectangle painted on the ground. Starts
    // exactly at the player's feet (dy=0 is the feet block, "height 1") and
    // reaches 9 blocks above that -- 10 blocks tall total.
    private static final int MIN_DY = 0;
    private static final int MAX_DY = 9;

    // Log blocks give their real wood item (type-correct, via getDrops so any
    // future enchant like Silk Touch is respected) straight into the
    // player's inventory instead of just vanishing.
    private void breakBlockWithDrops(Player p, Block block, ItemStack tool) {
        if (Tag.LOGS.isTagged(block.getType())) {
            for (ItemStack drop : block.getDrops(tool)) {
                giveOrDrop(p, drop);
            }
        }
        breakBlockSilent(block);
    }

    private void giveOrDrop(Player p, ItemStack stack) {
        for (ItemStack extra : p.getInventory().addItem(stack).values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), extra);
        }
    }

    private static final Color  C1  = Color.fromRGB(255, 80,  0);
    private static final Color  C2  = Color.fromRGB(255, 200, 0);
    private static final Vector UP  = new Vector(0, 1, 0);

    public RagnarokListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_AXE) return false;
        return DivineId.is(item, RagnarokCommand.ID);
    }

    @Override
    protected boolean isOwner(Player p, ItemStack item) {
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        return lore.get(lore.size() - 1).contains(p.getUniqueId().toString());
    }

    @Override protected double getMaxChargeSecs()   { return MAX_CHARGE; }
    @Override protected double getCdMultiplier()    { return 1.0; }
    @Override protected double getMinCooldownSecs() { return MIN_COOLDOWN; }
    @Override protected String getTheftKickMessage(Player victim) { return Msg.RAGNAROK_KICK_THEFT.get(victim); }

    // ─── Charge visual: storm builds up around player ───

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World    world = p.getWorld();
        double   angle = (System.currentTimeMillis() / 100.0) % (2 * Math.PI);
        Location base  = p.getLocation().clone().add(0, 0.5, 0);

        // Inner spinning flame ring
        int pts = (int)(4 + ratio * 8);
        for (int i = 0; i < pts; i++) {
            double a = angle + i * 2 * Math.PI / pts;
            double r = 1.0 + ratio * 1.5;
            world.spawnParticle(Particle.FLAME,
                    base.clone().add(Math.cos(a) * r, 0, Math.sin(a) * r),
                    3, 0.05, 0.12, 0.05, 0.04);
        }

        // Counter-rotating electric ring
        if (ratio > 0.3) {
            for (int i = 0; i < 6; i++) {
                double a = -angle * 1.5 + i * Math.PI / 3;
                double r = 1.8 + ratio * 1.2;
                world.spawnParticle(Particle.ELECTRIC_SPARK,
                        base.clone().add(Math.cos(a) * r, 0.5, Math.sin(a) * r),
                        1, 0.05, 0.1, 0.05, 0.06);
            }
        }

        // Upward column of fire at high charge
        if (ratio > 0.55) {
            double colH = ratio * 4.0;
            world.spawnParticle(Particle.FLAME,
                    base.clone().add(0, colH * 0.5, 0), 4, 0.4, colH * 0.25, 0.4, 0.03);
            world.spawnParticle(Particle.END_ROD,
                    base.clone().add(0, colH, 0), 2, 0.5, 0.1, 0.5, 0.08);
        }

        if (ratio > 0.8) {
            world.spawnParticle(Particle.LARGE_SMOKE,
                    base.clone().add(0, 1, 0), 3, 0.7, 0.5, 0.7, 0.02);
            world.playSound(base, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.2f, 0.6f);
        }
    }

    // ─── Cast: one simple rectangle directly in front of the player ───
    // No more animated multi-tick sweep across a wide arc -- that was both
    // the source of a persistent "looks diagonal" visual complaint and far
    // more complex than this skill needs. Just a small, fixed-shape zone
    // straight ahead, instant block destruction + entity damage, one burst
    // of effects.

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double halfWidth = 4 + 4 * ratio; // 4 → 8 (full width 8 → 16)
        double depth     = 3 + 5 * ratio; // 3 → 8 blocks deep, forward only
        double damage    = 20 + 20 * ratio; // 20 → 40 damage

        Vector forward = p.getLocation().getDirection();
        forward.setY(0);
        if (forward.lengthSquared() < 0.001) forward = new Vector(1, 0, 0);
        forward.normalize();

        final Vector fwd   = forward;
        // fwd.crossProduct(UP) would MUTATE fwd in place and return that same
        // object (Bukkit's Vector methods mutate `this` and return it) --
        // making `right` alias `fwd` and silently destroying the real forward
        // direction. clone() first so fwd keeps its own value. This was the
        // actual root cause of the long-standing "sweep looks diagonal/off to
        // the right" complaint: fd and sd ended up computed from the same
        // (right-only) vector, producing a strip along the true forward axis
        // but offset sideways instead of a rectangle in front of the player.
        final Vector right = fwd.clone().crossProduct(UP).normalize();

        Location feet  = p.getLocation();
        World    world = feet.getWorld();
        int      baseY = feet.getBlockY();
        ItemStack axeStack = p.getInventory().getItemInMainHand();

        double queryHalf = Math.max(halfWidth, depth) + 2;

        // ─── Block destruction: scan the small bounding box, test each ───
        // ─── candidate block's LOCAL (fwd/right) coordinates against the shape, ───
        // ─── a full 3D box from baseY+MIN_DY to baseY+MAX_DY, not a flat plane. ───
        int minBx = (int) Math.floor(feet.getX() - queryHalf);
        int maxBx = (int) Math.ceil( feet.getX() + queryHalf);
        int minBz = (int) Math.floor(feet.getZ() - queryHalf);
        int maxBz = (int) Math.ceil( feet.getZ() + queryHalf);
        for (int bx = minBx; bx <= maxBx; bx++) {
            for (int bz = minBz; bz <= maxBz; bz++) {
                double dx = (bx + 0.5) - feet.getX();
                double dz = (bz + 0.5) - feet.getZ();
                double fd = dx * fwd.getX()   + dz * fwd.getZ();
                double sd = dx * right.getX() + dz * right.getZ();
                if (fd < 0 || fd > depth || sd < -halfWidth || sd > halfWidth) continue;
                for (int dy = MIN_DY; dy <= MAX_DY; dy++) {
                    breakBlockWithDrops(p, world.getBlockAt(bx, baseY + dy, bz), axeStack);
                }
            }
        }

        // ─── Impact sounds ───
        world.playSound(feet, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.7f);
        world.playSound(feet, Sound.ENTITY_LIGHTNING_BOLT_IMPACT,  0.9f, 0.6f);
        world.playSound(feet, Sound.ENTITY_GENERIC_EXPLODE,        0.8f, 0.8f);

        // ─── One-shot particle burst filling the whole box (width x depth x ───
        // ─── height), not just a flat layer painted on the ground ───
        int fSteps = 5;
        int sSteps = 5;
        double[] yLayers = {
            MIN_DY + 0.2,
            MIN_DY + (MAX_DY - MIN_DY) / 3.0,
            MIN_DY + (MAX_DY - MIN_DY) * 2.0 / 3.0,
            MAX_DY - 0.2
        };
        for (double yOff : yLayers) {
            for (int i = 0; i <= fSteps; i++) {
                double fPos = depth * i / fSteps;
                for (int j = -sSteps; j <= sSteps; j++) {
                    double sPos = halfWidth * j / sSteps;
                    Location pt = feet.clone()
                            .add(fwd.clone().multiply(fPos))
                            .add(right.clone().multiply(sPos))
                            .add(0, yOff, 0);
                    world.spawnParticle(Particle.FLAME, pt, 3, 0.2, 0.3, 0.2, 0.04);
                    if ((i + j) % 3 == 0) {
                        world.spawnParticle(Particle.ELECTRIC_SPARK, pt, 1, 0.15, 0.2, 0.15, 0.05);
                    }
                }
            }
        }
        for (int i = 0; i < 4; i++) {
            Location lLoc = feet.clone()
                    .add(fwd.clone().multiply(Math.random() * depth))
                    .add(right.clone().multiply((Math.random() * 2 - 1) * halfWidth));
            world.strikeLightningEffect(lLoc);
        }
        spawnFirework(feet.clone().add(fwd.clone().multiply(depth * 0.5)).add(0, 1, 0),
                C1, C2, FireworkEffect.Type.BURST, false);

        // ─── Entity damage: same box, instant ───
        world.getNearbyEntities(feet, queryHalf, MAX_DY - MIN_DY + 1, queryHalf).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .filter(e -> {
                    double relY = e.getLocation().getY() - baseY;
                    if (relY < MIN_DY - 0.5 || relY > MAX_DY + 1.5) return false;
                    Vector rel = e.getLocation().toVector().subtract(feet.toVector());
                    rel.setY(0);
                    double fd = rel.dot(fwd);
                    double sd = rel.dot(right);
                    return fd >= 0 && fd <= depth + 1 && sd >= -halfWidth - 1 && sd <= halfWidth + 1;
                })
                .map(e -> (LivingEntity) e)
                .forEach(target -> {
                    target.damage(damage, p);
                    target.setFireTicks(60);

                    Location tLoc = target.getLocation().clone().add(0, 1, 0);
                    target.getWorld().spawnParticle(Particle.FLAME,          tLoc, 20, 0.4, 0.7, 0.4, 0.10);
                    target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tLoc, 10, 0.3, 0.5, 0.3, 0.08);
                    target.getWorld().spawnParticle(Particle.END_ROD,        tLoc,  8, 0.3, 0.4, 0.3, 0.12);
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    spawnFirework(tLoc.clone().add(0, 0.5, 0), C1, C2, FireworkEffect.Type.STAR, false);
                });

        p.sendMessage(Msg.RAGNAROK_CAST.get(p));
    }
}
