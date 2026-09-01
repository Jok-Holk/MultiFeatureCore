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
    static final double MIN_RADIUS  = 0.5;
    static final double MAX_RADIUS  = 2.0;
    static final double MIN_LENGTH  = 2.5;
    static final double MAX_LENGTH  = 10.0;
    static final double MAX_DAMAGE  = 40.0;
    private static final double MIN_COOLDOWN = 5.0;

    private static final Color C1 = Color.fromRGB(255, 100, 0);
    private static final Color C2 = Color.fromRGB(255, 200, 0);

    private static final Set<Material> ORES = Set.of(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
        Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS
    );

    public IgnisListener(MainPlugin plugin) {
        super(plugin);
    }

    // Ore blocks give their real, already-processed mineral straight into the
    // player's inventory -- quantity comes from getDrops() so Fortune (or any
    // other enchant on the pickaxe) scales it exactly like manual mining
    // would. Raw ore is skipped entirely: Ignis is a fire weapon, so
    // iron/copper/gold come out already smelted, same as a furnace would.
    private void breakBlockWithOreDrops(Player p, Block block, ItemStack tool) {
        if (ORES.contains(block.getType())) {
            for (ItemStack drop : block.getDrops(tool)) {
                giveOrDrop(p, smelted(drop));
            }
        }
        breakBlockSilent(block);
    }

    private ItemStack smelted(ItemStack raw) {
        Material smeltedType = switch (raw.getType()) {
            case RAW_IRON   -> Material.IRON_INGOT;
            case RAW_COPPER -> Material.COPPER_INGOT;
            case RAW_GOLD   -> Material.GOLD_INGOT;
            default -> null;
        };
        if (smeltedType == null) return raw;
        ItemStack out = raw.clone();
        out.setType(smeltedType);
        return out;
    }

    private void giveOrDrop(Player p, ItemStack stack) {
        for (ItemStack extra : p.getInventory().addItem(stack).values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), extra);
        }
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_PICKAXE) return false;
        return DivineId.is(item, IgnisCommand.ID);
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
    protected double getMinCooldownSecs() { return MIN_COOLDOWN; }

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
        double radius = MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * ratio; // 1 → 4 blocks
        double length = MIN_LENGTH + (MAX_LENGTH - MIN_LENGTH) * ratio; // 5 → 20 blocks
        double damage = 10 + 30 * ratio; // 10 → 40 (damage unchanged, only the destruction radius/length was nerfed)

        Location eye = p.getEyeLocation();
        Vector   dir = eye.getDirection().normalize();
        double   r2  = radius * radius;
        World    world = eye.getWorld();
        ItemStack pickStack = p.getInventory().getItemInMainHand();

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
                    breakBlockWithOreDrops(p, world.getBlockAt(bx, by, bz), pickStack);
                }
            }
        }

        // ─── SOUNDS ───
        world.playSound(p.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 0.6f);
        world.playSound(p.getLocation(), Sound.BLOCK_LAVA_AMBIENT,  0.8f, 1.2f);

        // ─── CYLINDER PARTICLES ───
        for (int s = 1; s <= steps; s += 2) {
            Location pt = eye.clone().add(dir.clone().multiply(s));
            world.spawnParticle(Particle.FLAME,            pt, 10, (float)radius*0.25f, 0.25f, (float)radius*0.25f, 0.05f);
            world.spawnParticle(Particle.SMALL_FLAME,      pt, 6, (float)radius*0.35f, 0.35f, (float)radius*0.35f, 0.02f);
            world.spawnParticle(Particle.DRIPPING_LAVA,    pt, 3, (float)radius*0.2f, 0.2f, (float)radius*0.2f, 0);
            if (s % 10 == 0) {
                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pt, 3, 0.5f, 0.5f, 0.5f, 0.02f);
                spawnFirework(pt, C1, C2, FireworkEffect.Type.BURST, false);
            }
        }

        // ─── ENTITY QUERY ───
        // Same perpendicular-distance-from-beam-axis check used for block breaking
        // above, so only entities actually inside the radius take damage — without
        // this the broad-phase box alone hits everything near the beam's full
        // length regardless of how far off-axis they are.
        Location midPt  = eye.clone().add(dir.clone().multiply(length / 2.0));
        double halfLen  = length / 2.0 + 1;
        Set<LivingEntity> targets = new HashSet<>();
        world.getNearbyEntities(midPt, halfLen + ri, ri, halfLen + ri).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .filter(e -> {
                    Vector toEnt = e.getLocation().toVector().subtract(eye.toVector());
                    double proj = toEnt.dot(dir);
                    if (proj < 0 || proj > length) return false;
                    double perpDist2 = toEnt.lengthSquared() - proj * proj;
                    return perpDist2 <= r2;
                })
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
