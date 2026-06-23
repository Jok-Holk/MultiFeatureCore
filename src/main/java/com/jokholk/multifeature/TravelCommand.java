package com.jokholk.multifeature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TravelCommand implements CommandExecutor, TabCompleter {

    private final MainPlugin plugin;

    public TravelCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean isValidId(String id, int max) {
        if (!id.startsWith("checkpoint")) return false;
        try {
            int n = Integer.parseInt(id.substring("checkpoint".length()));
            return n >= 1 && n <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<String> checkpointList(int max) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= max; i++) list.add("checkpoint" + i);
        return list;
    }

    // ── onCommand ─────────────────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!p.hasPermission("multifeature.travel")) {
            p.sendMessage("§cYou don't have permission to use fast travel.");
            return true;
        }

        CheckpointManager cm = plugin.getCheckpointManager();

        // /travel — mở GUI
        if (args.length == 0) {
            p.openInventory(GUIManager.createMenu(p, cm));
            return true;
        }

        String sub = args[0];

        // SAVE --------------------------------------------------
        if (sub.equalsIgnoreCase("save")) {

            if (!p.hasPermission("multifeature.checkpoint.save")) {
                p.sendMessage("§cYou don't have permission to save checkpoints.");
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 2) {
                p.sendMessage("§cUsage: /travel save <checkpoint1-" + max + "> [name]");
                return true;
            }

            String input = args[1];
            String id;

            if (isValidId(input, max)) {
                id = input;
            } else {
                String found = cm.findIdByName(p, input);
                if (found != null) {
                    id = found;
                } else {
                    p.sendMessage("§cInvalid checkpoint ID. Use checkpoint1 → checkpoint" + max);
                    return true;
                }
            }

            String name = args.length >= 3
                    ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)).replace("\"", "")
                    : id;

            cm.saveCheckpoint(p, id, name);
            p.sendMessage("§aSaved checkpoint §e" + name);
            return true;
        }

        // LOAD --------------------------------------------------
        if (sub.equalsIgnoreCase("load")) {

            if (!p.hasPermission("multifeature.checkpoint.load")) {
                p.sendMessage("§cYou don't have permission to load checkpoints.");
                return true;
            }

            if (args.length < 2) {
                p.sendMessage("§cUsage: /travel load <name or checkpoint>");
                return true;
            }

            String id = cm.findIdByName(p, args[1]);

            if (id == null) {
                p.sendMessage("§cCheckpoint does not exist!");
                return true;
            }

            Location target = cm.loadCheckpoint(p, id);

            if (target == null) {
                p.sendMessage("§cThis checkpoint has not been set yet!");
                return true;
            }

            p.sendMessage("§eFast travel starting... do not move");

            new BukkitRunnable() {

                int timer = 3;
                final Location start = p.getLocation();

                @Override
                public void run() {

                    if (p.getLocation().distance(start) > 0.3) {
                        p.sendMessage("§cCanceled: you moved!");
                        cancel();
                        return;
                    }

                    if (timer <= 0) {
                        p.teleport(target);
                        p.sendMessage("§aTeleported to §e" + cm.getName(p, id));
                        cancel();
                        return;
                    }

                    p.sendMessage("§7" + timer + "...");
                    timer--;
                }

            }.runTaskTimer(plugin, 0, 20);

            return true;
        }

        // DELETE ------------------------------------------------
        if (sub.equalsIgnoreCase("delete")) {

            if (!p.hasPermission("multifeature.checkpoint.save")) {
                p.sendMessage("§cYou don't have permission to delete checkpoints.");
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 2) {
                p.sendMessage("§cUsage: /travel delete <checkpoint1-" + max + ">");
                return true;
            }

            String id = args[1];

            if (!isValidId(id, max)) {
                p.sendMessage("§cYou must use a valid checkpoint ID (checkpoint1-" + max + ")!");
                return true;
            }

            if (cm.loadCheckpoint(p, id) == null) {
                p.sendMessage("§cThis checkpoint is already empty!");
                return true;
            }

            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                p.sendMessage("§eAre you sure you want to delete §c" + id + "§e?");
                p.sendMessage("§7Type: §f/travel delete " + id + " confirm");
                return true;
            }

            cm.deleteCheckpoint(p, id);
            p.sendMessage("§aCheckpoint §e" + id + " §ahas been permanently deleted.");
            return true;
        }

        // RENAME ------------------------------------------------
        if (sub.equalsIgnoreCase("name")) {

            if (!p.hasPermission("multifeature.checkpoint.rename")) {
                p.sendMessage("§cYou don't have permission to rename checkpoints.");
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 3) {
                p.sendMessage("§cUsage: /travel name <checkpoint1-" + max + "> <new name>");
                return true;
            }

            String id = args[1];

            if (!isValidId(id, max)) {
                p.sendMessage("§cYou can only rename using checkpoint ID (checkpoint1-" + max + ")!");
                return true;
            }

            if (cm.loadCheckpoint(p, id) == null) {
                p.sendMessage("§cThis checkpoint has not been set yet!");
                return true;
            }

            String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                    .replace("\"", "");

            cm.saveCheckpoint(p, id, name);
            p.sendMessage("§aCheckpoint §e" + id + " §arenamed to §e" + name);
            return true;
        }

        // SLOTS -------------------------------------------------
        if (sub.equalsIgnoreCase("slots")) {

            int current = cm.getMaxSlots(p);

            if (args.length < 2) {
                p.sendMessage("§7Your travel slots: §e" + current + " §7(max 54)");
                p.sendMessage("§7Usage: §f/travel slots <1-54>");
                return true;
            }

            int newMax;
            try {
                newMax = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage("§cInvalid number. Usage: /travel slots <1-54>");
                return true;
            }

            if (newMax < 1 || newMax > 54) {
                p.sendMessage("§cSlot count must be between §e1 §cand §e54§c.");
                return true;
            }

            int highest = cm.getHighestUsedSlot(p);
            if (newMax < highest) {
                p.sendMessage("§cCannot reduce to §e" + newMax
                        + "§c: you have a checkpoint at slot §e" + highest + "§c.");
                p.sendMessage("§7Delete §fcheckpoint" + highest
                        + " §7first: §f/travel delete checkpoint" + highest);
                return true;
            }

            cm.setMaxSlots(p, newMax);
            p.sendMessage("§aTravel slots set to §e" + newMax + "§a. Reopen §f/travel §ato see changes.");
            return true;
        }

        // ICON --------------------------------------------------
        if (sub.equalsIgnoreCase("icon")) {

            if (!p.hasPermission("multifeature.checkpoint.rename")) {
                p.sendMessage("§cYou don't have permission to customize checkpoint icons.");
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 3) {
                p.sendMessage("§cUsage: /travel icon <checkpoint1-" + max + "> <material|reset>");
                p.sendMessage("§7Example: §f/travel icon checkpoint1 minecraft:diamond_block");
                return true;
            }

            String id = args[1];
            if (!isValidId(id, max)) {
                p.sendMessage("§cInvalid checkpoint ID (checkpoint1–checkpoint" + max + ")");
                return true;
            }

            String matInput = args[2];

            if (matInput.equalsIgnoreCase("reset")) {
                cm.setIcon(p, id, null);
                p.sendMessage("§aIcon for §e" + id + " §areset to default.");
                return true;
            }

            Material mat = Material.matchMaterial(matInput);
            if (mat == null || !mat.isItem()) {
                p.sendMessage("§cUnknown material: §f" + matInput);
                p.sendMessage("§7Try: §fminecraft:grass_block §7or §fDIAMOND_BLOCK");
                return true;
            }

            cm.setIcon(p, id, mat);
            p.sendMessage("§aIcon for §e" + id + " §aset to §e" + mat.getKey() + "§a.");
            return true;
        }

        // FALLBACK ----------------------------------------------
        p.sendMessage("§cUnknown subcommand. Usage:");
        p.sendMessage("§7/travel §8— open fast travel menu");
        p.sendMessage("§7/travel save <checkpoint1-N> [name]");
        p.sendMessage("§7/travel load <name or id>");
        p.sendMessage("§7/travel name <checkpoint1-N> <new name>");
        p.sendMessage("§7/travel delete <checkpoint1-N>");
        p.sendMessage("§7/travel slots <1-54> §8— set your slot count");
        p.sendMessage("§7/travel icon <checkpoint> <material|reset> §8— set slot icon");
        return true;
    }

    // ── Tab complete ──────────────────────────────────────────────────────────

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String label, String[] args) {

        if (!(sender instanceof Player p)) return Collections.emptyList();
        if (!p.hasPermission("multifeature.travel")) return Collections.emptyList();

        CheckpointManager cm = plugin.getCheckpointManager();

        if (args.length == 1) {
            return List.of("save", "load", "name", "delete", "slots", "icon").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String sub = args[0].toLowerCase();
        int max = cm.getMaxSlots(p);

        if (args.length == 2 && (sub.equals("save") || sub.equals("load")
                || sub.equals("name") || sub.equals("delete") || sub.equals("icon"))) {
            return checkpointList(max).stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && sub.equals("delete")) {
            return List.of("confirm").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && sub.equals("icon")) {
            return List.of("reset").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
