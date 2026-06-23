package com.jokholk.multifeature;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AbyssalTridentListener implements Listener {

    private final MainPlugin plugin;
    private final Map<UUID, UUID> trackedTridents = new HashMap<>();
    // Item duoc store luc launch - truoc khi xoa khoi inventory, dam bao luon co item hop le
    private final Map<UUID, ItemStack> storedItems = new HashMap<>();

    // Dame truc tiep khi trung muc tieu trong dieu kien binh thuong
    static final double BASE_DAMAGE = 40.0;

    public AbyssalTridentListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // ======================================================
    // KIEM TRA ITEM
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

    // Tra ve true neu entity dang trong nuoc hoac mua co tiep xuc bau troi
    private boolean isWetCondition(Entity entity, Location loc) {
        if (entity.isInWater()) return true;
        World world = loc.getWorld();
        if (world.hasStorm()) {
            // Tiep xuc bau troi: entity o ngang hoac cao hon block cao nhat tai XZ do
            return world.getHighestBlockYAt(loc) <= loc.getBlockY();
        }
        return false;
    }

    // ======================================================
    // TRUNG PHAT KE TROM
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
    // CHAN NHAT TROM
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
    // PHONG TRIDENT -> BOOST TOC DO + TRACK
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

        // Xoa item khoi inventory ke ca Creative - bypass vanilla Creative behavior
        // Store clone TRUOC khi xoa de dam bao item hop le khi can tra ve
        for (int i = 0; i < shooter.getInventory().getSize(); i++) {
            ItemStack slot = shooter.getInventory().getItem(i);
            if (isAbyssalTrident(slot)) {
                storedItems.put(trident.getUniqueId(), slot.clone());
                shooter.getInventory().setItem(i, null);
                break;
            }
        }

        // BASE_DAMAGE dat o day; EntityDamageByEntityEvent se override neu wet
        trident.setVelocity(trident.getVelocity().multiply(3.0));
        trident.setDamage(BASE_DAMAGE);

        trackedTridents.put(trident.getUniqueId(), shooter.getUniqueId());

        shooter.playSound(shooter.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, 0.7f, 0.8f);
    }

    // ======================================================
    // TRUNG BLOCK -> QUAY VE CHU
    // Entity hit duoc xu ly o EntityDamageByEntityEvent
    // ======================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Trident trident)) return;
        if (!isAbyssalTrident(trident.getItemStack())) return;
        if (!trackedTridents.containsKey(trident.getUniqueId())) return;

        if (e.getHitEntity() == null) {
            // Trung block -> xoa khoi map va quay ve
            UUID shooterUUID = trackedTridents.remove(trident.getUniqueId());
            ItemStack returnItem = storedItems.remove(trident.getUniqueId());
            if (returnItem == null) returnItem = trident.getItemStack().clone();
            final ItemStack finalBlockItem = returnItem;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                trident.remove();
                Player shooter = Bukkit.getPlayer(shooterUUID);
                if (shooter != null && shooter.isOnline()) {
                    giveBack(shooter, finalBlockItem);
                    shooter.playSound(shooter.getLocation(), Sound.ITEM_TRIDENT_RETURN, 1.0f, 1.2f);
                }
            }, 2L);
        }
        // Trung entity: giu trong map de EntityDamageByEntityEvent xu ly dame + hieu ung
    }

    // ======================================================
    // TRUNG ENTITY -> DAME + HIEU UNG THEO DIEU KIEN UOT/KHO
    // ======================================================

    @EventHandler(priority = EventPriority.HIGH)
    public void onTridentDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Trident trident)) return;
        if (!isAbyssalTrident(trident.getItemStack())) return;

        UUID shooterUUID = trackedTridents.remove(trident.getUniqueId());
        if (shooterUUID == null) return;

        // Dung item da luu luc launch; fallback getItemStack neu lo thieu
        ItemStack returnItem = storedItems.remove(trident.getUniqueId());
        if (returnItem == null) returnItem = trident.getItemStack().clone();

        Entity victim = e.getEntity();
        Location hitLoc = victim.getLocation();
        boolean wet = isWetCondition(victim, hitLoc);

        // Dame truc tiep: x2 neu uot
        double effectiveDamage = wet ? BASE_DAMAGE * 2 : BASE_DAMAGE;
        // Dame ban tung toe: 50% cua dame truc tiep
        double splashDamage    = effectiveDamage * 0.5;
        // Pham vi splash: 3x3 (r=1.5) thuong, 6x6 (r=3.0) khi uot
        double splashRadius    = wet ? 3.0 : 1.5;
        // No to hon khi uot
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

        // Xoa entity trident + tra lai item cho chu bang UUID (khong dung captured Player ref)
        final ItemStack finalItem = returnItem;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            trident.remove();
            Player shooter = Bukkit.getPlayer(shooterUUID);
            if (shooter != null && shooter.isOnline()) {
                giveBack(shooter, finalItem);
            }
        }, 2L);
    }

    // Gay dame cho cac entity xung quanh diem va cham (tru muc tieu chinh)
    private void applySplash(Location center, double damage, double radius, Entity exclude, Projectile source) {
        center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(ent -> ent != exclude && ent instanceof LivingEntity)
                .forEach(ent -> ((LivingEntity) ent).damage(damage, source));
    }

    // ======================================================
    // QUAY NHANH VE CHU (chi khi trung block)
    // ======================================================

    private void giveBack(Player shooter, ItemStack item) {
        shooter.getInventory().addItem(item);
    }

    // ======================================================
    // HIEU UNG BIEN SAU - hoanh trang hon khi uot
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

        // IMPACT thay THUNDER: tieng no sac, it bass hon - khong lan at Guardian
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT,
                wet ? 0.5f : 0.35f, wet ? 0.7f : 0.9f);

        // Elder Guardian la am chinh - full volume, pitch thap xuong cho tram hon
        world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_DEATH,
                1.0f, wet ? 0.6f : 0.7f);
        // AMBIENT tao hau canh bien sau keo dai sau DEATH
        world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_AMBIENT,
                1.0f, wet ? 0.5f : 0.6f);

        if (wet) {
            world.playSound(loc, Sound.ENTITY_ELDER_GUARDIAN_HURT, 1.0f, 0.5f);
            world.playSound(loc, Sound.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, 0.8f, 0.8f);
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
