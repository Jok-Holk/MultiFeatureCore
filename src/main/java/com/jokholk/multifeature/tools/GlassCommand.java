package com.jokholk.multifeature.tools;
import com.jokholk.multifeature.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GlassCommand implements CommandExecutor {

    public GlassCommand(MainPlugin plugin) {}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage(Msg.ONLY_PLAYERS.get());
            return true;
        }

        if (!p.hasPermission("multifeature.glass")) {
            p.sendMessage(Msg.GLASS_NO_PERM.get(p));
            return true;
        }

        Block block = p.getLocation().subtract(0, 1, 0).getBlock();
        block.setType(Material.GLASS);

        p.sendMessage(Msg.GLASS_PLACED.fmt(p,
                "x", block.getX(), "y", block.getY(), "z", block.getZ()));
        return true;
    }
}
