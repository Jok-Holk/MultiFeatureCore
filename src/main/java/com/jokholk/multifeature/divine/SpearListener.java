package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SpearListener extends DivineWeaponListener {

    private static final double MAX_CHARGE   = 3.0;  // seconds
    private static final double CD_MULT      = 1.0;  // 1s cooldown per 1s charge
    private static final int    LUNGE_TICKS  = 28;   // how long we check path after launch
    private static final double HIT_RADIUS   = 2.5;  // blocks around player while lunging

    private static final Color C1 = Color.fromRGB(255, 215, 0);
    private static final Color C2 = Color.fromRGB(255, 160, 0);

    public SpearListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SPEAR) return false;
        if (!item.hasItemMeta()) return false;
        return SpearCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
    }

    @Override
    protected boolean isOwner(Player p, ItemStack item) {
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        return lore.get(lore.size() - 1).contains(p.getUniqueId().toString());
    }

    @Override protected double getMaxChargeSecs() { return MAX_CHARGE; }
    @Override protected double getCdMultiplier()  { return CD_MULT; }

    @Override
    protected String getTheftKickMessage(Player victim) {
        return Msg.SPEAR_KICK_THEFT.get(victim);
    }

    // ─── Charge visual (every 5 ticks) ───

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World world = p.getWorld();
        Location eye = p.getEyeLocation();
        Vector dir  = eye.getDirection().normalize();

        // Golden ENCHANT swirl — tightens as ratio increases
        int sparks = 5 + (int)(ratio * 10);
        double spread = 1.0 - ratio * 0.7;      // shrinks from 1.0 → 0.3
        for (int i = 0; i < sparks; i++) {
            Vector offset = new Vector(
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread,
                    (Math.random() - 0.5) * spread
            );
            world.spawnParticle(Particle.ENCHANT,
                    eye.clone().add(dir.clone().multiply(0.8 + ratio * 0.6)).add(offset),
                    2, 0.05, 0.05, 0.05, 0.15);
        }

        // CRIT dust around the spear tip
        world.spawnParticle(Particle.CRIT,
                eye.clone().add(dir.clone().multiply(1.2)), 3, 0.1, 0.1, 0.1, 0.2);

        // Golden ring orbiting the player at high charge
        if (ratio > 0.5) {
            double ringR = 0.9;
            double angle0 = (System.currentTimeMillis() / 150.0) % (2 * Math.PI);
            int pts = 8 + (int)(ratio * 4);
            for (int i = 0; i < pts; i++) {
                double a = angle0 + i * 2 * Math.PI / pts;
                Location ring = p.getLocation().clone()
                        .add(Math.cos(a) * ringR, 1.1, Math.sin(a) * ringR);
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, ring, 1, 0, 0, 0, 0);
            }
        }

        // Rising sound that escalates
        if (ratio > 0.3) {
            float pitch = 0.6f + (float)(ratio * 1.2f);
            world.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE,
                    0.4f + (float)(ratio * 0.4f), pitch);
        }
    }

    // ─── Release: lunge + path AoE ───

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        World world = p.getWorld();
        Location origin = p.getLocation();
        Vector dir = p.getEyeLocation().getDirection().normalize();

        // Launch VFX burst at feet
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, origin.clone().add(0, 1, 0), 50, 0.5, 0.6, 0.5, 0.5);
        world.spawnParticle(Particle.ENCHANT,          origin.clone().add(0, 1, 0), 35, 0.6, 0.6, 0.6, 0.5);
        world.spawnParticle(Particle.CRIT,             origin.clone().add(0, 1, 0), 20, 0.4, 0.4, 0.4, 0.4);
        world.playSound(origin, Sound.ITEM_TRIDENT_THROW,            1f, 0.7f + (float)(ratio * 0.5f));
        world.playSound(origin, Sound.ENTITY_PLAYER_ATTACK_SWEEP,    1f, 0.6f);

        // Lunge velocity: scales 1.6 → 3.4 with charge
        double power = 1.6 + 1.8 * ratio;
        p.setVelocity(dir.multiply(power).add(new Vector(0, 0.18, 0)));

        // Path AoE runnable
        double extraDamage = 15 + 30 * ratio;
        int slowTicks  = (int)(40 + 100 * ratio);  // 2s → 7s
        int blindTicks = (int)(30 +  70 * ratio);  // 1.5s → 5s

        Set<UUID> alreadyHit = new HashSet<>();

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!p.isOnline() || tick++ >= LUNGE_TICKS) {
                    cancel();
                    return;
                }

                Location cur = p.getLocation();

                // Trail along the path
                world.spawnParticle(Particle.ENCHANT, cur.clone().add(0, 1, 0),
                        8, 0.25, 0.25, 0.25, 0.35);
                world.spawnParticle(Particle.CRIT, cur.clone().add(0, 0.8, 0),
                        4, 0.2, 0.2, 0.2, 0.25);

                // Entities in radius
                for (Entity nearby : world.getNearbyEntities(cur, HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)) {
                    if (nearby.getUniqueId().equals(p.getUniqueId())) continue;
                    if (!(nearby instanceof LivingEntity target)) continue;
                    if (alreadyHit.contains(nearby.getUniqueId())) continue;
                    alreadyHit.add(nearby.getUniqueId());

                    // Damage (reset invulnerability so it lands even if recently hit)
                    target.setNoDamageTicks(0);
                    target.damage(extraDamage, p);

                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  slowTicks,  2));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindTicks, 0));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,   160,        0));

                    // Hit VFX
                    Location tLoc = target.getLocation().clone().add(0, 1, 0);
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, tLoc, 35, 0.5, 0.7, 0.5, 0.45);
                    world.spawnParticle(Particle.ENCHANT,          tLoc, 20, 0.4, 0.5, 0.4, 0.35);
                    world.spawnParticle(Particle.CRIT,             tLoc, 15, 0.4, 0.5, 0.4, 0.3);
                    world.playSound(tLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.4f);
                    world.playSound(tLoc, Sound.BLOCK_ANVIL_LAND,      0.4f, 1.6f);
                    spawnFirework(tLoc, C1, C2, FireworkEffect.Type.STAR, true);

                    // Kick SURVIVAL player
                    if (target instanceof Player victim
                            && victim.getGameMode() == GameMode.SURVIVAL) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (victim.isOnline()) victim.kickPlayer(Msg.SPEAR_KICK_HIT.get(victim));
                        }, 1L);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        p.sendMessage(Msg.SPEAR_CAST.get(p));
    }
}
