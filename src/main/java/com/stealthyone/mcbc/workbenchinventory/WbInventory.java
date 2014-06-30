package com.stealthyone.mcbc.workbenchinventory;

import com.stealthyone.mcb.stbukkitlib.autosaving.Autosavable;
import com.stealthyone.mcb.stbukkitlib.autosaving.Autosaver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class WbInventory extends JavaPlugin implements Autosavable {

    private InventorySaver inventorySaver;

    @Override
    public void onLoad() {
        getDataFolder().mkdir();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(inventorySaver = new InventorySaver(this), this);
        inventorySaver.load();
        if (!Autosaver.scheduleForMe(this, this, getConfig().getInt("Autosave interval", 0))) {
            getLogger().warning("Autosaving disabled.  It is recommended that you enable it to prevent data loss!");
        }
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