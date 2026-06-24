package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

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
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract base class for all divine weapons that use a charge-and-release mechanic.
 */
public abstract class DivineWeaponListener implements Listener {

    protected final MainPlugin plugin;

    private final Map<UUID, Long>        chargeStart  = new HashMap<>();
    private final Map<UUID, BukkitTask>  chargeTasks  = new HashMap<>();
    private final Map<UUID, Long>        cooldowns    = new HashMap<>();

    protected DivineWeaponListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    protected abstract boolean isWeapon(ItemStack item);
    protected abstract boolean isOwner(Player p, ItemStack item);
    protected abstract double  getMaxChargeSecs();
    protected abstract double  getCdMultiplier();
    protected abstract String  getTheftKickMessage(Player victim);
    protected abstract void onChargeVisual(Player p, double ratio);
    protected abstract void castSkill(Player p, double ratio, double chargedSecs);

    // ─── Anti-theft ───

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
                    p.kickPlayer(getTheftKickMessage(p));
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

        if (cooldowns.containsKey(p.getUniqueId())) {
            long remaining = cooldowns.get(p.getUniqueId()) - System.currentTimeMillis();
            if (remaining > 0) {
                // Format cooldown message using player's language
                Language lang = LanguageManager.getLang(p);
                String raw = Msg.DIVINE_COOLDOWN.get(lang);
                p.sendMessage(raw.formatted(remaining / 1000.0));
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

    // ─── Cancel on item switch or quit ───

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        if (chargeStart.containsKey(e.getPlayer().getUniqueId())) {
            cancelCharge(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uid = e.getPlayer().getUniqueId();
        if (chargeStart.containsKey(uid)) {
            cancelCharge(e.getPlayer());
        }
        cooldowns.remove(uid);
    }

    // ─── Charge lifecycle ───

    private void startCharge(Player p) {
        UUID uid = p.getUniqueId();
        chargeStart.put(uid, System.currentTimeMillis());
        p.sendMessage(Msg.DIVINE_CHARGING.get(p));

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

    // ─── Shared helpers ───

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

    private static final Set<Material> UNBREAKABLE = Set.of(
        Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
        Material.BEDROCK, Material.BARRIER,
        Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
        Material.STRUCTURE_BLOCK, Material.JIGSAW,
        Material.END_PORTAL, Material.END_PORTAL_FRAME,
        Material.NETHER_PORTAL
    );

    protected void breakBlockSilent(Block block) {
        if (UNBREAKABLE.contains(block.getType())) return;
        block.setType(Material.AIR);
    }
}
