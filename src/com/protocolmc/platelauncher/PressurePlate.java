package com.protocolmc.platelauncher;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class PressurePlate {
    private static PlateLauncher plugin;
    Location loc;
    double yPower;
    double xPower;

    PressurePlate(double yPow, double xPow) {
        this.yPower = yPow;
        this.xPower = xPow;
        plugin = PlateLauncher.plugin;
    }

    void launch(Entity player) {
        launch(this.xPower, this.yPower, player);
    }

    private static void launch(Double xPow, Double yPow, Entity player) {
        if (plugin.ncp && player instanceof Player) {
            plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), "ncp exempt " + ((Player)player).getName() + " moving_survivalfly");
        }

        plugin.entities.put(player.getEntityId(), new LaunchedEntity());
        ((LaunchedEntity)plugin.entities.get(player.getEntityId())).fallTicks = 10;
        if (player.getType() == EntityType.PLAYER) {
            ((LaunchedEntity)plugin.entities.get(player.getEntityId())).isPlayer = true;
        }

        float pitch = player.getLocation().getPitch();
        Location pitchLoc = player.getLocation();
        pitchLoc.setPitch(0.0F);
        player.setVelocity(pitchLoc.getDirection().multiply(xPow));
        player.setVelocity(player.getVelocity().setY(yPow));
        if (player.isInsideVehicle()) {
            player.getVehicle().setVelocity(pitchLoc.getDirection().multiply(xPow));
            player.getVehicle().setVelocity(player.getVelocity().setY(yPow));
        }

        player.getLocation().setPitch(pitch);
    }

    void save(ArrayList<String> plateList) {
        plateList.add(this.loc.getWorld().getName() + "|" + this.loc.getBlockX() + "|" + this.loc.getBlockY() + "|" + this.loc.getBlockZ() + "|" + this.xPower + "|" + this.yPower);
    }

    void load(String plate) {
        String[] split = plate.split("\\|");
        this.loc = new Location(plugin.getServer().getWorld(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]), Double.valueOf(split[3]));
        this.xPower = Double.valueOf(split[4]);
        this.yPower = Double.valueOf(split[5]);
    }
}
