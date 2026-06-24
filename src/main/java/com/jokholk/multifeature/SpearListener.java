package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpearListener implements Listener {

    private final MainPlugin plugin;

    // spearEntityId → shooterUUID
    private final Map<UUID, UUID>        trackedSpears = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask>  trailTasks    = new ConcurrentHashMap<>();

    private static final double EXTRA_DAMAGE  = 25.0;
    private static final Color  C1            = Color.fromRGB(255, 220, 0);
    private static final Color  C2            = Color.fromRGB(255, 180, 0);

    public SpearListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isSpear(ItemStack item) {
        if (item == null) return false;
        if (!item.hasItemMeta()) return false;
        return SpearCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
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
        UUID uid = e.getPlayer().getUniqueId();
        trackedSpears.values().removeIf(v -> v.equals(uid));
    }

    // ─── Anti-theft ───

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (!isSpear(item)) return;

        if (!(e.getEntity() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        if (!isOwner(p, item)) {
            e.getItem().remove();
            e.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (p.isOnline()) {
                    p.kickPlayer("§eJustice §7saw what you tried to do.\n§eIt remembers everything.");
                }
            }, 1L);
        }
    }

    // ─── Launch: boost velocity + track ───

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player p)) return;

        ItemStack spear = p.getInventory().getItemInMainHand();
        if (!isSpear(spear)) return;

        // Boost 4×
        arrow.setVelocity(arrow.getVelocity().multiply(4.0));
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

        UUID id = arrow.getUniqueId();
        trackedSpears.put(id, p.getUniqueId());
        startTrail(arrow);
    }

    // ─── Flying trail ───

    private void startTrail(AbstractArrow arrow) {
        UUID id = arrow.getUniqueId();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!arrow.isValid() || !trackedSpears.containsKey(id)) {
                    trailTasks.remove(id);
                    cancel();
                    return;
                }
                Location loc = arrow.getLocation();
                loc.getWorld().spawnParticle(Particle.ENCHANT,
                        loc, 5, 0.1, 0.1, 0.1, 0.2);
                loc.getWorld().spawnParticle(Particle.CRIT,
                        loc, 3, 0.08, 0.08, 0.08, 0.15);
                loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                        loc, 2, 0.05, 0.05, 0.05, 0.08);
            }
        }.runTaskTimer(plugin, 0L, 1L);
        trailTasks.put(id, task);
    }

    private void stopTrail(UUID id) {
        BukkitTask t = trailTasks.remove(id);
        if (t != null) t.cancel();
    }

    // ─── Entity hit: damage + effects + pierce ───

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof AbstractArrow arrow)) return;
        UUID id = arrow.getUniqueId();
        if (!trackedSpears.containsKey(id)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

        // Extra damage on top of normal
        e.setDamage(e.getDamage() + EXTRA_DAMAGE);

        // ─── Debuffs ───
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,    100, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,    60, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,     120, 0));

        // ─── Hit VFX ───
        Location tLoc = target.getLocation().clone().add(0, 1, 0);
        tLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, tLoc, 30, 0.5, 0.8, 0.5, 0.4);
        tLoc.getWorld().spawnParticle(Particle.ENCHANT,          tLoc, 20, 0.4, 0.5, 0.4, 0.3);
        tLoc.getWorld().spawnParticle(Particle.CRIT,             tLoc, 15, 0.4, 0.5, 0.4, 0.25);
        tLoc.getWorld().playSound(tLoc, Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.4f);

        spawnJusticeFirework(tLoc);

        // Kick if player (SURVIVAL)
        if (target instanceof Player victim && victim.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline()) {
                    victim.kickPlayer("§e⚖ SPEAR OF JUSTICE ⚖\n§7You were found guilty.\n§eThe verdict is final.");
                }
            }, 1L);
        }

        // Pierce: restore arrow velocity so it keeps going
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (arrow.isValid()) {
                Vector vel = arrow.getVelocity();
                if (vel.lengthSquared() < 0.5) {
                    arrow.setVelocity(arrow.getLocation().getDirection().normalize().multiply(3.5));
                }
            }
        }, 1L);
    }

    // ─── Block hit: impact burst + return ───

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof AbstractArrow arrow)) return;
        UUID id = arrow.getUniqueId();
        if (!trackedSpears.containsKey(id)) return;
        if (e.getHitBlock() == null) return; // entity hit handled by onDamage

        UUID shooterUid = trackedSpears.remove(id);
        stopTrail(id);

        // ─── Impact burst ───
        Location impactLoc = arrow.getLocation();
        impactLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, impactLoc, 40, 0.4, 0.4, 0.4, 0.35);
        impactLoc.getWorld().spawnParticle(Particle.ENCHANT,          impactLoc, 25, 0.5, 0.5, 0.5, 0.25);
        impactLoc.getWorld().spawnParticle(Particle.CRIT,             impactLoc, 15, 0.4, 0.4, 0.4, 0.2);
        impactLoc.getWorld().playSound(impactLoc, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.5f);
        spawnJusticeFirework(impactLoc);

        // Return to owner every 2 ticks at 2.0 blocks/tick
        final AbstractArrow finalArrow = arrow;
        new BukkitRunnable() {
            @Override
            public void run() {
                Player owner = Bukkit.getPlayer(shooterUid);
                if (owner == null || !owner.isOnline() || !finalArrow.isValid()) {
                    cancel();
                    return;
                }
                Vector toOwner = owner.getLocation().clone().add(0, 1, 0)
                        .subtract(finalArrow.getLocation()).toVector();
                if (toOwner.length() < 1.5) {
                    finalArrow.remove();
                    cancel();
                    return;
                }
                finalArrow.setVelocity(toOwner.normalize().multiply(2.0));
            }
        }.runTaskTimer(plugin, 5L, 2L);
    }

    private void spawnJusticeFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder()
                .withColor(C1, C2)
                .with(FireworkEffect.Type.STAR)
                .trail(true)
                .flicker(true)
                .build());
        fw.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }
}
