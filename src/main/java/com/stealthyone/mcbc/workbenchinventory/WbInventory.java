package com.stealthyone.mcbc.workbenchinventory;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class WbInventory extends JavaPlugin {

    private InventorySaver inventorySaver;

    @Override
    public void onLoad() {
        getDataFolder().mkdir();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(inventorySaver = new InventorySaver(this), this);
        inventorySaver.load();
        getLogger().info("WorkbenchInventory v" + getDescription().getVersion() + " by Stealth2800 ENABLED.");
    }

    @Override
    public void onDisable() {
        saveAll();
        getLogger().info("WorkbenchInventory v" + getDescription().getVersion() + " by Stealth2800 DISABLED.");
    }

    public void saveAll() {
        inventorySaver.save();
    }

}