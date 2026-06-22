package com.jokholk.multifeature;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.event.block.Action;

import java.util.*;

public class AbyssalTridentListener implements Listener {

    private final MainPlugin plugin;

    // Trident đang bay: tridentEntityId → shooterUUID
    private final Map<UUID, UUID> trackedTridents = new HashMap<>();

    public AbyssalTridentListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // ======================================================
    // KIỂM TRA ITEM
    // ======================================================

    private boolean isAbyssalTrident(ItemStack item) {
        if (item == null || item.getType() != Material.TRIDENT) return false;
        if (!item.hasItemMeta()) return false;
        return AbyssalTridentCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
    }

    private boolean isOwner(Player p, ItemStack item) {
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        String last = lore.get(lore.size() - 1);
        return last.contains(p.getUniqueId().toString());
    }

    // ======================================================
    // TRỪNG PHẠT KẺ TRỘM
    // ======================================================

    private void punishThief(Player p, ItemStack item) {
        p.getInventory().remove(item);

        p.getWorld().getNearbyEntities(p.getLocation(), 6, 6, 6).forEach(ent -> {
            if (ent instanceof Item dropped && isAbyssalTrident(dropped.getItemStack())) {
                dropped.remove();
            }
        });

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                p.kickPlayer(
                        "§3The abyss does not forget\n" +
                        "§bThis weapon was never yours"
                );
            }
        }, 1L);
    }

    // ======================================================
    // CHẶN NHẶT TRỘM
    // ======================================================

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (!isAbyssalTrident(item)) return;

        if (!(e.getEntity() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        if (!isOwner(p, item)) {
            e.getItem().remove();
            e.setCancelled(true);
            punishThief(p, item);
        }
    }

    // ======================================================
    // PHÓNG TRIDENT → BOOST TỐC ĐỘ + TRACK
    // ======================================================

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!(trident.getShooter() instanceof Player shooter)) return;
        if (!isAbyssalTrident(trident.getItemStack())) return;
        if (!isOwner(shooter, trident.getItemStack())) {
            punishThief(shooter, trident.getItemStack());
            e.setCancelled(true);
            return;
        }

        // Tăng tốc độ gấp 3× và tăng sát thương
        trident.setVelocity(trident.getVelocity().multiply(3.0));
        trident.setDamage(40.0);

        trackedTridents.put(trident.getUniqueId(), shooter.getUniqueId());

        // Âm thanh biển sâu khi phóng
        shooter.playSound(shooter.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 0.7f, 0.8f);
    }

    // ======================================================
    // TRÚNG MỤC TIÊU → HIỆU ỨNG + QUAY LẠI
    // ======================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!isAbyssalTrident(trident.getItemStack())) return;
        if (!trackedTridents.containsKey(trident.getUniqueId())) return;

        UUID shooterUUID = trackedTridents.remove(trident.getUniqueId());
        Player shooter = Bukkit.getPlayer(shooterUUID);

        Entity hitEntity = e.getHitEntity();

        if (hitEntity != null) {
            // ── Trúng entity → hiệu ứng, Loyalty 3 tự quay về ──
            Location hitLoc = hitEntity.getLocation();
            spawnAbyssalEffects(hitLoc);

            if (hitEntity instanceof LivingEntity && !(hitEntity instanceof Player)) {
                hitLoc.getWorld().strikeLightning(hitLoc);
                hitLoc.getWorld().createExplosion(hitLoc, 3.5f, false, false);
            }

            if (hitEntity instanceof Player victim &&
                    victim.getGameMode() == GameMode.SURVIVAL) {

                hitLoc.getWorld().strikeLightningEffect(hitLoc);
                hitLoc.getWorld().createExplosion(hitLoc, 3.5f, false, false);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (victim.isOnline()) {
                        victim.kickPlayer(
                                "§b⚓ THE ABYSS CLAIMS YOU ⚓\n\n" +
                                "§3The crushing deep has swallowed you whole..."
                        );
                    }
                }, 1L);
            }
            // Không gọi scheduleReturn — Loyalty 3 xử lý quay về tự nhiên

        } else {
            // ── Trúng block → kéo nhanh về chủ ──
            if (shooter != null && shooter.isOnline()) {
                scheduleReturn(trident, shooter);
            }
        }
    }

    // ======================================================
    // QUAY NHANH VỀ CHỦ
    // ======================================================

    private void scheduleReturn(Trident trident, Player shooter) {
        ItemStack returnItem = trident.getItemStack().clone();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!shooter.isOnline()) { cancel(); return; }

                // Entity bị thu hồi ngoài scheduler (Loyalty hoặc pickup)
                if (!trident.isValid()) {
                    giveBack(shooter, returnItem);
                    cancel();
                    return;
                }

                Location tridentLoc = trident.getLocation();
                Location targetLoc  = shooter.getLocation().add(0, 1, 0);

                if (tridentLoc.distanceSquared(targetLoc) < 1.5) {
                    trident.remove();
                    giveBack(shooter, returnItem);
                    cancel();
                    return;
                }

                // Kéo về chủ — tốc độ 1.75 block/tick
                Vector dir = targetLoc.toVector()
                        .subtract(tridentLoc.toVector())
                        .normalize()
                        .multiply(1.75);
                trident.setVelocity(dir);
            }
        }.runTaskTimer(plugin, 3L, 1L);
    }

    // Creative mode giữ item trong inventory khi ném — không trả thêm để tránh duplicate
    private void giveBack(Player shooter, ItemStack item) {
        if (shooter.getGameMode() != GameMode.CREATIVE) {
            shooter.getInventory().addItem(item);
        }
    }

    // ======================================================
    // HIỆU ỨNG BIỂN SÂU
    // ======================================================

    private void spawnAbyssalEffects(Location loc) {
        World world = loc.getWorld();

        // Pháo hoa màu xanh biển
        for (int i = 0; i < 3; i++) {
            Location fwLoc = loc.clone().add(
                    Math.cos(i * 2 * Math.PI / 3) * 1.5,
                    0,
                    Math.sin(i * 2 * Math.PI / 3) * 1.5
            );
            spawnFirework(fwLoc);
        }

        // Tiếng sét + guardian
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 0.7f);
        world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_DEATH, 0.8f, 1.2f);
    }

    private void spawnFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        var meta = fw.getFireworkMeta();

        meta.addEffect(
                FireworkEffect.builder()
                        .withColor(Color.AQUA, Color.BLUE, Color.fromRGB(0, 120, 180))
                        .with(FireworkEffect.Type.BURST)
                        .trail(true)
                        .flicker(false)
                        .build()
        );

        meta.setPower(0);
        fw.setFireworkMeta(meta);

        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }
}
