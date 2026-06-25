package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VoidBowListener extends DivineWeaponListener {

    static final double MAX_CHARGE       = 5.0;
    static final int    MAX_ARROWS       = 25;
    static final double BASE_RANGE       = 30.0;
    static final double MAX_RANGE        = 80.0;
    static final double MAX_AOE_RADIUS   = 10.0;
    static final double BASE_ARROW_DMG   = 8.0;
    static final double MAX_ARROW_DMG    = 12.0;

    private static final Color C1 = Color.fromRGB(30,   30,  160);
    private static final Color C2 = Color.fromRGB(180, 180, 255);

    // Tracking fired arrows for trail effect
    private final Map<UUID, BukkitTask> arrowTrails = new ConcurrentHashMap<>();

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
    protected String getTheftKickMessage(Player victim) {
        return Msg.VOID_KICK_THEFT.get(victim);
    }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World  world = p.getWorld();
        // Expanding END_ROD ring around player
        double r = 1.5 + ratio * 2.0;
        int    pts = 8 + (int)(ratio * 8); // 8 → 16 points
        double angle0 = (System.currentTimeMillis() / 280.0) % (2 * Math.PI);
        for (int i = 0; i < pts; i++) {
            double a = angle0 + i * 2 * Math.PI / pts;
            Location loc = p.getLocation().clone().add(Math.cos(a) * r, 1.0, Math.sin(a) * r);
            world.spawnParticle(Particle.END_ROD, loc, 1, 0, 0, 0, 0);
        }
        // PORTAL swirl at center
        world.spawnParticle(Particle.PORTAL, p.getLocation().clone().add(0, 1, 0),
                6, 0.5, 0.5, 0.5, 0.3);
        // REVERSE_PORTAL rising at high charge
        if (ratio > 0.6) {
            world.spawnParticle(Particle.REVERSE_PORTAL, p.getLocation().clone().add(0, 0.5, 0),
                    4, 0.4, 0.4, 0.4, 0.15);
        }
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        int    arrowCount = (int)(5 + 20 * ratio);
        double range      = BASE_RANGE + 50 * ratio;
        double aoeRadius  = 3 + 7 * ratio;
        double arrowDmg   = 24 + 36 * ratio; // 24 → 60 per arrow

        Location eye  = p.getEyeLocation();
        Vector   dir  = eye.getDirection().normalize();
        World    world = p.getWorld();

        // Ray-cast to target point
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
        if (!hitSolid) target = eye.clone().add(dir.clone().multiply(range));

        final Location targetPoint = target;

        // ─── RING PORTAL VFX ───
        Location ringCenter = eye.clone().add(dir.clone().multiply(2.0));
        // 16-point END_ROD ring
        for (int i = 0; i < 16; i++) {
            double a = i * 2 * Math.PI / 16;
            Location pLoc = ringCenter.clone().add(Math.cos(a) * 1.8, Math.sin(a) * 1.8, 0);
            world.spawnParticle(Particle.END_ROD, pLoc, 2, 0, 0, 0, 0);
        }
        world.spawnParticle(Particle.PORTAL,         ringCenter, 25, 0.5, 0.5, 0.5, 0.5);
        world.spawnParticle(Particle.REVERSE_PORTAL, ringCenter, 10, 0.3, 0.3, 0.3, 0.2);
        spawnFirework(ringCenter, C1, C2, FireworkEffect.Type.STAR, true);

        // ─── SOUNDS ───
        world.playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT,   0.9f, 0.5f);
        world.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT,   0.7f, 0.8f);

        // ─── FIRE ARROWS after 3 ticks ───
        final Location finalRingCenter = ringCenter;
        final int      finalArrows     = arrowCount;
        final double   finalDmg        = arrowDmg;
        final double   finalAoe        = aoeRadius;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) return;
            Set<UUID> firedArrows = new HashSet<>();
            for (int i = 0; i < finalArrows; i++) {
                double spreadX = (Math.random() - 0.5) * 2 * finalAoe * 0.1;
                double spreadZ = (Math.random() - 0.5) * 2 * finalAoe * 0.1;

                Vector arrowDir = targetPoint.toVector()
                        .subtract(finalRingCenter.toVector())
                        .add(new Vector(spreadX, 0, spreadZ));

                if (arrowDir.lengthSquared() > 0.001) {
                    arrowDir.normalize().multiply(6.0);
                } else {
                    arrowDir = dir.clone().multiply(6.0);
                }

                final Vector finalVelocity = arrowDir;

                @SuppressWarnings("unchecked")
                Arrow arrow = (Arrow) world.spawnEntity(
                        finalRingCenter, EntityType.ARROW,
                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                        entity -> {
                            Arrow a = (Arrow) entity;
                            a.setDamage(finalDmg);
                            a.setShooter(p);
                            a.setVelocity(finalVelocity);
                            a.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                        }
                );
                firedArrows.add(arrow.getUniqueId());
                startArrowTrail(arrow);
            }
        }, 3L);

        p.sendMessage("§9§l✦ §3Stars fall. §9§l✦");
    }

    private void startArrowTrail(Arrow arrow) {
        UUID id = arrow.getUniqueId();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid()) {
                    arrowTrails.remove(id);
                    cancel();
                    return;
                }
                Location loc = arrow.getLocation();
                loc.getWorld().spawnParticle(Particle.END_ROD,        loc, 2, 0.05, 0.05, 0.05, 0.01);
                loc.getWorld().spawnParticle(Particle.PORTAL,         loc, 3, 0.1,  0.1,  0.1,  0.15);
                loc.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc, 1, 0.05, 0.05, 0.05, 0.05);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        arrowTrails.put(id, task);
    }
}
