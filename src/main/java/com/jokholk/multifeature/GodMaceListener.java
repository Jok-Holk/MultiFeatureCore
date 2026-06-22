package com.jokholk.multifeature;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GodMaceListener implements Listener {

    private final MainPlugin plugin;

    // Người đang ở trạng thái “thần phán”
    private final Set<UUID> smashMode = new HashSet<>();

    public GodMaceListener(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // ======================================================
    // KIỂM TRA ITEM
    // ======================================================

    private boolean isGodMace(ItemStack i) {
        if (i == null) return false;
        if (i.getType() != Material.MACE) return false;
        if (!i.hasItemMeta()) return false;

        return "§x§F§B§D§A§0§0✦ GOD MACE ✦"
                .equals(i.getItemMeta().getDisplayName());
    }

    private boolean isOwner(Player p, ItemStack i) {

        ItemMeta m = i.getItemMeta();
        List<String> lore = m.getLore();

        if (lore == null) return false;

        String last = lore.get(lore.size() - 1);

        return last.contains(p.getUniqueId().toString());
    }

    // ======================================================
    // TRỪNG PHẠT KẺ TRỘM
    // ======================================================

    private void punishThief(Player p, ItemStack i) {

        // 1. Xóa trong inventory
        p.getInventory().remove(i);

        // 2. Xóa mọi item rơi xung quanh
        p.getWorld().getNearbyEntities(
                p.getLocation(), 6, 6, 6
        ).forEach(ent -> {

            if (ent instanceof Item item) {
                if (isGodMace(item.getItemStack())) {
                    item.remove();
                }
            }

        });

        // 3. Kick
        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (p.isOnline()) {
                p.kickPlayer(
                        "§cThis relic does not belong to you\n" +
                                "§6The gods reject your touch"
                );
            }

        }, 1L);
    }

    // ======================================================
    // CHẶN NHẶT KHÔNG THUỘC CHỦ
    // ======================================================

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {

        ItemStack i = e.getItem().getItemStack();

        if (!isGodMace(i)) return;

        // Mob nhặt → cancel, không kick
        if (!(e.getEntity() instanceof Player p)) {
            e.setCancelled(true);
            return;
        }

        if (!isOwner(p, i)) {

            e.getItem().remove();
            e.setCancelled(true);

            punishThief(p, i);
        }
    }

    // ======================================================
    // CHUỘT PHẢI → KÍCH HOẠT SMASH
    // ======================================================

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {

        Action a = e.getAction();

        if (a != Action.RIGHT_CLICK_AIR &&
                a != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack i = e.getItem();
        if (!isGodMace(i)) return;

        Player p = e.getPlayer();

        // kiểm ownership
        if (!isOwner(p, i)) {
            punishThief(p, i);
            return;
        }

        // bay lên
        p.setVelocity(new Vector(0, 3.2, 0));

        smashMode.add(p.getUniqueId());

        p.playSound(p.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_FLAP,
                1f, 1f);

        startWatcher(p);

        e.setCancelled(true);
    }

    // ======================================================
    // WATCHER – KHÔNG DỰA DAMAGE
    // ======================================================

    private void startWatcher(Player p) {

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {

            if (!p.isOnline() ||
                    !smashMode.contains(p.getUniqueId())) {

                task.cancel();
                return;
            }

            // đang rơi
            if (p.getVelocity().getY() < -0.25) {

                Player target = findTarget(p);

                if (target != null) {
                    executeJudgement(p, target);
                    task.cancel();
                }
            }

        }, 2L, 2L);
    }

    // ======================================================
    // TÌM MỤC TIÊU TRƯỚC MẶT
    // ======================================================

    private Player findTarget(Player p) {

        for (Entity e :
                p.getNearbyEntities(2.2, 3, 2.2)) {

            if (e instanceof Player victim &&
                    !victim.equals(p)) {

                return victim;
            }
        }

        return null;
    }

    // ======================================================
    // PHÁN QUYẾT THẦN THÁNH
    // ======================================================

    private void executeJudgement(Player damager, Player victim) {

        Location loc = victim.getLocation();

        // Sét
        loc.getWorld().strikeLightningEffect(loc);

        // Pháo hoa thánh thần
        for (int i = 0; i < 4; i++) {
            spawnFirework(loc.clone().add(
                    Math.cos(i * Math.PI/2) * 2,
                    0,
                    Math.sin(i * Math.PI/2) * 2
            ));
        }

        // Kick
        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (victim.isOnline()) {

                victim.kickPlayer(
                        "§6✞ YOU HAVE BEEN JUDGED BY GOD ✞\n\n" +
                                "§eReturn if you dare..."
                );
            }

        }, 1L);

        smashMode.remove(damager.getUniqueId());
    }

    // ======================================================
    // CHỐNG FALL DAMAGE
    // ======================================================

    @EventHandler
    public void onFall(EntityDamageEvent e) {

        if (!(e.getEntity() instanceof Player p))
            return;

        if (e.getCause() ==
                EntityDamageEvent.DamageCause.FALL &&
                smashMode.contains(p.getUniqueId())) {

            e.setCancelled(true);
            smashMode.remove(p.getUniqueId());
        }
    }

    // ======================================================
    // FIREWORK VÀNG CAM + STAR
    // ======================================================

    private void spawnFirework(Location loc) {

        Firework fw =
                loc.getWorld().spawn(loc, Firework.class);

        var meta = fw.getFireworkMeta();

        meta.addEffect(
                FireworkEffect.builder()
                        .withColor(Color.YELLOW, Color.ORANGE)
                        .with(FireworkEffect.Type.STAR)
                        .trail(true)
                        .flicker(true)
                        .build()
        );

        meta.setPower(0);
        fw.setFireworkMeta(meta);

        Bukkit.getScheduler().runTaskLater(
                plugin, fw::detonate, 1L
        );
    }
}
