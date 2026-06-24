package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Firework;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VerdantListener implements Listener {

    private final MainPlugin plugin;
    private final Map<UUID, Integer> modes = new HashMap<>();

    private static final int[]    RADII      = {0, 1, 2, 4, 7};
    private static final String[] MODE_NAMES = {"1x1", "3x3", "5x5", "9x9", "15x15"};

    private static final Set<Material> CROPS = Set.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.NETHER_WART
    );

    private static final Set<Material> GRASS_VARIANTS = Set.of(
            Material.SHORT_GRASS, Material.TALL_GRASS,
            Material.FERN, Material.LARGE_FERN,
            Material.DANDELION, Material.POPPY,
            Material.BLUE_ORCHID, Material.ALLIUM,
            Material.AZURE_BLUET, Material.RED_TULIP,
            Material.ORANGE_TULIP, Material.WHITE_TULIP,
            Material.PINK_TULIP, Material.OXEYE_DAISY,
            Material.CORNFLOWER, Material.LILY_OF_THE_VALLEY,
            Material.SUNFLOWER, Material.LILAC,
            Material.ROSE_BUSH, Material.PEONY
    );

    private static final Color C1 = Color.fromRGB(0,   180, 0);
    private static final Color C2 = Color.fromRGB(120, 255, 0);

    public VerdantListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isVerdant(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_HOE) return false;
        if (!item.hasItemMeta()) return false;
        return VerdantCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
        if (!isVerdant(item)) return;

        if (!(e.getEntity() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        if (!isOwner(p, item)) {
            e.getItem().remove();
            e.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline()) {
                    p.kickPlayer(Msg.VERDANT_KICK_THEFT.get(p));
                }
            }, 1L);
        }
    }

    // ─── Interact ───

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        ItemStack held = e.getItem();
        if (!isVerdant(held)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();

        Action action = e.getAction();
        boolean isRightClick = action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
        if (!isRightClick) return;

        UUID uid = p.getUniqueId();

        if (p.isSneaking()) {
            int mode = modes.getOrDefault(uid, 0);
            mode = (mode + 1) % RADII.length;
            modes.put(uid, mode);
            // Mode switch: green sparkle + message
            p.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().clone().add(0, 1, 0),
                    8, 0.5, 0.5, 0.5, 0);
            p.sendMessage(Msg.VERDANT_MODE.fmt(p, "area", MODE_NAMES[mode]));
            return;
        }

        int radius = RADII[modes.getOrDefault(uid, 0)];

        Location center;
        if (action == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            center = e.getClickedBlock().getLocation();
        } else {
            center = p.getLocation();
        }

        applyMode(p, center, radius);
        p.sendMessage(Msg.VERDANT_CAST.get(p));
    }

    private void applyMode(Player p, Location center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                var block = center.clone().add(dx, 0, dz).getBlock();
                Material type = block.getType();

                if (type == Material.GRASS_BLOCK || type == Material.DIRT) {
                    block.setType(Material.FARMLAND);
                } else if (CROPS.contains(type)) {
                    var data = block.getBlockData();
                    if (data instanceof Ageable ageable) {
                        ageable.setAge(ageable.getMaximumAge());
                        block.setBlockData(ageable);
                    }
                } else if (GRASS_VARIANTS.contains(type)) {
                    block.setType(Material.AIR);
                }
            }
        }

        // ─── VFX ───
        center.getWorld().playSound(center, Sound.BLOCK_GRASS_BREAK, 0.7f, 1.1f);

        // HAPPY_VILLAGER spreading outward in spiral
        int pCount = Math.max(8, radius * radius * 3);
        for (int i = 0; i < pCount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r2    = Math.random() * (radius + 0.5);
            Location loc = center.clone().add(Math.cos(angle) * r2, 0.5, Math.sin(angle) * r2);
            center.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 1, 0.1, 0.2, 0.1, 0);
        }
        // SPORE_BLOSSOM_AIR floating softly
        center.getWorld().spawnParticle(Particle.SPORE_BLOSSOM_AIR, center.clone().add(0, 1, 0),
                Math.max(10, radius * 5), radius * 0.6f, 0.5f, radius * 0.6f, 0.02f);
        // Cherry leaves drift (optional, 1.20+)
        if (radius >= 2) {
            center.getWorld().spawnParticle(Particle.CHERRY_LEAVES, center.clone().add(0, 1.5, 0),
                    radius * 4, radius * 0.5f, 0.4f, radius * 0.5f, 0.02f);
        }

        // Fireworks at center
        int fwCount = radius == 0 ? 1 : Math.min(3, 1 + radius / 3);
        for (int i = 0; i < fwCount; i++) {
            double angle = i * 2 * Math.PI / Math.max(1, fwCount);
            double r2    = radius * 0.4;
            Location fwLoc = center.clone().add(Math.cos(angle) * r2, 1.0, Math.sin(angle) * r2);
            spawnFirework(fwLoc);
        }
    }

    private void spawnFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(
                FireworkEffect.builder()
                        .withColor(C1, C2)
                        .with(FireworkEffect.Type.BURST)
                        .flicker(false)
                        .build()
        );
        fw.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }
}
