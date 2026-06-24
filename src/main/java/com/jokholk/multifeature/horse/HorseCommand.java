package com.jokholk.multifeature.horse;
import com.jokholk.multifeature.*;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HorseCommand implements CommandExecutor, TabCompleter {

    private final MainPlugin plugin;

    public HorseCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!p.hasPermission("multifeature.horse")) {
            p.sendMessage("§cYou don't have permission to summon a horse.");
            return true;
        }

        // Sub-commands
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "confirm" -> { HorseManager.confirmSpawn(p, plugin.getServer()); return true; }
                case "cancel"  -> { HorseManager.cancelSpawn(p);                      return true; }
                case "dismiss" -> { HorseManager.dismiss(p, plugin.getServer());       return true; }
            }
        }

        // /horse <breed> [armor] [name]
        if (args.length == 0) {
            p.sendMessage("§cUsage: §f/horse <breed> [armor] [name]");
            p.sendMessage("§7Breeds: §f" + String.join("§7, §f", HorseManager.BREEDS.keySet()));
            p.sendMessage("§7Armor:  §f" + String.join("§7, §f", HorseManager.ARMORS.keySet()));
            p.sendMessage("§7Other:  §f/horse dismiss §7| §f/horse confirm §7| §f/horse cancel");
            return true;
        }

        String breedKey = args[0].toLowerCase();
        if (!HorseManager.BREEDS.containsKey(breedKey)) {
            p.sendMessage("§cUnknown breed: §f" + args[0]);
            p.sendMessage("§7Available: §f" + String.join("§7, §f", HorseManager.BREEDS.keySet()));
            return true;
        }

        String armorKey = null;
        int nameStart  = 1;

        if (args.length >= 2 && HorseManager.ARMORS.containsKey(args[1].toLowerCase())) {
            armorKey  = args[1].toLowerCase();
            nameStart = 2;
        }

        // Check if armor arg was provided but breed doesn't support armor
        if (armorKey != null && !HorseManager.BREEDS.get(breedKey).entityType().name().equals("HORSE")) {
            p.sendMessage("§7Note: §f" + breedKey + " §7cannot wear armor — armor argument ignored.");
            armorKey = null;
        }

        String customName = args.length > nameStart
                ? String.join(" ", Arrays.copyOfRange(args, nameStart, args.length))
                : null;

        boolean spawned = HorseManager.trySpawn(p, breedKey, armorKey, customName, plugin.getServer());
        // Confirmation message (if not spawned) is handled inside HorseManager.
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p) || !p.hasPermission("multifeature.horse"))
            return Collections.emptyList();

        if (args.length == 1) {
            List<String> options = new java.util.ArrayList<>(HorseManager.BREEDS.keySet());
            options.addAll(List.of("dismiss", "confirm", "cancel"));
            return options.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Arg 2: armor (only if arg 1 is a valid normal-horse breed or already a breed that supports armor)
        if (args.length == 2) {
            String breed = args[0].toLowerCase();
            if (HorseManager.BREEDS.containsKey(breed) &&
                    HorseManager.BREEDS.get(breed).entityType().name().equals("HORSE")) {
                return HorseManager.ARMORS.keySet().stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        // Arg 3+: name — no completion
        return Collections.emptyList();
    }
}
