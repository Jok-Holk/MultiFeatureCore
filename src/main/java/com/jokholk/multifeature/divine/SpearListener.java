package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpearListener implements Listener {

    private final MainPlugin plugin;

    // UUID -> last lunge timestamp (ms)
    private final Map<UUID, Long> lungeCooldown = new ConcurrentHashMap<>();

    private static final long   COOLDOWN_MS  = 3_000L;
    private static final double LUNGE_POWER  = 2.8;
    private static final double EXTRA_DAMAGE = 30.0;

    private static final Color C1 = Color.fromRGB(255, 220, 0);
    private static final Color C2 = Color.fromRGB(255, 160, 0);

    public SpearListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isSpear(ItemStack item) {
        if (item == null || item.getType() != Material.NETHERITE_SPEAR) return false;
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
        lungeCooldown.remove(e.getPlayer().getUniqueId());
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
                if (p.isOnline()) p.kickPlayer(Msg.SPEAR_KICK_THEFT.get(p));
            }, 1L);
        }
    }

    // ─── Right-click: lunge ───

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR
                && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!isSpear(item)) return;

        if (!isOwner(p, item)) return;

        long now = System.currentTimeMillis();
        Long last = lungeCooldown.get(p.getUniqueId());
        if (last != null && now - last < COOLDOWN_MS) return;
        lungeCooldown.put(p.getUniqueId(), now);

        // Lunge forward with slight upward kick
        Vector dir = p.getEyeLocation().getDirection().normalize();
        p.setVelocity(dir.multiply(LUNGE_POWER).add(new Vector(0, 0.25, 0)));

        // Launch VFX
        Location loc = p.getLocation();
        p.getWorld().spawnParticle(Particle.ENCHANT,    loc.clone().add(0, 1, 0), 30, 0.4, 0.4, 0.4, 0.6);
        p.getWorld().spawnParticle(Particle.CRIT,       loc.clone().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.4);
        p.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 10, 0.2, 0.4, 0.2, 0.3);
        p.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.7f);
        p.getWorld().playSound(loc, Sound.ITEM_TRIDENT_THROW,          0.8f, 1.3f);

        // Trail for 20 ticks while lunging
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!p.isOnline() || ticks++ >= 20) { cancel(); return; }
                Location cur = p.getLocation().clone().add(0, 1, 0);
                cur.getWorld().spawnParticle(Particle.ENCHANT, cur, 6, 0.2, 0.2, 0.2, 0.3);
                cur.getWorld().spawnParticle(Particle.CRIT,    cur, 3, 0.15, 0.15, 0.15, 0.2);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ─── Hit: extra damage + debuffs + VFX ───

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (!isSpear(item)) return;
        if (!isOwner(p, item)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

        e.setDamage(e.getDamage() + EXTRA_DAMAGE);

        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  100, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,  60, 0));
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,   160, 0));

        // Impact VFX
        Location tLoc = target.getLocation().clone().add(0, 1, 0);
        tLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, tLoc, 40, 0.6, 0.8, 0.6, 0.45);
        tLoc.getWorld().spawnParticle(Particle.ENCHANT,          tLoc, 25, 0.5, 0.6, 0.5, 0.35);
        tLoc.getWorld().spawnParticle(Particle.CRIT,             tLoc, 20, 0.4, 0.5, 0.4, 0.3);
        tLoc.getWorld().playSound(tLoc, Sound.ENTITY_PLAYER_LEVELUP,   0.7f, 1.4f);
        tLoc.getWorld().playSound(tLoc, Sound.BLOCK_ANVIL_LAND,        0.4f, 1.6f);
        spawnFirework(tLoc);

        // Kick SURVIVAL player
        if (target instanceof Player victim
                && victim.getGameMode() == GameMode.SURVIVAL) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (victim.isOnline()) victim.kickPlayer(Msg.SPEAR_KICK_HIT.get(victim));
            }, 1L);
        }
    }

    private void spawnFirework(Location loc) {
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
