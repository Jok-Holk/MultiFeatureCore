package com.jokholk.multifeature;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class AbyssalTridentListener implements Listener {

    private final MainPlugin plugin;
    private final Map<UUID, UUID> trackedTridents = new HashMap<>();

    // Dame trực tiếp khi trúng mục tiêu trong điều kiện bình thường
    static final double BASE_DAMAGE = 40.0;

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

    // Trả về true nếu entity đang trong nước hoặc mưa có tiếp xúc bầu trời
    private boolean isWetCondition(Entity entity, Location loc) {
        if (entity.isInWater()) return true;
        World world = loc.getWorld();
        if (world.hasStorm()) {
            // Tiếp xúc bầu trời: entity ở ngang hoặc cao hơn block cao nhất tại XZ đó
            return world.getHighestBlockYAt(loc) <= loc.getBlockY();
        }
        return false;
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

        // Xóa item khỏi inventory kể cả Creative — bypass vanilla Creative behavior
        for (int i = 0; i < shooter.getInventory().getSize(); i++) {
            ItemStack slot = shooter.getInventory().getItem(i);
            if (isAbyssalTrident(slot)) {
                shooter.getInventory().setItem(i, null);
                break;
            }
        }

        // BASE_DAMAGE đặt ở đây; EntityDamageByEntityEvent sẽ override nếu wet
        trident.setVelocity(trident.getVelocity().multiply(3.0));
        trident.setDamage(BASE_DAMAGE);

        trackedTridents.put(trident.getUniqueId(), shooter.getUniqueId());

        shooter.playSound(shooter.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 0.7f, 0.8f);
    }

    // ======================================================
    // TRÚNG BLOCK → QUAY VỀ CHỦ
    // Entity hit được xử lý ở EntityDamageByEntityEvent
    // ======================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!isAbyssalTrident(trident.getItemStack())) return;
        if (!trackedTridents.containsKey(trident.getUniqueId())) return;

        if (e.getHitEntity() == null) {
            // Trúng block → xóa khỏi map và quay về
            UUID shooterUUID = trackedTridents.remove(trident.getUniqueId());
            Player shooter = Bukkit.getPlayer(shooterUUID);
            if (shooter != null && shooter.isOnline()) {
                scheduleReturn(trident, shooter);
            }
        }
        // Trúng entity: giữ trong map để EntityDamageByEntityEvent xử lý dame + hiệu ứng
    }

    // ======================================================
    // TRÚNG ENTITY → DAME + HIỆU ỨNG THEO ĐIỀU KIỆN ƯỚT/KHÔ
    // ======================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onTridentDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Trident trident)) return;
        if (!isAbyssalTrident(trident.getItemStack())) return;

        UUID shooterUUID = trackedTridents.remove(trident.getUniqueId());
        if (shooterUUID == null) return;

        Entity victim = e.getEntity();
        Location hitLoc = victim.getLocation();
        boolean wet = isWetCondition(victim, hitLoc);

        // Dame trực tiếp: x2 nếu ướt
        double effectiveDamage = wet ? BASE_DAMAGE * 2 : BASE_DAMAGE;
        // Dame bắn tung tóe: 50% của dame trực tiếp
        double splashDamage    = effectiveDamage * 0.5;
        // Phạm vi splash: 3×3 (r=1.5) thường, 6×6 (r=3.0) khi ướt
        double splashRadius    = wet ? 3.0 : 1.5;
        // Nổ to hơn khi ướt
        float  explosionSize   = wet ? 6.0f : 3.5f;

        e.setDamage(effectiveDamage);

        spawnAbyssalEffects(hitLoc, wet);

        if (victim instanceof Player victimPlayer) {
            if (victimPlayer.getGameMode() == GameMode.SURVIVAL) {
                hitLoc.getWorld().strikeLightningEffect(hitLoc);
                hitLoc.getWorld().createExplosion(hitLoc, explosionSize, false, false);
                applySplash(hitLoc, splashDamage, splashRadius, victim, trident);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (victimPlayer.isOnline()) {
                        victimPlayer.kickPlayer(
                                "§b⚓ THE ABYSS CLAIMS YOU ⚓\n\n" +
                                "§3The crushing deep has swallowed you whole..."
                        );
                    }
                }, 1L);
            }
        } else if (victim instanceof LivingEntity) {
            hitLoc.getWorld().strikeLightning(hitLoc);
            hitLoc.getWorld().createExplosion(hitLoc, explosionSize, false, false);
            applySplash(hitLoc, splashDamage, splashRadius, victim, trident);
        }

        // Xóa entity trident sau khi xử lý để không rơi thành item
        Bukkit.getScheduler().runTaskLater(plugin, trident::remove, 2L);
    }

    // Gây dame cho các entity xung quanh điểm va chạm (trừ mục tiêu chính)
    private void applySplash(Location center, double damage, double radius, Entity exclude, Projectile source) {
        center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(ent -> ent != exclude && ent instanceof LivingEntity)
                .forEach(ent -> ((LivingEntity) ent).damage(damage, source));
    }

    // ======================================================
    // QUAY NHANH VỀ CHỦ (chỉ khi trúng block)
    // ======================================================

    private void scheduleReturn(Trident trident, Player shooter) {
        ItemStack returnItem = trident.getItemStack().clone();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!shooter.isOnline()) { cancel(); return; }

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

                Vector dir = targetLoc.toVector()
                        .subtract(tridentLoc.toVector())
                        .normalize()
                        .multiply(1.75);
                trident.setVelocity(dir);
            }
        }.runTaskTimer(plugin, 3L, 1L);
    }

    private void giveBack(Player shooter, ItemStack item) {
        shooter.getInventory().addItem(item);
    }

    // ======================================================
    // HIỆU ỨNG BIỂN SÂU — hoành tráng hơn khi ướt
    // ======================================================

    private void spawnAbyssalEffects(Location loc, boolean wet) {
        World world = loc.getWorld();
        int  fwCount = wet ? 6 : 3;
        double ring  = wet ? 2.5 : 1.5;

        for (int i = 0; i < fwCount; i++) {
            Location fwLoc = loc.clone().add(
                    Math.cos(i * 2 * Math.PI / fwCount) * ring,
                    0,
                    Math.sin(i * 2 * Math.PI / fwCount) * ring
            );
            spawnFirework(fwLoc, wet);
        }

        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                wet ? 1.5f : 1f, wet ? 0.5f : 0.7f);
        world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_DEATH,
                wet ? 1.2f : 0.8f, 1.2f);

        if (wet) {
            // Âm thanh bổ sung khi ướt — vang hơn, dữ hơn
            world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_HURT, 1f, 0.6f);
            world.playSound(loc, Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, 1f, 0.8f);
        }
    }

    private void spawnFirework(Location loc, boolean wet) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        var meta = fw.getFireworkMeta();

        meta.addEffect(
                FireworkEffect.builder()
                        .withColor(Color.AQUA, Color.BLUE, Color.fromRGB(0, 120, 180))
                        .with(wet ? FireworkEffect.Type.STAR : FireworkEffect.Type.BURST)
                        .trail(wet)
                        .flicker(wet)
                        .build()
        );

        meta.setPower(0);
        fw.setFireworkMeta(meta);

        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }
}
