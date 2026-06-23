package com.jokholk.multifeature;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import java.util.UUID;

public class HorseListener implements Listener {

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player rider)) return;

        UUID ownerUUID = HorseManager.getOwner(e.getVehicle().getUniqueId());
        if (ownerUUID == null) return; // không phải ngựa được track

        if (!rider.getUniqueId().equals(ownerUUID)) {
            e.setCancelled(true);
            rider.sendMessage("§cThis is not your horse.");
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        HorseManager.removeHorse(e.getEntity().getUniqueId());
    }
}
