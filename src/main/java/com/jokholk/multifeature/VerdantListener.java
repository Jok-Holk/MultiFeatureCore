package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * VerdantListener — cong cu nong nghiep, khong dung charge mechanic.
 * Shift+Right-click: doi mode; Right-click: ap dung mode tai vi tri nham.
 */
public class VerdantListener implements Listener {

    private final MainPlugin plugin;
    private final Map<UUID, Integer> modes = new HashMap<>();

    private static final int[]    RADII      = {0, 1, 2, 4, 7};
    private static final String[] MODE_NAMES = {"1x1", "3x3", "5x5", "9x9", "15x15"};

    // Cac mat do cay trong duoc lam chin
    private static final Set<Material> CROPS = Set.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.NETHER_WART
    );

    // Cay co / hoa duoc xoa
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

    private static final Color C1 = Color.fromRGB(0,   150, 0);
    private static final Color C2 = Color.fromRGB(100, 200, 0);

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
                    p.kickPlayer("§cThis power was never yours.");
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
            // Doi mode
            int mode = modes.getOrDefault(uid, 0);
            mode = (mode + 1) % RADII.length;
            modes.put(uid, mode);
            p.sendMessage("§2Mode: §a" + MODE_NAMES[mode]);
            return;
        }

        // Ap dung mode
        int radius = RADII[modes.getOrDefault(uid, 0)];

        Location center;
        if (action == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock() != null) {
            center = e.getClickedBlock().getLocation();
        } else {
            center = p.getLocation();
        }

        applyMode(p, center, radius);
    }

    private void applyMode(Player p, Location center, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                var block = center.clone().add(dx, 0, dz).getBlock();
                Material type = block.getType();

                if (type == Material.GRASS_BLOCK || type == Material.DIRT) {
                    block.setType(Material.FARMLAND);
                } else if (CROPS.contains(type)) {
                    // Lam chin cay trong
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

        // Spawn 2-3 firework nho o trung tam
        int fwCount = radius == 0 ? 1 : Math.min(3, 1 + radius / 3);
        for (int i = 0; i < fwCount; i++) {
            double angle = i * 2 * Math.PI / fwCount;
            double r2 = radius * 0.4;
            Location fwLoc = center.clone().add(
                    Math.cos(angle) * r2, 1.0, Math.sin(angle) * r2
            );
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
                        .build()
        );
        fw.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }
}
