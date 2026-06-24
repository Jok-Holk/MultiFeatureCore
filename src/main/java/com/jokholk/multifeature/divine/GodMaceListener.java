package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GodMaceListener implements Listener {

    private final MainPlugin plugin;

    private final Set<UUID>            smashMode   = new HashSet<>();
    private final Map<UUID, BukkitTask> watcherTasks = new ConcurrentHashMap<>();

    public GodMaceListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Item checks ───

    private boolean isGodMace(ItemStack i) {
        if (i == null) return false;
        if (i.getType() != Material.MACE) return false;
        if (!i.hasItemMeta()) return false;
        return "§x§F§B§D§A§0§0✦ GOD MACE ✦".equals(i.getItemMeta().getDisplayName());
    }

    private boolean isOwner(Player p, ItemStack i) {
        ItemMeta m = i.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        return lore.get(lore.size() - 1).contains(p.getUniqueId().toString());
    }

    // ─── Cleanup on quit ───

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uid = e.getPlayer().getUniqueId();
        smashMode.remove(uid);
        BukkitTask t = watcherTasks.remove(uid);
        if (t != null) t.cancel();
    }

    // ─── Thief punishment ───

    private void punishThief(Player p, ItemStack i) {
        p.getInventory().remove(i);
        p.getWorld().getNearbyEntities(p.getLocation(), 6, 6, 6).forEach(ent -> {
            if (ent instanceof Item item && isGodMace(item.getItemStack())) item.remove();
        });
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (p.isOnline()) {
                p.kickPlayer(Msg.GODMACE_KICK_THEFT.get(p));
            }
        }, 1L);
    }

    // ─── Anti-theft ───

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        ItemStack i = e.getItem().getItemStack();
        if (!isGodMace(i)) return;
        if (!(e.getEntity() instanceof Player p)) { e.setCancelled(true); return; }
        if (!isOwner(p, i)) {
            e.getItem().remove();
            e.setCancelled(true);
            punishThief(p, i);
        }
    }

    // ─── Right-click: ascend ───

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        ItemStack i = e.getItem();
        if (!isGodMace(i)) return;
        Player p = e.getPlayer();
        if (!isOwner(p, i)) { punishThief(p, i); return; }

        // Launch
        p.setVelocity(new Vector(0, 3.2, 0));
        smashMode.add(p.getUniqueId());

        // ─── Launch VFX ───
        Location loc = p.getLocation();
        loc.getWorld().spawnParticle(Particle.CRIT,     loc.clone().add(0, 1, 0), 25, 0.4, 0.6, 0.4, 0.4);
        loc.getWorld().spawnParticle(Particle.ENCHANT,  loc.clone().add(0, 0.5, 0), 20, 0.5, 1.0, 0.5, 0.5);
        loc.getWorld().spawnParticle(Particle.END_ROD,  loc.clone().add(0, 0.2, 0), 10, 0.3, 0.1, 0.3, 0.1);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.0f);
        loc.getWorld().playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.3f);

        startWatcher(p);
        e.setCancelled(true);
    }

    // ─── Watcher: falling trail + target detection ───

    private void startWatcher(Player p) {
        UUID uid = p.getUniqueId();
        BukkitTask old = watcherTasks.remove(uid);
        if (old != null) old.cancel();

        BukkitTask task = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline() || !smashMode.contains(uid)) {
                    cancel();
                    watcherTasks.remove(uid);
                    return;
                }

                if (p.getVelocity().getY() < -0.25) {
                    // ─── Falling trail ───
                    Location pLoc = p.getLocation();
                    pLoc.getWorld().spawnParticle(Particle.ENCHANT, pLoc.clone().add(0, 0.5, 0),
                            5, 0.3, 0.4, 0.3, 0.2);
                    pLoc.getWorld().spawnParticle(Particle.CRIT, pLoc.clone().add(0, 0.3, 0),
                            3, 0.2, 0.2, 0.2, 0.1);

                    Player target = findTarget(p);
                    if (target != null) {
                        cancel();
                        watcherTasks.remove(uid);
                        executeJudgement(p, target);
                    }
                }
            }
        }.runTaskTimer(plugin, 2L, 2L);
        watcherTasks.put(uid, task);
    }

    // ─── Find nearby player ───

    private Player findTarget(Player p) {
        for (Entity e : p.getNearbyEntities(2.2, 3, 2.2)) {
            if (e instanceof Player victim && !victim.equals(p)) return victim;
        }
        return null;
    }

    // ─── Divine Judgement ───

    private void executeJudgement(Player damager, Player victim) {
        Location loc   = victim.getLocation();
        World    world = loc.getWorld();

        // Lightning × 3 at offset positions
        world.strikeLightningEffect(loc);
        world.strikeLightningEffect(loc.clone().add(1.5, 0,  0));
        world.strikeLightningEffect(loc.clone().add(-1.5, 0, 0));

        // ─── Judgment VFX ───
        // TOTEM burst
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 80, 0.6, 1.0, 0.6, 0.5);
        // CRIT rain
        world.spawnParticle(Particle.CRIT,             loc.clone().add(0, 2, 0), 40, 1.0, 0.8, 1.0, 0.4);
        // ENCHANT shower
        world.spawnParticle(Particle.ENCHANT,          loc.clone().add(0, 2.5, 0), 50, 1.2, 0.5, 1.2, 0.6);
        // END_ROD ring
        for (int i = 0; i < 12; i++) {
            double angle = i * 2 * Math.PI / 12;
            Location rLoc = loc.clone().add(Math.cos(angle) * 2.0, 0.5, Math.sin(angle) * 2.0);
            world.spawnParticle(Particle.END_ROD, rLoc, 3, 0.1, 0.4, 0.1, 0.08);
        }

        // ─── Judgment fireworks ───
        spawnFirework(loc.clone().add(0, 0.5, 0), FireworkEffect.Type.BURST);
        for (int i = 0; i < 6; i++) {
            final int fi = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double angle = fi * Math.PI / 3;
                spawnFirework(loc.clone().add(Math.cos(angle) * 2.5, 0.5, Math.sin(angle) * 2.5),
                        FireworkEffect.Type.STAR);
            }, fi * 2L);
        }

        // ─── Judgment sounds ───
        world.playSound(loc, Sound.ITEM_TOTEM_USE,              1.0f, 0.8f);
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 0.7f);
        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL,   0.8f, 0.5f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (victim.isOnline()) {
                victim.kickPlayer(Msg.GODMACE_KICK_VERDICT.get(victim));
            }
        }, 1L);

        smashMode.remove(damager.getUniqueId());
    }

    // ─── Fall damage immunity ───

    @EventHandler
    public void onFall(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (e.getCause() == EntityDamageEvent.DamageCause.FALL &&
                smashMode.contains(p.getUniqueId())) {
            e.setCancelled(true);
            smashMode.remove(p.getUniqueId());
        }
    }

    // ─── Firework helpers ───

    private void spawnFirework(Location loc, FireworkEffect.Type type) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        var meta = fw.getFireworkMeta();
        meta.addEffect(
                FireworkEffect.builder()
                        .withColor(Color.YELLOW, Color.ORANGE, Color.WHITE)
                        .with(type)
                        .trail(true)
                        .flicker(true)
                        .build()
        );
        meta.setPower(0);
        fw.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }
}
