package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ExcaliburListener extends DivineWeaponListener {

    static final double MAX_CHARGE  = 10.0;
    static final double MAX_RADIUS  = 25.0;

    private static final Color C1 = Color.fromRGB(139, 0,  0);
    private static final Color C2 = Color.fromRGB(40,  0,  0);

    // UUIDs of entities that should drop nothing when killed by the slam
    private final Set<UUID> slamKillTargets = new HashSet<>();

    public ExcaliburListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SWORD) return false;
        if (!item.hasItemMeta()) return false;
        return ExcaliburCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
    }

    @Override
    protected boolean isOwner(Player p, ItemStack item) {
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        return lore.get(lore.size() - 1).contains(p.getUniqueId().toString());
    }

    @Override protected double getMaxChargeSecs() { return MAX_CHARGE; }
    @Override protected double getCdMultiplier()  { return 0.3; }
    @Override protected String getTheftKickMessage(Player victim) { return Msg.EXCALIBUR_KICK_THEFT.get(victim); }

    // ─── Charge visual: dark blade rising upward ───

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World    world = p.getWorld();
        Location feet  = p.getLocation();
        double pillarH = 2.0 + 9.0 * ratio;     // blade rises from 2 → 11 blocks high
        double angle   = (System.currentTimeMillis() / 130.0) % (2 * Math.PI);

        // Orbiting dark souls spiraling upward along the pillar
        int layers = (int)(4 + ratio * 10); // 4 → 14 layers
        for (int lay = 0; lay < layers; lay++) {
            double h  = (lay / (double) layers) * pillarH;
            double r  = 0.5 - (lay / (double) layers) * 0.3; // thinner at top: 0.5 → 0.2
            double a1 = angle + h * 0.8;
            double a2 = angle + h * 0.8 + Math.PI;
            world.spawnParticle(Particle.SOUL,
                    feet.clone().add(Math.cos(a1) * r, h, Math.sin(a1) * r), 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.SOUL,
                    feet.clone().add(Math.cos(a2) * r, h, Math.sin(a2) * r), 1, 0, 0, 0, 0);
        }

        // Dark ash rising at base
        world.spawnParticle(Particle.ASH, feet.clone().add(0, 0.3, 0), 4, 0.4, 0.1, 0.4, 0.02);

        if (ratio > 0.4) {
            world.spawnParticle(Particle.SOUL_FIRE_FLAME,
                    feet.clone().add(0, 0.2, 0), 2, 0.5, 0.05, 0.5, 0.01);
        }

        if (ratio > 0.6) {
            // Tip spark: ink explosion at blade top
            Location tip = feet.clone().add(0, pillarH, 0);
            world.spawnParticle(Particle.SQUID_INK, tip, 3, 0.25, 0.25, 0.25, 0.06);
            world.spawnParticle(Particle.SCULK_SOUL, tip, 2, 0.15, 0.15, 0.15, 0.04);
        }

        if (ratio > 0.8) {
            world.spawnParticle(Particle.SOUL_FIRE_FLAME,
                    feet.clone().add(0, pillarH * 0.5, 0), 3, 0.3, pillarH * 0.15, 0.3, 0.02);
            world.playSound(feet, Sound.ENTITY_WITHER_AMBIENT, 0.25f, 1.5f);
        }
    }

    // ─── Cast: dark slam — expanding shockwave, no drops ───

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        World    world  = p.getWorld();
        Location origin = p.getLocation();
        double   maxR   = 5 + 20 * ratio; // 5 → 25 blocks

        if (chargedSecs >= 9.5) {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.sendMessage(Msg.EXCALIBUR_BROADCAST.get(viewer));
            }
        }

        // Pillar collapse: downward burst
        double pillarH = 2.0 + 9.0 * ratio;
        for (int i = 0; i < 80; i++) {
            double h = Math.random() * pillarH;
            double r = Math.random() * 1.2;
            double a = Math.random() * 2 * Math.PI;
            world.spawnParticle(Particle.SQUID_INK,
                    origin.clone().add(Math.cos(a) * r, h, Math.sin(a) * r), 1, 0, 0.05, 0, 0.08);
        }

        // Strike lightning at impact
        world.strikeLightningEffect(origin);

        // Sounds
        world.playSound(origin, Sound.ENTITY_WITHER_DEATH,            0.9f, 0.5f);
        world.playSound(origin, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,  1.0f, 0.7f);
        world.playSound(origin, Sound.ENTITY_GENERIC_EXPLODE,         0.9f, 0.6f);

        // Expanding shockwave ring over 15 ticks
        new BukkitRunnable() {
            int t = 0;
            final int TOTAL = 15;
            final Set<UUID> hit = new HashSet<>();

            @Override
            public void run() {
                if (t >= TOTAL) { cancel(); return; }

                double ringR = maxR * (t + 1.0) / TOTAL;
                int    pts   = Math.max(20, (int)(ringR * 7));

                // Ring at ground level
                for (int i = 0; i < pts; i++) {
                    double a   = i * 2 * Math.PI / pts;
                    Location rLoc = origin.clone().add(Math.cos(a) * ringR, 0.1, Math.sin(a) * ringR);
                    world.spawnParticle(Particle.SOUL,       rLoc, 4, 0.1, 0.15, 0.1, 0.02);
                    world.spawnParticle(Particle.ASH,        rLoc, 3, 0.1, 0.35, 0.1, 0.03);
                    if (i % 5 == 0) {
                        world.spawnParticle(Particle.SCULK_SOUL,
                                rLoc.clone().add(0, 0.5, 0), 1, 0, 0, 0, 0);
                    }
                }

                // Hit entities inside ring radius
                world.getNearbyEntities(origin, ringR + 1.5, 3, ringR + 1.5).stream()
                        .filter(e -> e instanceof LivingEntity && e != p)
                        .filter(e -> !hit.contains(e.getUniqueId()))
                        .filter(e -> e.getLocation().distance(origin) <= ringR + 1.5)
                        .map(e -> (LivingEntity) e)
                        .forEach(target -> {
                            hit.add(target.getUniqueId());
                            slamKillTargets.add(target.getUniqueId());
                            target.setNoDamageTicks(0);
                            target.damage(9999.0, p); // obliterate

                            Location tLoc = target.getLocation().clone().add(0, 1, 0);
                            world.spawnParticle(Particle.SOUL,       tLoc, 25, 0.5, 0.8, 0.5, 0.06);
                            world.spawnParticle(Particle.SQUID_INK,  tLoc, 15, 0.4, 0.6, 0.4, 0.05);
                            world.spawnParticle(Particle.SCULK_SOUL, tLoc, 10, 0.3, 0.4, 0.3, 0.06);
                            world.strikeLightningEffect(target.getLocation());
                            spawnFirework(tLoc.clone().add(0, 0.5, 0), C1, C2, FireworkEffect.Type.BURST, false);

                            if (target instanceof Player victim
                                    && victim.getGameMode() == GameMode.SURVIVAL) {
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    if (victim.isOnline()) victim.kickPlayer(Msg.EXCALIBUR_KICK_HIT.get(victim));
                                }, 1L);
                            }
                        });

                // Periodic firework at ring edge
                if (t % 4 == 0) {
                    double a  = Math.random() * 2 * Math.PI;
                    Location fw = origin.clone().add(Math.cos(a) * ringR, 1, Math.sin(a) * ringR);
                    spawnFirework(fw, C1, C2, FireworkEffect.Type.BURST, false);
                }

                t++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        p.sendMessage(Msg.EXCALIBUR_CAST.get(p));
    }

    // Suppress drops for entities obliterated by the slam
    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (slamKillTargets.remove(e.getEntity().getUniqueId())) {
            e.getDrops().clear();
            e.setDroppedExp(0);
        }
    }
}
