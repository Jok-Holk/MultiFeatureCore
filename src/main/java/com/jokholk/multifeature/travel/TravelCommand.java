package com.jokholk.multifeature.travel;
import com.jokholk.multifeature.*;

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

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage(Msg.ONLY_PLAYERS.get());
            return true;
        }

        if (!p.hasPermission("multifeature.travel")) {
            p.sendMessage(Msg.TRAVEL_NO_PERM.get(p));
            return true;
        }

        CheckpointManager cm = plugin.getCheckpointManager();

        if (args.length == 0) {
            p.openInventory(GUIManager.createMenu(p, cm));
            return true;
        }

        String sub = args[0];

        // SAVE
        if (sub.equalsIgnoreCase("save")) {

            if (!p.hasPermission("multifeature.checkpoint.save")) {
                p.sendMessage(Msg.TRAVEL_NO_PERM_SAVE.get(p));
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 2) {
                p.sendMessage(Msg.TRAVEL_SAVE_USAGE.fmt(p, "max", max));
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
                    p.sendMessage(Msg.TRAVEL_INVALID_ID.fmt(p, "max", max));
                    return true;
                }
            }

            String name = args.length >= 3
                    ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)).replace("\"", "")
                    : id;

            cm.saveCheckpoint(p, id, name);
            p.sendMessage(Msg.TRAVEL_SAVED.fmt(p, "name", name));
            return true;
        }

        // LOAD
        if (sub.equalsIgnoreCase("load")) {

            if (!p.hasPermission("multifeature.checkpoint.load")) {
                p.sendMessage(Msg.TRAVEL_NO_PERM_LOAD.get(p));
                return true;
            }

            if (args.length < 2) {
                p.sendMessage(Msg.TRAVEL_LOAD_USAGE.get(p));
                return true;
            }

            String id = cm.findIdByName(p, args[1]);

            if (id == null) {
                p.sendMessage(Msg.TRAVEL_CHECKPOINT_NOT_EXIST.get(p));
                return true;
            }

            Location target = cm.loadCheckpoint(p, id);

            if (target == null) {
                p.sendMessage(Msg.TRAVEL_CHECKPOINT_NOT_SET.get(p));
                return true;
            }

            p.sendMessage(Msg.TRAVEL_STARTING.get(p));

            new BukkitRunnable() {

                int timer = 3;
                final Location start = p.getLocation();

                @Override
                public void run() {
                    if (p.getLocation().distance(start) > 0.3) {
                        p.sendMessage(Msg.TRAVEL_CANCELLED_MOVED.get(p));
                        cancel();
                        return;
                    }

                    if (timer <= 0) {
                        p.teleport(target);
                        p.sendMessage(Msg.TRAVEL_TELEPORTED.fmt(p, "name", cm.getName(p, id)));
                        cancel();
                        return;
                    }

                    p.sendMessage(Msg.TRAVEL_COUNTDOWN.fmt(p, "timer", timer));
                    timer--;
                }

            }.runTaskTimer(plugin, 0, 20);

            return true;
        }

        // DELETE
        if (sub.equalsIgnoreCase("delete")) {

            if (!p.hasPermission("multifeature.checkpoint.save")) {
                p.sendMessage(Msg.TRAVEL_NO_PERM_DELETE.get(p));
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 2) {
                p.sendMessage(Msg.TRAVEL_DELETE_USAGE.fmt(p, "max", max));
                return true;
            }

            String id = args[1];

            if (!isValidId(id, max)) {
                p.sendMessage(Msg.TRAVEL_INVALID_ID_DELETE.fmt(p, "max", max));
                return true;
            }

            if (cm.loadCheckpoint(p, id) == null) {
                p.sendMessage(Msg.TRAVEL_CHECKPOINT_EMPTY.get(p));
                return true;
            }

            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                p.sendMessage(Msg.TRAVEL_DELETE_CONFIRM_PROMPT.fmt(p, "id", id));
                p.sendMessage(Msg.TRAVEL_DELETE_CONFIRM_CMD.fmt(p, "id", id));
                return true;
            }

            cm.deleteCheckpoint(p, id);
            p.sendMessage(Msg.TRAVEL_DELETED.fmt(p, "id", id));
            return true;
        }

        // RENAME (name)
        if (sub.equalsIgnoreCase("name")) {

            if (!p.hasPermission("multifeature.checkpoint.rename")) {
                p.sendMessage(Msg.TRAVEL_NO_PERM_RENAME.get(p));
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 3) {
                p.sendMessage(Msg.TRAVEL_NAME_USAGE.fmt(p, "max", max));
                return true;
            }

            String id = args[1];

            if (!isValidId(id, max)) {
                p.sendMessage(Msg.TRAVEL_INVALID_ID_RENAME.fmt(p, "max", max));
                return true;
            }

            if (cm.loadCheckpoint(p, id) == null) {
                p.sendMessage(Msg.TRAVEL_CHECKPOINT_NOT_SET.get(p));
                return true;
            }

            String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length))
                    .replace("\"", "");

            cm.saveCheckpoint(p, id, name);
            p.sendMessage(Msg.TRAVEL_RENAMED.fmt(p, "id", id, "name", name));
            return true;
        }

        // SLOTS
        if (sub.equalsIgnoreCase("slots")) {

            int current = cm.getMaxSlots(p);

            if (args.length < 2) {
                p.sendMessage(Msg.TRAVEL_SLOTS_INFO.fmt(p, "current", current));
                p.sendMessage(Msg.TRAVEL_SLOTS_USAGE_HINT.get(p));
                return true;
            }

            int newMax;
            try {
                newMax = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                p.sendMessage(Msg.TRAVEL_SLOTS_INVALID.get(p));
                return true;
            }

            if (newMax < 1 || newMax > 54) {
                p.sendMessage(Msg.TRAVEL_SLOTS_RANGE.get(p));
                return true;
            }

            int highest = cm.getHighestUsedSlot(p);
            if (newMax < highest) {
                p.sendMessage(Msg.TRAVEL_SLOTS_CONFLICT.fmt(p, "new", newMax, "highest", highest));
                p.sendMessage(Msg.TRAVEL_SLOTS_CONFLICT_HINT.fmt(p, "highest", highest));
                return true;
            }

            cm.setMaxSlots(p, newMax);
            p.sendMessage(Msg.TRAVEL_SLOTS_SET.fmt(p, "new", newMax));
            return true;
        }

        // ICON
        if (sub.equalsIgnoreCase("icon")) {

            if (!p.hasPermission("multifeature.checkpoint.rename")) {
                p.sendMessage(Msg.TRAVEL_NO_PERM_ICON.get(p));
                return true;
            }

            int max = cm.getMaxSlots(p);

            if (args.length < 3) {
                p.sendMessage(Msg.TRAVEL_ICON_USAGE.fmt(p, "max", max));
                p.sendMessage(Msg.TRAVEL_ICON_EXAMPLE.get(p));
                return true;
            }

            String id = args[1];
            if (!isValidId(id, max)) {
                p.sendMessage(Msg.TRAVEL_INVALID_ID_ICON.fmt(p, "max", max));
                return true;
            }

            String matInput = args[2];

            if (matInput.equalsIgnoreCase("reset")) {
                cm.setIcon(p, id, null);
                p.sendMessage(Msg.TRAVEL_ICON_RESET.fmt(p, "id", id));
                return true;
            }

            Material mat = Material.matchMaterial(matInput);
            if (mat == null || !mat.isItem()) {
                p.sendMessage(Msg.TRAVEL_ICON_UNKNOWN.fmt(p, "mat", matInput));
                p.sendMessage(Msg.TRAVEL_ICON_UNKNOWN_HINT.get(p));
                return true;
            }

            cm.setIcon(p, id, mat);
            p.sendMessage(Msg.TRAVEL_ICON_SET.fmt(p, "id", id, "mat", mat.getKey()));
            return true;
        }

        // FALLBACK
        p.sendMessage(Msg.TRAVEL_FALLBACK.get(p));
        p.sendMessage(Msg.TRAVEL_FALLBACK_MENU.get(p));
        p.sendMessage(Msg.TRAVEL_FALLBACK_SAVE.get(p));
        p.sendMessage(Msg.TRAVEL_FALLBACK_LOAD.get(p));
        p.sendMessage(Msg.TRAVEL_FALLBACK_NAME.get(p));
        p.sendMessage(Msg.TRAVEL_FALLBACK_DELETE.get(p));
        p.sendMessage(Msg.TRAVEL_FALLBACK_SLOTS.get(p));
        p.sendMessage(Msg.TRAVEL_FALLBACK_ICON.get(p));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) return Collections.emptyList();
        if (!p.hasPermission("multifeature.travel")) return Collections.emptyList();

        CheckpointManager cm = plugin.getCheckpointManager();

        if (args.length == 1) {
            return List.of("save", "load", "name", "delete", "slots", "icon").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String sub2 = args[0].toLowerCase();
        int max = cm.getMaxSlots(p);

        if (args.length == 2 && (sub2.equals("save") || sub2.equals("load")
                || sub2.equals("name") || sub2.equals("delete") || sub2.equals("icon"))) {
            return checkpointList(max).stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && sub2.equals("delete")) {
            return List.of("confirm").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && sub2.equals("icon")) {
            return List.of("reset").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
