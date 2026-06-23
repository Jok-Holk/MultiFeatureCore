package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;

public class VoidBowListener extends DivineWeaponListener {

    static final double MAX_CHARGE       = 5.0;
    static final int    MAX_ARROWS       = 25;
    static final double BASE_RANGE       = 30.0;
    static final double MAX_RANGE        = 80.0;
    static final double MAX_AOE_RADIUS   = 10.0;
    static final double BASE_ARROW_DMG   = 8.0;
    static final double MAX_ARROW_DMG    = 12.0;

    private static final Color C1 = Color.fromRGB(50,  50,  200);
    private static final Color C2 = Color.fromRGB(200, 200, 255);

    public VoidBowListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.BOW) return false;
        if (!item.hasItemMeta()) return false;
        return VoidBowCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
    protected void onChargeVisual(Player p, double ratio) {
        double angle = (System.currentTimeMillis() / 200.0) % (2 * Math.PI);
        double r = 2.0;
        Location loc = p.getLocation().clone().add(
                Math.cos(angle) * r, 1.0, Math.sin(angle) * r
        );
        spawnFirework(loc, C1, C2, FireworkEffect.Type.STAR, false);
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        int    arrowCount = (int)(5 + 20 * ratio);          // 5-25
        double range      = BASE_RANGE + 50 * ratio;        // 30-80
        double aoeRadius  = 3 + 7 * ratio;                  // 3-10
        double arrowDmg   = BASE_ARROW_DMG + 4 * ratio;     // 8-12

        // Tim diem muc tieu: ray cast doc theo huong nhin
        Location eye  = p.getEyeLocation();
        Vector   dir  = eye.getDirection().normalize();
        Location target = eye.clone();
        boolean hitSolid = false;

        for (int step = 1; step <= (int) range; step++) {
            Location check = eye.clone().add(dir.clone().multiply(step));
            if (check.getBlock().getType().isSolid()) {
                target = check;
                hitSolid = true;
                break;
            }
            target = check;
        }
        if (!hitSolid) {
            target = eye.clone().add(dir.clone().multiply(range));
        }

        final Location targetPoint = target;
        World world = p.getWorld();

        // Spawn ring particles (END_ROD) 2 block phia truoc mat player
        Location ringCenter = eye.clone().add(dir.clone().multiply(2.0));
        for (int i = 0; i < 8; i++) {
            double a = i * 2 * Math.PI / 8;
            Location particleLoc = ringCenter.clone().add(
                    Math.cos(a) * 1.5, Math.sin(a) * 1.5, 0
            );
            world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
        }

        // Spawn firework ring chinh
        spawnFirework(ringCenter, C1, C2, FireworkEffect.Type.STAR, false);

        final Location finalRingCenter = ringCenter;
        final int      finalArrows     = arrowCount;
        final double   finalDmg        = arrowDmg;
        final double   finalAoe        = aoeRadius;

        // Sau 3 ticks: ban mui ten
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (int i = 0; i < finalArrows; i++) {
                // Spread ngau nhien
                double spreadX = (Math.random() - 0.5) * 2 * finalAoe * 0.1;
                double spreadZ = (Math.random() - 0.5) * 2 * finalAoe * 0.1;

                Vector arrowDir = targetPoint.toVector()
                        .subtract(finalRingCenter.toVector())
                        .add(new Vector(spreadX, 0, spreadZ));

                if (arrowDir.lengthSquared() > 0.001) {
                    arrowDir.normalize().multiply(3.0);
                } else {
                    arrowDir = dir.clone().multiply(3.0);
                }

                final Vector finalVelocity = arrowDir;

                @SuppressWarnings("unchecked")
                Arrow arrow = (Arrow) world.spawnEntity(
                        finalRingCenter,
                        EntityType.ARROW,
                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                        entity -> {
                            Arrow a = (Arrow) entity;
                            a.setDamage(finalDmg);
                            a.setShooter(p);
                            a.setVelocity(finalVelocity);
                            a.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                        }
                );
            }
        }, 3L);
    }
}
