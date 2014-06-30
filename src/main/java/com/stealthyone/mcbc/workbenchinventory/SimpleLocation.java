package com.stealthyone.mcbc.workbenchinventory;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SimpleLocation {

    private String worldName;
    private int x;
    private int y;
    private int z;

    public SimpleLocation(Location location) {
        worldName = location.getWorld().getName();
        x = location.getBlockX();
        y = location.getBlockY();
        z = location.getBlockZ();
    }

    public SimpleLocation(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return worldName + "." + x + "." + z + "." + y;
    }

    public Location getBukkitLocation() {
        try {
            return Bukkit.getWorld(worldName).getBlockAt(x, y, z).getLocation();
        } catch (Exception ex) {
            return null;
        }
    }

}