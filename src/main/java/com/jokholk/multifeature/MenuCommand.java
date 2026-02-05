package com.jokholk.multifeature;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class MenuCommand implements CommandExecutor {

    private final MainPlugin plugin;

    public MenuCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
                             String label, String[] args) {

        if (!(sender instanceof Player p)) {
            return true;
        }

        CheckpointManager cm = plugin.getCheckpointManager();

        // /menu
        if (args.length == 0) {
            p.openInventory(GUIManager.createMenu(p, cm));
            return true;
        }

        String sub = args[0];

        // SAVE --------------------------------------------------
        if (sub.equalsIgnoreCase("save")) {

            // 1. Kiểm tra có nhập tên không
            if (args.length < 2) {
                p.sendMessage("§cUsage: /menu save <checkpoint1-9 | name>");
                return true;
            }

            String input = args[1];

            // 2. Xác định ID hợp lệ
            String id;

            // Nếu nhập checkpoint1 -> checkpoint9
            if (input.matches("checkpoint[1-9]")) {
                id = input;
            }
            else {
                // Tìm theo tên đã tồn tại
                String found = cm.findIdByName(p, input);

                if (found != null) {
                    id = found;
                } else {
                    p.sendMessage("§cInvalid checkpoint ID! Use checkpoint1 → checkpoint9");
                    return true;
                }
            }

            // 3. Xử lý tên hiển thị
            String name;

            if (args.length >= 3) {
                name = String.join(" ",
                                Arrays.copyOfRange(args, 2, args.length))
                        .replace("\"", "");
            } else {
                name = id;
            }

            // 4. SAVE AN TOÀN
            cm.saveCheckpoint(p, id, name);

            // 5. THÔNG BÁO
            p.sendMessage("§aSaved checkpoint §e" + name);
            return true;
        }

// LOAD --------------------------------------------------
        if (sub.equalsIgnoreCase("load")) {

            if (args.length < 2) {
                p.sendMessage("§cUsage: /menu load <name or checkpoint>");
                return true;
            }

            String input = args[1];

            String id = cm.findIdByName(p, input);

            // ➤ CHECK QUAN TRỌNG
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
        }// DELETE ------------------------------------------------
        if (sub.equalsIgnoreCase("delete")) {

            if (args.length < 2) {
                p.sendMessage("§cUsage: /menu delete <checkpoint1-9>");
                return true;
            }

            String id = args[1];

            // ➤ CHỈ CHO PHÉP ID GỐC
            if (!id.matches("checkpoint[1-9]")) {
                p.sendMessage("§cYou must use checkpoint ID (checkpoint1-9)!");
                return true;
            }

            Location l = cm.loadCheckpoint(p, id);

            if (l == null) {
                p.sendMessage("§cThis checkpoint is already empty!");
                return true;
            }

            // ➤ CHƯA CÓ CONFIRM
            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {

                p.sendMessage("§eAre you sure you want to delete §c" + id + "§e?");
                p.sendMessage("§7Type: §f/menu delete " + id + " confirm");
                return true;
            }

            // ➤ XÓA THẬT
            cm.deleteCheckpoint(p, id);

            p.sendMessage("§aCheckpoint §e" + id + " §ahas been permanently deleted.");
            return true;
        }
// RENAME ------------------------------------------------
        if (sub.equalsIgnoreCase("name")) {

            if (args.length < 3) {
                p.sendMessage("§cUsage: /menu name <checkpoint1-9> <new name>");
                return true;
            }

            String id = args[1];

            // ➤ CHỈ CHO PHÉP ID GỐC
            if (!id.matches("checkpoint[1-9]")) {
                p.sendMessage("§cYou can only rename using checkpoint ID (checkpoint1-9)!");
                return true;
            }

            Location l = cm.loadCheckpoint(p, id);

            if (l == null) {
                p.sendMessage("§cThis checkpoint has not been set yet!");
                return true;
            }

            String name = String.join(" ",
                            Arrays.copyOfRange(args, 2, args.length))
                    .replace("\"", "");

            cm.saveCheckpoint(p, id, name);

            p.sendMessage("§aCheckpoint §e" + id + " §arenamed to §e" + name);
            return true;
        }
        // ===== FALLBACK AN TOÀN =====
        p.sendMessage("§cUnknown menu command.");
        p.sendMessage("§7/menu");
        p.sendMessage("§7/menu save <checkpoint1-9> <name>");
        p.sendMessage("§7/menu load <name or id>");
        p.sendMessage("§7/menu name <checkpoint1-9> <new name>");
        p.sendMessage("§7/menu delete <checkpoint1-9>");
        return true;
    }
}
