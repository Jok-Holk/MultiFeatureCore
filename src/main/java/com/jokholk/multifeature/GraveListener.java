package com.jokholk.multifeature;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GraveListener extends DivineWeaponListener {

    static final double MAX_CHARGE  = 8.0;
    static final double MAX_RADIUS  = 20.0;
    static final double MAX_DEPTH   = 12.0;
    static final double MAX_DAMAGE  = 20.0;

    private static final Color C1 = Color.fromRGB(100, 0, 150);
    private static final Color C2 = Color.fromRGB(50,  0, 80);

    // Cac block bi pha boi Grave Sovereign (shovel-tier)
    private static final Set<Material> SHOVEL_BLOCKS = Set.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT,
            Material.PODZOL, Material.MYCELIUM,
            Material.SAND, Material.RED_SAND,
            Material.GRAVEL, Material.CLAY,
            Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.SNOW, Material.SNOW_BLOCK,
            Material.MUD, Material.MUDDY_MANGROVE_ROOTS
    );

    public GraveListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SHOVEL) return false;
        if (!item.hasItemMeta()) return false;
        return GraveCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
    protected void onChargeVisual(Player p, double ratio) {
        double angle = (System.currentTimeMillis() / 300.0) % (2 * Math.PI);
        double r = 1.2 + ratio;
        Location loc = p.getLocation().clone().add(
                Math.cos(angle) * r, 0.2, Math.sin(angle) * r
        );
        spawnFirework(loc, C1, C2, FireworkEffect.Type.BURST, false);
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double radius     = 3 + 17 * ratio;            // 3-20
        double depth      = 3 + 9 * ratio;             // 3-12
        int    witherTicks = (int)(60 + 100 * ratio);  // 3-8 giay wither
        double damage     = 5 + 15 * ratio;            // 5-20

        Location center = p.getLocation();

        // Pha cac block shovel-tier trong cylinder xuong phia duoi
        int ri = (int) Math.ceil(radius);
        int d  = (int) Math.ceil(depth);
        for (int dx = -ri; dx <= ri; dx++) {
            for (int dz = -ri; dz <= ri; dz++) {
                if (dx*dx + dz*dz <= radius*radius) {
                    for (int dy = 0; dy >= -d; dy--) {
                        var block = center.clone().add(dx, dy, dz).getBlock();
                        if (SHOVEL_BLOCKS.contains(block.getType())) {
                            breakBlockSilent(block);
                        }
                    }
                }
            }
        }

        // Gay sat thuong + wither cho entities xung quanh
        Set<LivingEntity> targets = new HashSet<>();
        center.getWorld().getNearbyEntities(center, radius, 3, radius).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        for (LivingEntity target : targets) {
            target.damage(damage, p);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, witherTicks, 2));
        }

        // Soul particles rai trong vung
        for (int i = 0; i < 40; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * radius;
            Location particleLoc = center.clone().add(
                    Math.cos(angle) * r, 0.5, Math.sin(angle) * r
            );
            center.getWorld().spawnParticle(Particle.SOUL, particleLoc, 3, 0.3, 0.5, 0.3, 0.02);
        }

        // Fireworks vong tron
        int fwCount = Math.max(4, (int)(4 + 4 * ratio));
        for (int i = 0; i < fwCount; i++) {
            double angle = i * 2 * Math.PI / fwCount;
            Location fwLoc = center.clone().add(
                    Math.cos(angle) * radius * 0.9, 1.0, Math.sin(angle) * radius * 0.9
            );
            spawnFirework(fwLoc, C1, C2, FireworkEffect.Type.BURST, false);
        }

        p.sendMessage("§5§lGRAVE SOVEREIGN §7opens the earth.");
    }
}
