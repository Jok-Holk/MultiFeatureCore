package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GraveListener implements Listener {

    private final MainPlugin plugin;
    private final Map<UUID, Integer> modes = new HashMap<>();

    // radius, depth, displayed name
    private static final int[]    RADII      = {0, 1, 2, 4,  7};
    private static final int[]    DEPTHS     = {3, 4, 5, 7, 10};
    private static final String[] MODE_NAMES = {"1x1", "3x3", "5x5", "9x9", "15x15"};

    private static final Set<Material> SHOVEL_BLOCKS = Set.of(
            Material.DIRT, Material.GRASS_BLOCK, Material.COARSE_DIRT,
            Material.PODZOL, Material.MYCELIUM,
            Material.SAND, Material.RED_SAND,
            Material.GRAVEL, Material.CLAY,
            Material.SOUL_SAND, Material.SOUL_SOIL,
            Material.SNOW, Material.SNOW_BLOCK,
            Material.MUD, Material.MUDDY_MANGROVE_ROOTS
    );

    private static final Color C1 = Color.fromRGB(100, 0, 150);
    private static final Color C2 = Color.fromRGB(30,  0,  60);

    public GraveListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isGrave(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SHOVEL) return false;
        if (!item.hasItemMeta()) return false;
        return GraveCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
    }

    private boolean isOwner(Player p, ItemStack item) {
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        return lore.get(lore.size() - 1).contains(p.getUniqueId().toString());
    }

    // ─── Cleanup on quit ───

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        modes.remove(e.getPlayer().getUniqueId());
    }

    // ─── Anti-theft ───

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (!isGrave(item)) return;

        if (!(e.getEntity() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        if (!isOwner(p, item)) {
            e.getItem().remove();
            e.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline()) p.kickPlayer(Msg.GRAVE_KICK_THEFT.get(p));
            }, 1L);
        }
    }

    // ─── Interact: shift = cycle mode, right-click = activate ───

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Action action = e.getAction();
        boolean isRight = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (!isRight) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!isGrave(item)) return;
        if (!isOwner(p, item)) return;

        e.setCancelled(true);

        if (p.isSneaking()) {
            // Cycle mode
            int mode = (modes.getOrDefault(p.getUniqueId(), 0) + 1) % MODE_NAMES.length;
            modes.put(p.getUniqueId(), mode);
            p.sendMessage(Msg.GRAVE_MODE.fmt(p, "area", MODE_NAMES[mode]));
            // Mode switch VFX: small soul particle puff
            p.getWorld().spawnParticle(Particle.SOUL,
                    p.getLocation().clone().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.02);
            p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SOUL_SAND_STEP, 0.8f, 0.5f);
        } else {
            // Activate
            activateGrave(p);
        }
    }

    private void activateGrave(Player p) {
        int mode  = modes.getOrDefault(p.getUniqueId(), 0);
        int r     = RADII[mode];
        int depth = DEPTHS[mode];

        Location center = p.getLocation();
        World    world  = center.getWorld();

        // ─── Break blocks (circle downward) ───
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx*dx + dz*dz <= r*r + r) {          // smooth circle
                    for (int dy = 0; dy >= -depth; dy--) {
                        Block block = center.clone().add(dx, dy, dz).getBlock();
                        if (SHOVEL_BLOCKS.contains(block.getType())) {
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        }

        // ─── Entities in radius ───
        double effectR = Math.max(2.0, r + 1.5);
        Set<LivingEntity> targets = new HashSet<>();
        world.getNearbyEntities(center, effectR, 3, effectR).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        for (LivingEntity target : targets) {
            target.damage(15, p);
            target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,  140, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1));
            Location tLoc = target.getLocation().clone().add(0, 1, 0);
            tLoc.getWorld().spawnParticle(Particle.SOUL,       tLoc, 12, 0.4, 0.5, 0.4, 0.04);
            tLoc.getWorld().spawnParticle(Particle.SCULK_SOUL, tLoc.clone().add(0, 0.5, 0),
                    6, 0.3, 0.3, 0.3, 0.06);
        }

        // ─── Area VFX ───
        double vfxR = Math.max(1.5, r);
        world.playSound(center, Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD, 1.0f, 0.6f);
        world.playSound(center, Sound.ENTITY_WITHER_AMBIENT,         0.8f, 0.5f);

        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist  = Math.random() * vfxR;
            Location loc = center.clone().add(Math.cos(angle) * dist, 0.5, Math.sin(angle) * dist);
            world.spawnParticle(Particle.SOUL, loc, 2, 0.2, 0.4, 0.2, 0.02);
        }
        for (int i = 0; i < 25; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist  = Math.random() * vfxR;
            Location loc = center.clone().add(Math.cos(angle) * dist, 2.0, Math.sin(angle) * dist);
            world.spawnParticle(Particle.ASH, loc, 3, 0.4, 0.5, 0.4, 0.03);
        }

        // Fireworks at edge (skip for 1x1 mode)
        if (r > 0) {
            int fwCount = Math.min(4 + r, 12);
            for (int i = 0; i < fwCount; i++) {
                double angle = i * 2 * Math.PI / fwCount;
                Location fwLoc = center.clone().add(
                        Math.cos(angle) * vfxR * 0.9, 1.0, Math.sin(angle) * vfxR * 0.9);
                spawnFirework(fwLoc);
            }
        }

        p.sendMessage(Msg.GRAVE_CAST.get(p));
    }

    private void spawnFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder()
                .withColor(C1, C2)
                .with(FireworkEffect.Type.BURST)
                .build());
        fw.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }
}
