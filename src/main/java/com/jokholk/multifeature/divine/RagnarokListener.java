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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RagnarokListener extends DivineWeaponListener {

    static final double MAX_CHARGE      = 5.0;
    static final double MAX_HALF_WIDTH  = 42.0;
    static final double MAX_DEPTH       = 5.0;
    static final double MAX_DAMAGE      = 120.0;

    private static final Color  C1  = Color.fromRGB(255, 80,  0);
    private static final Color  C2  = Color.fromRGB(255, 200, 0);
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

    @Override protected double getMaxChargeSecs() { return MAX_CHARGE; }
    @Override protected double getCdMultiplier()  { return 1.0; }
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

    // ─── Cast: animated left→right sweep across the zone ───

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        // A rectangle straight in front of the player: full width scales with
        // charge, depth stays short and mostly fixed, projecting forward only.
        double halfWidth = 12 + 30 * ratio; // 12 → 42 blocks wide on each side
        double depth     = 3 + 2 * ratio;   // 3 → 5 blocks deep, forward only
        double damage    = 40 + 80 * ratio; // 40 → 120 damage

        Vector forward = p.getLocation().getDirection();
        forward.setY(0);
        if (forward.lengthSquared() < 0.001) forward = new Vector(1, 0, 0);
        forward.normalize();

        final Vector fwd   = forward;
        final Vector right = fwd.crossProduct(UP).normalize();

        Location feet  = p.getLocation();
        World    world = feet.getWorld();

        // ─── Block destruction: scan the world-space bounding box, test each ───
        // ─── candidate block's LOCAL (fwd/right) coordinates against the shape ───
        // Stepping along the rotated fwd/right axes and rounding to the nearest
        // block (the old approach) leaves gaps whenever the player doesn't face
        // a cardinal direction, since consecutive rotated sample points can round
        // to non-adjacent blocks -- the classic rotated-line aliasing/staircase
        // artifact. Scanning the AABB and testing membership per-block has no
        // such gaps, same pattern already used correctly by Ignis's beam.
        int baseY = feet.getBlockY();
        double blockQueryHalfX = Math.abs(fwd.getX()) * depth + Math.abs(right.getX()) * halfWidth + 1;
        double blockQueryHalfZ = Math.abs(fwd.getZ()) * depth + Math.abs(right.getZ()) * halfWidth + 1;
        int minBx = (int) Math.floor(feet.getX() - blockQueryHalfX);
        int maxBx = (int) Math.ceil( feet.getX() + blockQueryHalfX);
        int minBz = (int) Math.floor(feet.getZ() - blockQueryHalfZ);
        int maxBz = (int) Math.ceil( feet.getZ() + blockQueryHalfZ);
        for (int bx = minBx; bx <= maxBx; bx++) {
            for (int bz = minBz; bz <= maxBz; bz++) {
                double dx = (bx + 0.5) - feet.getX();
                double dz = (bz + 0.5) - feet.getZ();
                double fd = dx * fwd.getX()   + dz * fwd.getZ();
                double sd = dx * right.getX() + dz * right.getZ();
                if (fd < 0 || fd > depth || sd < -halfWidth || sd > halfWidth) continue;
                for (int dy = -1; dy <= 2; dy++) {
                    breakBlockSilent(world.getBlockAt(bx, baseY + dy, bz));
                }
            }
        }

        // ─── Initial impact sounds ───
        world.playSound(feet, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.7f);
        world.playSound(feet, Sound.ENTITY_LIGHTNING_BOLT_IMPACT,  0.9f, 0.6f);
        world.playSound(feet, Sound.ENTITY_GENERIC_EXPLODE,        0.8f, 0.8f);

        // ─── Animated sweep: left → right over 20 ticks ───
        final int SWEEP_TICKS = 20;
        final Set<UUID> hit = new HashSet<>();

        // World-axis-aligned half-extents that fully contain the rotated
        // depth x (2*halfWidth) rectangle, regardless of player facing —
        // fixes entities being missed unless the player faced a cardinal
        // direction (getNearbyEntities' box is always world-axis-aligned).
        final double queryHalfX = Math.abs(fwd.getX()) * depth + Math.abs(right.getX()) * halfWidth + 2;
        final double queryHalfZ = Math.abs(fwd.getZ()) * depth + Math.abs(right.getZ()) * halfWidth + 2;

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= SWEEP_TICKS) { cancel(); return; }

                // Current sweep edge: from -halfWidth to +halfWidth
                double sweepSide = -halfWidth + (2.0 * halfWidth * (t + 1) / SWEEP_TICKS);
                // Thickness of the current slice (show a thick wall moving across)
                double sliceWidth = (2.0 * halfWidth) / SWEEP_TICKS * 3;

                // Spawn particles at the current sweep column (at every forward position)
                int colSteps = Math.max(8, (int)(depth / 2.5));
                for (int s = 0; s <= colSteps; s++) {
                    double fwdPos = (s / (double) colSteps) * depth;
                    Location colBase = feet.clone()
                            .add(fwd.clone().multiply(fwdPos))
                            .add(right.clone().multiply(sweepSide));

                    // Main FLAME wall
                    world.spawnParticle(Particle.FLAME,          colBase.clone().add(0, 0.1, 0), 6, 0.2, 0.8, 0.2, 0.06);
                    world.spawnParticle(Particle.LARGE_SMOKE,    colBase.clone().add(0, 1.5, 0), 4, 0.2, 0.6, 0.2, 0.02);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, colBase.clone().add(0, 1.0, 0), 3, 0.3, 0.5, 0.3, 0.08);
                    if (s % 3 == 0) {
                        world.spawnParticle(Particle.END_ROD,    colBase.clone().add(0, 2.5, 0), 2, 0.4, 0.4, 0.4, 0.10);
                    }
                }

                // Ground fire line sweeping with the wall
                for (int s = 0; s <= (int)(depth / 1.5); s++) {
                    double fwdPos = (s / (double)(depth / 1.5)) * depth;
                    Location gLoc = feet.clone()
                            .add(fwd.clone().multiply(fwdPos))
                            .add(right.clone().multiply(sweepSide))
                            .add(0, 0.05, 0);
                    world.spawnParticle(Particle.FLAME, gLoc, 2, 0.15, 0.05, 0.15, 0.02);
                }

                // Sweep lightning effect at front edge every 3 ticks
                if (t % 3 == 0) {
                    for (int fSteps = 0; fSteps < 5; fSteps++) {
                        double fwdPos = Math.random() * depth;
                        Location lLoc = feet.clone()
                                .add(fwd.clone().multiply(fwdPos))
                                .add(right.clone().multiply(sweepSide));
                        world.strikeLightningEffect(lLoc);
                    }
                    world.playSound(feet, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 0.9f);
                }

                // Firework at sweep edge (every 4 ticks)
                if (t % 4 == 0) {
                    double fwdPos = depth * 0.5 + (Math.random() - 0.5) * depth * 0.6;
                    Location fwLoc = feet.clone()
                            .add(fwd.clone().multiply(fwdPos))
                            .add(right.clone().multiply(sweepSide))
                            .add(0, 1.5, 0);
                    spawnFirework(fwLoc, C1, C2, FireworkEffect.Type.STAR, false);
                }

                // Hit entities in the current sweep slice. The broad-phase box is
                // centered on the player and sized to fully contain the rotated
                // sweep rectangle in world space; the precise fwd/right dot-product
                // filter below does the actual shape check.
                world.getNearbyEntities(feet, queryHalfX, 3, queryHalfZ).stream()
                        .filter(e -> e instanceof LivingEntity && e != p)
                        .filter(e -> !hit.contains(e.getUniqueId()))
                        .filter(e -> {
                            Vector rel = e.getLocation().toVector().subtract(feet.toVector());
                            rel.setY(0);
                            double fd  = rel.dot(fwd);
                            double sd  = rel.dot(right); // signed
                            return fd >= 0 && fd <= depth + 1
                                    && sd >= sweepSide - sliceWidth && sd <= sweepSide + 1;
                        })
                        .map(e -> (LivingEntity) e)
                        .forEach(target -> {
                            hit.add(target.getUniqueId());
                            target.damage(damage, p);
                            target.setFireTicks(60);

                            Location tLoc = target.getLocation().clone().add(0, 1, 0);
                            target.getWorld().spawnParticle(Particle.FLAME,          tLoc, 20, 0.4, 0.7, 0.4, 0.10);
                            target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tLoc, 10, 0.3, 0.5, 0.3, 0.08);
                            target.getWorld().spawnParticle(Particle.END_ROD,        tLoc,  8, 0.3, 0.4, 0.3, 0.12);
                            target.getWorld().strikeLightningEffect(target.getLocation());
                            spawnFirework(tLoc.clone().add(0, 0.5, 0), C1, C2, FireworkEffect.Type.STAR, false);
                        });

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        p.sendMessage(Msg.RAGNAROK_CAST.get(p));
    }
}
