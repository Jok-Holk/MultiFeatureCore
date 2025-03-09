package com.jokholk.multifeature;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MenuCommand implements CommandExecutor {
    private MainPlugin plugin;

    public MenuCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            // Check permission
            if (player.hasPermission("multifeature.menu")) {
                player.openInventory(GUIManager.createMenu(player));
                return true;
            } else {
                player.sendMessage("§cYou do not have permission to use this menu!");
                return false;
            }
        }
        sender.sendMessage("Only players can use this command.");
        return false;
    }
}