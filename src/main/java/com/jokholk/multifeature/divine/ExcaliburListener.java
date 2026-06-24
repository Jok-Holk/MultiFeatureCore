package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExcaliburListener extends DivineWeaponListener {

    static final double MAX_CHARGE    = 10.0;
    static final double MAX_WIDTH     = 5.0;
    static final double MAX_LENGTH    = 100.0;
    static final double MAX_TOTAL_DMG = 80.0;
    static final int    MAX_HITS      = 4;

    private static final Color C1 = Color.fromRGB(139, 0,  0);
    private static final Color C2 = Color.fromRGB(40,  0,  0);

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

    @Override
    protected double getMaxChargeSecs() { return MAX_CHARGE; }

    @Override
    protected double getCdMultiplier()  { return 0.3; }

    @Override
    protected String getTheftKickMessage(Player victim) {
        return Msg.EXCALIBUR_KICK_THEFT.get(victim);
    }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World world = p.getWorld();
        double angle = (System.currentTimeMillis() / 150.0) % (2 * Math.PI);
        double r = 1.0 + ratio * 1.5;
        // 3-point soul fire spiral
        for (int i = 0; i < 3; i++) {
            double a = angle + i * 2 * Math.PI / 3;
            Location loc = p.getLocation().clone().add(Math.cos(a) * r, 1.0, Math.sin(a) * r);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 2, 0.05, 0.05, 0.05, 0.01);
        }
        // Soul wisps rising
        world.spawnParticle(Particle.SOUL, p.getLocation().clone().add(0, 0.5, 0), 3, 0.4, 0.3, 0.4, 0.02);
        // Ink puffs at high charge
        if (ratio > 0.5) {
            world.spawnParticle(Particle.SQUID_INK, p.getEyeLocation(), 1, 0.15, 0.15, 0.15, 0);
        }
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double width        = 1 + 4 * ratio;
        double length       = 10 + 90 * ratio;
        int    hits         = Math.max(1, (int)(1 + 3 * ratio));
        double damagePerHit = (20 + 60 * ratio) / hits;

        if (chargedSecs >= 9.5) {
            for (Player viewer : org.bukkit.Bukkit.getOnlinePlayers()) {
                viewer.sendMessage(Msg.EXCALIBUR_BROADCAST.get(viewer));
            }
        }

        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.8f, 0.7f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_SHOOT,   1.0f, 0.5f);
        p.sendMessage(Msg.EXCALIBUR_CAST.get(p));

        for (int i = 0; i < hits; i++) {
            final int    waveIndex = i;
            final double finalW    = width;
            final double finalL    = length;
            final double finalDmg  = damagePerHit;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!p.isOnline()) return;
                beamDamage(p, finalL, finalW, finalDmg);
                Vector dir = p.getEyeLocation().getDirection().normalize();
                for (int j = 1; j <= 3; j++) {
                    double dist = finalL * (j / 4.0);
                    Location fwLoc = p.getEyeLocation().clone().add(dir.clone().multiply(dist));
                    spawnFirework(fwLoc, C1, C2, FireworkEffect.Type.BURST, false);
                }
            }, waveIndex * 10L);
        }
    }

    private void beamDamage(Player source, double length, double width, double damage) {
        Location eye  = source.getEyeLocation();
        Vector   dir  = eye.getDirection().normalize();
        World    world = eye.getWorld();
        Set<LivingEntity> targets = new HashSet<>();

        int steps = (int)(length / 2.0);
        for (int s = 1; s <= steps; s++) {
            Location point = eye.clone().add(dir.clone().multiply(s * 2.0));
            // Dark beam particles along path
            world.spawnParticle(Particle.SOUL,       point, 4, width * 0.3, 0.3, width * 0.3, 0.02);
            world.spawnParticle(Particle.SCULK_SOUL, point, 2, 0.4, 0.4, 0.4, 0.04);

            world.getNearbyEntities(point, width, width, width).stream()
                    .filter(e -> e instanceof LivingEntity && e != source)
                    .map(e -> (LivingEntity) e)
                    .forEach(targets::add);
        }

        for (LivingEntity target : targets) {
            target.damage(damage, source);
            // Ink burst on each hit
            target.getWorld().spawnParticle(Particle.SQUID_INK,
                    target.getLocation().clone().add(0, 1, 0), 12, 0.4, 0.4, 0.4, 0.03);
            target.getWorld().spawnParticle(Particle.SOUL,
                    target.getLocation().clone().add(0, 1, 0), 8, 0.5, 0.5, 0.5, 0.05);

            if (target instanceof Player victim && victim.getGameMode() == GameMode.SURVIVAL) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (victim.isOnline()) {
                        victim.kickPlayer(Msg.EXCALIBUR_KICK_HIT.get(victim));
                    }
                }, 1L);
            }
        }
    }
}
