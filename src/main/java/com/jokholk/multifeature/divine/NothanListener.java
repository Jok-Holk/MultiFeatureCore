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
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NothanListener extends DivineWeaponListener {

    static final double MAX_CHARGE    = 4.0;
    static final double MAX_CONE_HALF = 45.0; // degrees
    static final double MAX_RANGE     = 25.0;
    static final double MAX_DAMAGE    = 30.0;

    private static final Color C1 = Color.fromRGB(255, 200, 30);
    private static final Color C2 = Color.fromRGB(255, 140, 0);

    // cos(45°) = 0.707
    private static final double COS_MIN = Math.cos(Math.toRadians(MAX_CONE_HALF));

    public NothanListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.CROSSBOW) return false;
        if (!item.hasItemMeta()) return false;
        return NothanCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
    protected double getCdMultiplier()  { return 0.8; }

    @Override
    protected String getTheftKickMessage(Player victim) {
        return Msg.NOTHAN_KICK_THEFT.get(victim);
    }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World  world = p.getWorld();
        Vector dir   = p.getEyeLocation().getDirection().normalize();

        // Golden ENCHANT sparks radiating in a forward cone
        int sparks = 4 + (int)(ratio * 8);
        for (int i = 0; i < sparks; i++) {
            double spread = 0.7 * (1 - ratio * 0.4); // cone tightens
            Vector v = dir.clone()
                    .add(new Vector(
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread))
                    .normalize().multiply(1.5 + ratio);
            Location pLoc = p.getEyeLocation().clone().add(v);
            world.spawnParticle(Particle.ENCHANT, pLoc, 2, 0.05, 0.05, 0.05, 0.05);
        }
        // CRIT particles at muzzle
        world.spawnParticle(Particle.CRIT, p.getEyeLocation().clone().add(dir.clone().multiply(1.5)),
                3, 0.1, 0.1, 0.1, 0.1);
        // Golden glow ring at high charge
        if (ratio > 0.5) {
            double r = 0.8;
            double angle0 = (System.currentTimeMillis() / 200.0) % (2 * Math.PI);
            for (int i = 0; i < 8; i++) {
                double a = angle0 + i * 2 * Math.PI / 8;
                Location loc = p.getLocation().clone().add(Math.cos(a) * r, 1.0, Math.sin(a) * r);
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double range  = 10 + 15 * ratio;
        double damage = 10 + 20 * ratio;
        int    sickTicks = (int)(40 + 80 * ratio);

        Location eye  = p.getEyeLocation();
        Vector   look = eye.getDirection().normalize();
        World    world = eye.getWorld();

        // ─── MUZZLE BLAST VFX ───
        Location muzzle = eye.clone().add(look.clone().multiply(1.5));
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, muzzle, 60, 0.6, 0.6, 0.6, 0.5);
        world.spawnParticle(Particle.ENCHANT,          muzzle, 40, 0.8, 0.8, 0.8, 0.4);
        world.spawnParticle(Particle.CRIT,             muzzle, 25, 0.5, 0.5, 0.5, 0.3);
        // Cone particle stream
        int streamPts = 12 + (int)(ratio * 8);
        for (int i = 0; i < streamPts; i++) {
            double spread = 0.5 * (i / (double)streamPts);
            double dist   = range * (i / (double)streamPts);
            Vector sv = look.clone()
                    .add(new Vector(
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread))
                    .normalize().multiply(dist);
            Location pt = eye.clone().add(sv);
            world.spawnParticle(Particle.TOTEM_OF_UNDYING, pt, 3, 0.2, 0.2, 0.2, 0.08);
            world.spawnParticle(Particle.ENCHANT,          pt, 2, 0.15, 0.15, 0.15, 0.05);
        }

        // ─── Firework at muzzle ───
        spawnFirework(muzzle, C1, C2, FireworkEffect.Type.BURST, true);

        // ─── SOUNDS ───
        world.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER,      0.9f, 0.7f);
        world.playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.3f);
        world.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f, 0.8f);

        // ─── CONE ENTITY QUERY ───
        // Đặt query sphere bao gồm toàn bộ cone
        Set<LivingEntity> targets = new HashSet<>();
        world.getNearbyEntities(eye, range, range, range).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .filter(e -> {
                    Vector toEnt = e.getLocation().toVector().subtract(eye.toVector()).normalize();
                    double dot = look.dot(toEnt);
                    return dot >= COS_MIN;
                })
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        for (LivingEntity target : targets) {
            target.damage(damage, p);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  sickTicks, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,  sickTicks, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA,    sickTicks, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,   160,       0));

            // Per-entity hit VFX
            Location tLoc = target.getLocation().clone().add(0, 1, 0);
            tLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, tLoc, 25, 0.4, 0.6, 0.4, 0.35);
            tLoc.getWorld().spawnParticle(Particle.ENCHANT,          tLoc, 15, 0.3, 0.4, 0.3, 0.2);
            tLoc.getWorld().spawnParticle(Particle.CRIT,             tLoc, 10, 0.3, 0.4, 0.3, 0.15);
            spawnFirework(tLoc.clone().add(0, 0.5, 0), C1, C2, FireworkEffect.Type.STAR, false);
        }

        p.sendMessage(Msg.NOTHAN_CAST.fmt(p, "count", targets.size()));
    }
}
