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

public class IgnisListener extends DivineWeaponListener {

    static final double MAX_CHARGE  = 8.0;
    static final double MAX_RADIUS  = 12.0;
    static final double MAX_LENGTH  = 60.0;
    static final double MAX_DAMAGE  = 40.0;

    private static final Color C1 = Color.fromRGB(255, 100, 0);
    private static final Color C2 = Color.fromRGB(255, 200, 0);


    public IgnisListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE) return false;
        if (!item.hasItemMeta()) return false;
        return IgnisCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
    protected double getCdMultiplier()  { return 0.5; }

    @Override
    protected String getTheftKickMessage(Player victim) {
        return Msg.IGNIS_KICK_THEFT.get(victim);
    }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        World  world = p.getWorld();
        double angle = (System.currentTimeMillis() / 100.0) % (2 * Math.PI); // fast spin
        // 5-point FLAME ring, tightening as charge rises
        for (int i = 0; i < 5; i++) {
            double a = angle + i * 2 * Math.PI / 5;
            double r = 1.0 + ratio * 0.8;
            Location loc = p.getLocation().clone().add(Math.cos(a) * r, 0.8, Math.sin(a) * r);
            world.spawnParticle(Particle.FLAME, loc, 3, 0.04, 0.08, 0.04, 0.03);
        }
        // Lava drips at high charge
        if (ratio > 0.3) {
            world.spawnParticle(Particle.DRIPPING_LAVA, p.getEyeLocation(), 2, 0.3, 0.3, 0.3, 0);
        }
        if (ratio > 0.7) {
            world.spawnParticle(Particle.LAVA, p.getLocation().clone().add(0, 0.5, 0), 3, 0.6, 0.2, 0.6, 0);
        }
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double radius = 2 + 10 * ratio;
        double length = 10 + 50 * ratio;
        double damage = 10 + 30 * ratio;

        Location eye = p.getEyeLocation();
        Vector   dir = eye.getDirection().normalize();
        double   r2  = radius * radius;
        World    world = eye.getWorld();

        int ri    = (int) Math.ceil(radius);
        int steps = (int) length;
        Location tip = eye.clone().add(dir.clone().multiply(length));

        int minX = (int) Math.floor(Math.min(eye.getX(), tip.getX()) - ri);
        int maxX = (int) Math.ceil( Math.max(eye.getX(), tip.getX()) + ri);
        int minY = (int) Math.floor(Math.min(eye.getY(), tip.getY()) - ri);
        int maxY = (int) Math.ceil( Math.max(eye.getY(), tip.getY()) + ri);
        int minZ = (int) Math.floor(Math.min(eye.getZ(), tip.getZ()) - ri);
        int maxZ = (int) Math.ceil( Math.max(eye.getZ(), tip.getZ()) + ri);

        // Pha blocks: duyet bbox, kiem tra khoang cach den axis
        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    Vector toBlock = new Vector(bx - eye.getX(), by - eye.getY(), bz - eye.getZ());
                    double proj = toBlock.dot(dir);
                    if (proj < 0 || proj > length) continue;
                    double perpDist2 = toBlock.lengthSquared() - proj * proj;
                    if (perpDist2 > r2) continue;
                    breakBlockSilent(world.getBlockAt(bx, by, bz));
                }
            }
        }

        // ─── SOUNDS ───
        world.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.6f);
        world.playSound(p.getLocation(), Sound.BLOCK_LAVA_AMBIENT,  0.8f, 1.2f);

        // ─── CYLINDER PARTICLES ───
        for (int s = 1; s <= steps; s += 2) {
            Location pt = eye.clone().add(dir.clone().multiply(s));
            world.spawnParticle(Particle.FLAME,            pt, 6, (float)radius*0.2f, 0.2f, (float)radius*0.2f, 0.04f);
            world.spawnParticle(Particle.SMALL_FLAME,      pt, 4, (float)radius*0.3f, 0.3f, (float)radius*0.3f, 0.01f);
            world.spawnParticle(Particle.DRIPPING_LAVA,    pt, 2, (float)radius*0.2f, 0.2f, (float)radius*0.2f, 0);
            if (s % 10 == 0) {
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pt, 3, 0.5f, 0.5f, 0.5f, 0.02f);
                spawnFirework(pt, C1, C2, FireworkEffect.Type.BURST, false);
            }
        }

        // ─── ENTITY QUERY ───
        Location midPt  = eye.clone().add(dir.clone().multiply(length / 2.0));
        double halfLen  = length / 2.0 + 1;
        Set<LivingEntity> targets = new HashSet<>();
        world.getNearbyEntities(midPt, halfLen + ri, ri, halfLen + ri).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        for (LivingEntity target : targets) {
            target.damage(damage, p);
            target.setFireTicks(100);
            target.getWorld().spawnParticle(Particle.FLAME,
                    target.getLocation().clone().add(0, 1, 0), 15, 0.4, 0.4, 0.4, 0.08);
        }

        p.sendMessage(Msg.IGNIS_CAST.get(p));
    }
}
