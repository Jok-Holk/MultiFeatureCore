package com.jokholk.multifeature;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class LanguageCommand implements CommandExecutor, TabCompleter {

    private final LanguageManager languageManager;

    public LanguageCommand(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Msg.ONLY_PLAYERS.get());
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(Msg.LANG_USAGE.get(p));
            return true;
        }

        Language current = LanguageManager.getLang(p);
        Language target  = Language.fromString(args[0]);

        if (target == Language.VIETNAMESE) {
            if (current == Language.VIETNAMESE) {
                p.sendMessage(Msg.LANG_ALREADY_VIETNAMESE.get(p));
                return true;
            }
            languageManager.set(p, Language.VIETNAMESE);
            // Gửi xác nhận bằng ngôn ngữ MỚI (Vietnamese)
            p.sendMessage(Msg.LANG_SET_VIETNAMESE.get(Language.VIETNAMESE));
        } else {
            // Default to ENGLISH for unknown input
            if (current == Language.ENGLISH) {
                p.sendMessage(Msg.LANG_ALREADY_ENGLISH.get(p));
                return true;
            }
            languageManager.set(p, Language.ENGLISH);
            // Gửi xác nhận bằng ngôn ngữ MỚI (English)
            p.sendMessage(Msg.LANG_SET_ENGLISH.get(Language.ENGLISH));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            return List.of("english", "vietnamese").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
