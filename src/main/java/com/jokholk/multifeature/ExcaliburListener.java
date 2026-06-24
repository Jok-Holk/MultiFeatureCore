package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
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

    // Mau sac firework khi charge
    private static final Color C1 = Color.fromRGB(139, 0, 0);
    private static final Color C2 = Color.fromRGB(80,  0, 0);

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
    protected void onChargeVisual(Player p, double ratio) {
        // Spawn 1 firework quay quanh player theo hinh tron
        double angle = (System.currentTimeMillis() / 200.0) % (2 * Math.PI);
        double r = 1.5 + ratio;
        Location loc = p.getLocation().clone().add(
                Math.cos(angle) * r,
                1.0,
                Math.sin(angle) * r
        );
        spawnFirework(loc, C1, C2, FireworkEffect.Type.BURST, false);
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double width        = 1 + 4 * ratio;
        double length       = 10 + 90 * ratio;
        int    hits         = Math.max(1, (int)(1 + 3 * ratio));
        double damagePerHit = (20 + 60 * ratio) / hits;

        if (chargedSecs >= 9.5) {
            Bukkit.broadcastMessage("§4§l⚔ §c§lEXCALIBUR!!! §4§l⚔");
        }

        for (int i = 0; i < hits; i++) {
            final int waveIndex   = i;
            final double finalW   = width;
            final double finalL   = length;
            final double finalDmg = damagePerHit;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!p.isOnline()) return;
                beamDamage(p, finalL, finalW, finalDmg);
                // Spawn 3 fireworks doc theo beam
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
        Location eye = source.getEyeLocation();
        Vector dir   = eye.getDirection().normalize();
        Set<LivingEntity> targets = new HashSet<>();

        // Quet doc beam, moi 2 block lay entities xung quanh
        int steps = (int)(length / 2.0);
        for (int s = 1; s <= steps; s++) {
            Location point = eye.clone().add(dir.clone().multiply(s * 2.0));
            point.getWorld().getNearbyEntities(point, width, width, width).stream()
                    .filter(e -> e instanceof LivingEntity && e != source)
                    .map(e -> (LivingEntity) e)
                    .forEach(targets::add);
        }

        for (LivingEntity target : targets) {
            target.damage(damage, source);
            if (target instanceof Player victim && victim.getGameMode() == GameMode.SURVIVAL) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (victim.isOnline()) {
                        victim.kickPlayer("§4The darkness devours you.");
                    }
                }, 1L);
            }
        }
    }
}