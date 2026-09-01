package com.jokholk.multifeature.divine;
import com.jokholk.multifeature.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.GsonBuilder;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Map;
import java.util.zip.*;

/**
 * Debug-only: live-edit a resource pack item model's display transform
 * (rotation/translation/scale) and re-push the pack to the calling player
 * immediately, without needing to leave/rejoin.
 *
 * Usage:
 *   /modeltune <weapon> <context> rot <x> <y> <z>
 *   /modeltune <weapon> <context> pos <x> <y> <z>
 *   /modeltune <weapon> <context> scale <x> <y> <z>
 *   /modeltune <weapon> <context> show
 *   /modeltune push
 */
public class ModelTuneCommand implements CommandExecutor {

    private static final String ZIP_PATH = "multifeature-pack.zip";
    private static final String RESOURCE_PACK_URL = "http://127.0.0.1:8765/multifeature-pack.zip";

    private final MainPlugin plugin;

    public ModelTuneCommand(MainPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;

        String rank = plugin.getRankSystem().getRank(p);
        if (!rank.equals("OWNER") && !rank.equals("ADMIN") && !rank.equals("DEVELOPER")) {
            p.sendMessage("§cYou don't have permission to use this debug command.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("push")) {
            pushToPlayer(p);
            return true;
        }

        if (args.length < 3) {
            p.sendMessage("§cUsage: /modeltune <weapon> <context> <rot|pos|scale> <x> <y> <z>");
            p.sendMessage("§7       /modeltune <weapon> <context> show");
            p.sendMessage("§7       /modeltune push");
            return true;
        }

        String weapon = args[0];
        String context = args[1];
        String op = args[2].toLowerCase();

        try {
            byte[] zipBytes = Files.readAllBytes(Paths.get(ZIP_PATH));
            String entryPath = "assets/multifeature/models/item/" + weapon + ".json";

            JsonObject model = readZipJson(zipBytes, entryPath);
            if (model == null) {
                p.sendMessage("§cModel not found: " + entryPath);
                return true;
            }
            JsonObject display = model.getAsJsonObject("display");
            if (display == null) {
                p.sendMessage("§cThis model has no explicit \"display\" block to edit.");
                return true;
            }

            if (op.equals("show")) {
                JsonObject ctxObj = display.getAsJsonObject(context);
                if (ctxObj == null) {
                    p.sendMessage("§cNo context \"" + context + "\" on " + weapon);
                    return true;
                }
                p.sendMessage("§d[" + weapon + "/" + context + "] §7" + ctxObj.toString());
                return true;
            }

            if (args.length < 6) {
                p.sendMessage("§cUsage: /modeltune <weapon> <context> <rot|pos|scale> <x> <y> <z>");
                return true;
            }
            double x = Double.parseDouble(args[3]);
            double y = Double.parseDouble(args[4]);
            double z = Double.parseDouble(args[5]);

            String field = switch (op) {
                case "rot" -> "rotation";
                case "pos" -> "translation";
                case "scale" -> "scale";
                default -> null;
            };
            if (field == null) {
                p.sendMessage("§cField must be one of: rot, pos, scale");
                return true;
            }

            setVec3(display, context, field, x, y, z);

            // Auto-mirror the lefthand counterpart for handedness contexts
            if (context.endsWith("_righthand")) {
                String leftCtx = context.replace("_righthand", "_lefthand");
                if (field.equals("rotation")) {
                    setVec3(display, leftCtx, field, x, -y, -z);
                } else {
                    setVec3(display, leftCtx, field, x, y, z);
                }
            }

            byte[] newZip = writeZipJson(zipBytes, entryPath, model);
            Files.write(Paths.get(ZIP_PATH), newZip);

            p.sendMessage("§a[" + weapon + "/" + context + "] " + field + " -> [" + x + ", " + y + ", " + z + "]");
            pushToPlayer(p);

        } catch (NumberFormatException e) {
            p.sendMessage("§cInvalid number in x/y/z.");
        } catch (Exception e) {
            p.sendMessage("§cError: " + e.getMessage());
            plugin.getLogger().warning("ModelTuneCommand error: " + e);
        }

        return true;
    }

    private void setVec3(JsonObject display, String context, String field, double x, double y, double z) {
        JsonObject ctxObj = display.getAsJsonObject(context);
        if (ctxObj == null) {
            ctxObj = new JsonObject();
            display.add(context, ctxObj);
        }
        JsonArray arr = new JsonArray();
        arr.add(round(x));
        arr.add(round(y));
        arr.add(round(z));
        ctxObj.add(field, arr);
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private JsonObject readZipJson(byte[] zipBytes, String entryPath) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(entryPath)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    zin.transferTo(out);
                    return JsonParser.parseString(out.toString("UTF-8")).getAsJsonObject();
                }
            }
        }
        return null;
    }

    private byte[] writeZipJson(byte[] zipBytes, String entryPath, JsonObject newContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String pretty = new GsonBuilder().setPrettyPrinting().create().toJson(newContent);

        try (ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipBytes));
             ZipOutputStream zout = new ZipOutputStream(baos)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                zout.putNextEntry(new ZipEntry(entry.getName()));
                if (entry.getName().equals(entryPath)) {
                    zout.write(pretty.getBytes("UTF-8"));
                } else {
                    zin.transferTo(zout);
                }
                zout.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    private void pushToPlayer(Player p) {
        try {
            byte[] zipBytes = Files.readAllBytes(Paths.get(ZIP_PATH));
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(zipBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            String hashHex = sb.toString();

            p.setResourcePack(RESOURCE_PACK_URL, hashHex, true);
            p.sendMessage("§7Resource pack re-pushed (sha1 " + hashHex.substring(0, 8) + "...)");
        } catch (Exception e) {
            p.sendMessage("§cFailed to push resource pack: " + e.getMessage());
        }
    }
}
