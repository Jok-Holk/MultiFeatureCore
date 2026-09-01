package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NothanListener extends DivineWeaponListener {

    static final double MAX_CHARGE    = 4.0;
    static final double MAX_CONE_HALF = 55.0; // degrees
    static final double MAX_RANGE     = 50.0;
    static final double MAX_DAMAGE    = 90.0; // was 30.0 — that's the base, not the max ("30 + 60*ratio")

    private static final Color C1 = Color.fromRGB(255, 200, 30);
    private static final Color C2 = Color.fromRGB(255, 140, 0);

    // cos(45°) = 0.707
    private static final double COS_MIN = Math.cos(Math.toRadians(MAX_CONE_HALF));

    // Players currently holding a fully-loaded shot, ready to fire on their
    // NEXT right-click (a separate click, not a continued hold).
    private final Set<UUID> loaded = new HashSet<>();

    public NothanListener(MainPlugin plugin) {
        super(plugin);
    }

    // Real crossbow UX: click 1 (hold) loads; click 2 (separate, while
    // loaded) fires immediately with no further charging. Intercept before
    // the base class's onInteract so a loaded weapon doesn't start a fresh
    // load cycle on the firing click.
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        var action = e.getAction();
        boolean isRight = action == org.bukkit.event.block.Action.RIGHT_CLICK_AIR
                       || action == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;
        if (!isRight) return;

        ItemStack held = e.getItem();
        if (!isWeapon(held)) return;

        Player p = e.getPlayer();
        if (loaded.remove(p.getUniqueId())) {
            e.setUseInteractedBlock(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            fireLoadedShot(p);
            return;
        }

        super.onInteract(e);
    }

    @Override
    protected boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.CROSSBOW) return false;
        if (!item.hasItemMeta()) return false;
        return NothanCommand.DISPLAY_NAME.equals(item.getItemMeta().getDisplayName());
    }

    @Override
    protected boolean isOwner(Player p, ItemStack item) {
        ItemMeta m = item.getItemMeta();
        if (m == null || m.getLore() == null) return false;
        List<String> lore = m.getLore();
        return lore.get(lore.size() - 1).contains(p.getUniqueId().toString());
    }

    @Override
    protected double getMaxChargeSecs() { return MAX_CHARGE; }

    @Override
    protected double getCdMultiplier()  { return 0.8; }

    // Real crossbow semantics instead of a variable-power charge: hold to
    // load (fixed MAX_CHARGE duration) -> fires automatically at full power
    // the instant it's loaded. Releasing before it's loaded cancels outright
    // (no partial-power shot) -- "lên nòng và bắn", 2 fixed stages.
    @Override
    protected boolean autoFireAtMaxCharge()   { return true; }
    @Override
    protected boolean cancelIfReleasedEarly() { return true; }

    @Override
    protected String getTheftKickMessage(Player victim) {
        return Msg.NOTHAN_KICK_THEFT.get(victim);
    }

    // The item is a real Material.CROSSBOW, so vanilla's own load/fire mechanic
    // (sped up by Quick Charge) runs independently of our Consumable-based
    // charge, and can complete + fire a real bolt on top of castSkill()'s cone
    // effect -- "1 charge, 2 shots", sometimes even from a single quick click.
    // Cancelling the shoot event alone wasn't enough, so also strip
    // CHARGED_PROJECTILES the moment it appears, so the crossbow can never
    // actually finish vanilla-loading in the first place.
    @EventHandler
    public void onVanillaShoot(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!isWeapon(e.getBow())) return;
        e.setCancelled(true);
        stripChargedProjectiles(p);
    }

    private void stripChargedProjectiles(Player p) {
        ItemStack held = p.getInventory().getItemInMainHand();
        if (!isWeapon(held)) return;
        if (held.hasData(DataComponentTypes.CHARGED_PROJECTILES)) {
            held.unsetData(DataComponentTypes.CHARGED_PROJECTILES);
            p.getInventory().setItemInMainHand(held);
        }
    }

    @Override
    protected void onChargeStart(Player p) {
        // Model stays on the idle/unloaded state while loading is in progress
        // (onChargeVisual's particles carry the "charging up" feedback) --
        // it only swaps to the loaded model once loading actually completes,
        // in castSkill() below.
        stripChargedProjectiles(p);
    }

    @Override
    protected void onChargeEnd(Player p) {
        // Only reached via an early-release cancel (cancelIfReleasedEarly) --
        // a successful load doesn't go through here with an unloaded model,
        // since the model was never swapped to charged in the first place.
        stripChargedProjectiles(p);
    }

    private void swapModel(Player p, String modelKey) {
        ItemStack held = p.getInventory().getItemInMainHand();
        if (!isWeapon(held)) return;
        ItemMeta m = held.getItemMeta();
        m.setItemModel(new NamespacedKey("multifeature", modelKey));
        held.setItemMeta(m);
        p.getInventory().setItemInMainHand(held);
    }

    @Override
    protected void onChargeVisual(Player p, double ratio) {
        // Vanilla's own (Quick-Charge-sped-up) crossbow loading can complete
        // mid-charge, well before our custom release -- keep stripping it
        // throughout the hold, not just at start/end.
        stripChargedProjectiles(p);

        World  world = p.getWorld();
        Vector dir   = p.getEyeLocation().getDirection().normalize();

        // Golden ENCHANT sparks radiating in a forward cone
        int sparks = 4 + (int)(ratio * 8);
        for (int i = 0; i < sparks; i++) {
            double spread = 0.7 * (1 - ratio * 0.4); // cone tightens
            Vector v = dir.clone()
                    .add(new Vector(
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread))
                    .normalize().multiply(1.5 + ratio);
            Location pLoc = p.getEyeLocation().clone().add(v);
            world.spawnParticle(Particle.ENCHANT, pLoc, 2, 0.05, 0.05, 0.05, 0.05);
        }
        // CRIT particles at muzzle
        world.spawnParticle(Particle.CRIT, p.getEyeLocation().clone().add(dir.clone().multiply(1.5)),
                3, 0.1, 0.1, 0.1, 0.1);
        // Golden glow ring at high charge
        if (ratio > 0.5) {
            double r = 0.8;
            double angle0 = (System.currentTimeMillis() / 200.0) % (2 * Math.PI);
            for (int i = 0; i < 8; i++) {
                double a = angle0 + i * 2 * Math.PI / 8;
                Location loc = p.getLocation().clone().add(Math.cos(a) * r, 1.0, Math.sin(a) * r);
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 1, 0, 0, 0, 0);
            }
        }
    }

    // Called by the base class the instant loading completes (ratio hits
    // 1.0 via autoFireAtMaxCharge). This does NOT fire -- it just marks the
    // weapon "loaded" and shows the loaded model; the actual shot happens in
    // fireLoadedShot() on the player's next, separate right-click.
    @Override
    protected void castSkill(Player p, double ratio, double chargedSecs) {
        loaded.add(p.getUniqueId());
        swapModel(p, "item/no_than_charged");
        p.playSound(p.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
        p.sendMessage(Msg.NOTHAN_LOADED.get(p));
    }

    private void fireLoadedShot(Player p) {
        swapModel(p, "item/no_than");

        // Fixed power -- no charging involved in the firing click itself.
        double range  = MAX_RANGE;
        double damage = MAX_DAMAGE;
        int    sickTicks = 120;

        Location eye  = p.getEyeLocation();
        Vector   look = eye.getDirection().normalize();
        World    world = eye.getWorld();

        // ─── MUZZLE BLAST VFX ───
        Location muzzle = eye.clone().add(look.clone().multiply(1.5));
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, muzzle, 60, 0.6, 0.6, 0.6, 0.5);
        world.spawnParticle(Particle.ENCHANT,          muzzle, 40, 0.8, 0.8, 0.8, 0.4);
        world.spawnParticle(Particle.CRIT,             muzzle, 25, 0.5, 0.5, 0.5, 0.3);
        // Cone particle stream
        int streamPts = 32;
        for (int i = 0; i < streamPts; i++) {
            double spread = 0.5 * (i / (double)streamPts);
            double dist   = range * (i / (double)streamPts);
            Vector sv = look.clone()
                    .add(new Vector(
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread,
                            (Math.random() - 0.5) * spread))
                    .normalize().multiply(dist);
            Location pt = eye.clone().add(sv);
            world.spawnParticle(Particle.TOTEM_OF_UNDYING, pt, 3, 0.2, 0.2, 0.2, 0.08);
            world.spawnParticle(Particle.ENCHANT,          pt, 2, 0.15, 0.15, 0.15, 0.05);
        }

        // ─── Firework at muzzle ───
        spawnFirework(muzzle, C1, C2, FireworkEffect.Type.BURST, true);

        // ─── SOUNDS ───
        world.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER,      0.9f, 0.7f);
        world.playSound(p.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.3f);
        world.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.7f, 0.8f);

        // ─── CONE ENTITY QUERY ───
        // Đặt query sphere bao gồm toàn bộ cone
        Set<LivingEntity> targets = new HashSet<>();
        world.getNearbyEntities(eye, range, range, range).stream()
                .filter(e -> e instanceof LivingEntity && e != p)
                .filter(e -> {
                    Vector toEnt = e.getLocation().toVector().subtract(eye.toVector()).normalize();
                    double dot = look.dot(toEnt);
                    return dot >= COS_MIN;
                })
                .map(e -> (LivingEntity) e)
                .forEach(targets::add);

        for (LivingEntity target : targets) {
            target.damage(damage, p);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,  sickTicks, 2));
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,  sickTicks, 1));
            target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA,    sickTicks, 0));
            target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING,   160,       0));

            // Per-entity hit VFX
            Location tLoc = target.getLocation().clone().add(0, 1, 0);
            tLoc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, tLoc, 25, 0.4, 0.6, 0.4, 0.35);
            tLoc.getWorld().spawnParticle(Particle.ENCHANT,          tLoc, 15, 0.3, 0.4, 0.3, 0.2);
            tLoc.getWorld().spawnParticle(Particle.CRIT,             tLoc, 10, 0.3, 0.4, 0.3, 0.15);
            spawnFirework(tLoc.clone().add(0, 0.5, 0), C1, C2, FireworkEffect.Type.STAR, false);
        }

        p.sendMessage(Msg.NOTHAN_CAST.fmt(p, "count", targets.size()));
    }
}
