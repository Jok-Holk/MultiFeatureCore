package com.jokholk.multifeature;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class for all divine weapons that use a charge-and-release mechanic.
 * Subclasses: ExcaliburListener, RagnarokListener, IgnisListener, GraveListener, VoidBowListener
 */
public abstract class DivineWeaponListener implements Listener {

    protected final MainPlugin plugin;

    // chargeStart: time in ms when player started charging
    private final Map<UUID, Long>        chargeStart  = new HashMap<>();
    private final Map<UUID, BukkitTask>  chargeTasks  = new HashMap<>();
    private final Map<UUID, Long>        cooldowns    = new HashMap<>();

    protected DivineWeaponListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // ─── Abstract methods subclasses must implement ───

    protected abstract boolean isWeapon(ItemStack item);
    protected abstract boolean isOwner(Player p, ItemStack item);
    protected abstract double  getMaxChargeSecs();
    protected abstract double  getCdMultiplier();
    /** Called every 5 ticks during charge with current charge ratio (0.0–1.0) */
    protected abstract void onChargeVisual(Player p, double ratio);
    /** Called when player releases the charge */
    protected abstract void castSkill(Player p, double ratio, double chargedSecs);

    // ─── Anti-theft pickup handler ───

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        ItemStack item = e.getItem().getItemStack();
        if (!isWeapon(item)) return;

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

    // ─── Right-click to charge / release ───

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        var action = e.getAction();
        boolean isRightClick = action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                || action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
        if (!isRightClick) return;

        ItemStack held = e.getItem();
        if (!isWeapon(held)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();

        // Check cooldown
        if (cooldowns.containsKey(p.getUniqueId())) {
            long remaining = cooldowns.get(p.getUniqueId()) - System.currentTimeMillis();
            if (remaining > 0) {
                p.sendMessage("§cCooldown: §e%.1fs".formatted(remaining / 1000.0));
                return;
            } else {
                cooldowns.remove(p.getUniqueId());
            }
        }

        if (!chargeStart.containsKey(p.getUniqueId())) {
            startCharge(p);
        } else {
            releaseCharge(p);
        }
    }

    // ─── Cancel charge when switching item ───

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        if (chargeStart.containsKey(e.getPlayer().getUniqueId())) {
            cancelCharge(e.getPlayer());
        }
    }

    // ─── Charge lifecycle ───

    private void startCharge(Player p) {
        UUID uid = p.getUniqueId();
        chargeStart.put(uid, System.currentTimeMillis());
        p.sendMessage("§6Charging... right-click again to release.");

        final int[] ticks = {0};
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!p.isOnline() || !isHoldingWeapon(p)) {
                cancelCharge(p);
                return;
            }
            ticks[0]++;
            if (ticks[0] % 5 == 0) {
                onChargeVisual(p, getChargeRatio(uid));
            }
            if (ticks[0] >= (int)(getMaxChargeSecs() * 20)) {
                releaseCharge(p);
            }
        }, 1L, 1L);
        chargeTasks.put(uid, task);
    }

    private void releaseCharge(Player p) {
        UUID uid = p.getUniqueId();
        Long startMs = chargeStart.remove(uid);
        BukkitTask task = chargeTasks.remove(uid);
        if (task != null) task.cancel();
        if (startMs == null) return;

        double chargedSecs = Math.min((System.currentTimeMillis() - startMs) / 1000.0, getMaxChargeSecs());
        double ratio = chargedSecs / getMaxChargeSecs();

        // Set cooldown
        long cdMs = (long)(chargedSecs * getCdMultiplier() * 1000L);
        cooldowns.put(uid, System.currentTimeMillis() + cdMs);

        castSkill(p, ratio, chargedSecs);
    }

    protected void cancelCharge(Player p) {
        UUID uid = p.getUniqueId();
        chargeStart.remove(uid);
        BukkitTask task = chargeTasks.remove(uid);
        if (task != null) task.cancel();
    }

    protected double getChargeRatio(UUID uid) {
        Long startMs = chargeStart.get(uid);
        if (startMs == null) return 0.0;
        return Math.min((System.currentTimeMillis() - startMs) / (getMaxChargeSecs() * 1000.0), 1.0);
    }

    protected boolean isHoldingWeapon(Player p) {
        return isWeapon(p.getInventory().getItemInMainHand());
    }

    // ─── Shared helpers for subclasses ───

    protected void spawnFirework(Location loc, Color c1, Color c2, FireworkEffect.Type type, boolean trail) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        var meta = fw.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(
                FireworkEffect.builder()
                        .withColor(c1, c2)
                        .with(type)
                        .trail(trail)
                        .build()
        );
        fw.setFireworkMeta(meta);
        Bukkit.getScheduler().runTaskLater(plugin, fw::detonate, 1L);
    }

    protected void breakBlockSilent(Block block) {
        Material t = block.getType();
        if (t == Material.AIR || t == Material.BEDROCK || t == Material.BARRIER) return;
        block.setType(Material.AIR);
    }
}