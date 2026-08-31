package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DivineDebugCommand implements CommandExecutor {

    private static final String[] WEAPON_COMMANDS = {
        "godmace", "trident", "excalibur", "ragnarok",
        "ignis", "grave", "verdant", "void", "spear", "nothan"
    };

    private final MainPlugin plugin;

    public DivineDebugCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender,
                             Command cmd,
                             String label,
                             String[] args) {

        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);

        if (!rank.equals("OWNER") &&
                !rank.equals("ADMIN") &&
                !rank.equals("DEVELOPER")) {

            p.sendMessage(Msg.DIVINEDEBUG_NO_PERM.get(p));
            return true;
        }

        for (String weaponCmd : WEAPON_COMMANDS) {
            Bukkit.dispatchCommand(p, weaponCmd);
        }

        p.sendMessage(Msg.DIVINEDEBUG_GIVEN.get(p));

        return true;
    }
}
