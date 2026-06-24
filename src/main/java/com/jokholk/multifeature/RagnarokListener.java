package com.jokholk.multifeature;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RagnarokListener extends DivineWeaponListener {

    static final double MAX_CHARGE      = 5.0;
    static final double MAX_HALF_WIDTH  = 25.0;
    static final double MAX_DEPTH       = 20.0;
    static final double MAX_DAMAGE      = 40.0;

    private static final Color C1 = Color.fromRGB(200, 50,  0);
    private static final Color C2 = Color.fromRGB(255, 100, 0);
    private static final Vector UP = new Vector(0, 1, 0);

    public RagnarokListener(MainPlugin plugin) {
        super(plugin);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_AXE) return false;
        if (!item.hasItemMeta()) return false;
        return RagnarokCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
        // 2 fireworks phia truoc player, mau do/cam
        double angle = (System.currentTimeMillis() / 300.0) % (2 * Math.PI);
        Location base = p.getLocation().clone().add(0, 1, 0);
        for (int i = 0; i < 2; i++) {
            double a = angle + i * Math.PI;
            Location loc = base.clone().add(Math.cos(a) * 1.5, 0, Math.sin(a) * 1.5);
            spawnFirework(loc, C1, C2, FireworkEffect.Type.STAR, false);
        }
    }

    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        double halfWidth = 5 + 20 * ratio;   // 5-25
        double depth     = 5 + 15 * ratio;   // 5-20
        double damage    = 15 + 25 * ratio;  // 15-40

        // Tinh forward va right vector (bo qua Y)
        Vector forward = p.getLocation().getDirection();
        forward.setY(0);
        if (forward.lengthSquared() < 0.001) forward = new Vector(1, 0, 0);
        forward.normalize();

        final Vector fwd   = forward;
        final Vector right = fwd.crossProduct(UP).normalize();

        Location feet = p.getLocation();
        Set<LivingEntity> targets = new HashSet<>();

        int hw = (int) Math.ceil(halfWidth);
        int d  = (int) Math.ceil(depth);

        // Pha blocks: duyet bbox cua vung quet
        int minX = (int) Math.floor(Math.min(
                feet.getX() - hw * Math.abs(right.getX()) - d * Math.abs(fwd.getX()),
                feet.getX() + hw * Math.abs(right.getX()) + d * Math.abs(fwd.getX()))) - 1;
        int maxX = (int) Math.ceil(Math.max(
                feet.getX() - hw * Math.abs(right.getX()) - d * Math.abs(fwd.getX()),
                feet.getX() + hw * Math.abs(right.getX()) + d * Math.abs(fwd.getX()))) + 1;
        int minZ = (int) Math.floor(Math.min(
                feet.getZ() - hw * Math.abs(right.getZ()) - d * Math.abs(fwd.getZ()),
                feet.getZ() + hw * Math.abs(right.getZ()) + d * Math.abs(fwd.getZ()))) - 1;
        int maxZ = (int) Math.ceil(Math.max(
                feet.getZ() - hw * Math.abs(right.getZ()) - d * Math.abs(fwd.getZ()),
                feet.getZ() + hw * Math.abs(right.getZ()) + d * Math.abs(fwd.getZ()))) + 1;
        int baseY = feet.getBlockY();

        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                Vector toBlock = new Vector(bx - feet.getX(), 0, bz - feet.getZ());
                double fwdDot  = toBlock.dot(fwd);
                double sideDot = Math.abs(toBlock.dot(right));
                if (fwdDot < 0 || fwdDot > depth || sideDot > halfWidth) continue;
                for (int dy = -1; dy <= 1; dy++) {
                    breakBlockSilent(feet.getWorld().getBlockAt(bx, baseY + dy, bz));
                }
            }
        }

        // Entity query: 1 lan duy nhat, loc bang dot product
        Location sweepCenter = feet.clone()
                .add(fwd.clone().multiply(depth / 2.0));
        double queryHalfLen = depth / 2.0 + 2;
        feet.getWorld().getNearbyEntities(sweepCenter, queryHalfLen, 3, queryHalfLen).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .filter(e -> {
                    Vector rel = e.getLocation().toVector().subtract(feet.toVector());
                    rel.setY(0);
                    double fwdDot2  = rel.dot(fwd);
                    double sideDot2 = Math.abs(rel.dot(right));
                    return fwdDot2 >= 0 && fwdDot2 <= depth + 1 && sideDot2 <= halfWidth + 1;
                })
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        // Gay sát thương cho entities
        for (LivingEntity target : targets) {
            target.damage(damage, p);
        }

        // Fireworks ngang qua vung quet
        int fwCount = Math.max(3, (int)(halfWidth / 5));
        for (int i = 0; i < fwCount; i++) {
            double sideOffset = -halfWidth + (2 * halfWidth * i / (fwCount - 1 + 0.001));
            Location fwLoc = feet.clone()
                    .add(fwd.clone().multiply(depth / 2.0))
                    .add(right.clone().multiply(sideOffset))
                    .add(0, 1, 0);
            spawnFirework(fwLoc, C1, C2, FireworkEffect.Type.STAR, false);
        }

        p.sendMessage("§c§lRAGNAROK §7sweeps the field!");
    }
}