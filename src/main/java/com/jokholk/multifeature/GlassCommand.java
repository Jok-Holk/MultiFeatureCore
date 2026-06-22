package com.jokholk.multifeature;

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
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        if (!p.hasPermission("multifeature.glass")) {
            p.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        Block block = p.getLocation().getBlock();
        block.setType(Material.GLASS);

        p.sendMessage("§7Placed §fglass §7at §e"
                + block.getX() + " " + block.getY() + " " + block.getZ());
        return true;
    }
}
