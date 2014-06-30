package com.stealthyone.mcbc.workbenchinventory;

import com.stealthyone.mcb.stbukkitlib.storage.YamlFileManager;
import com.stealthyone.mcb.stbukkitlib.utils.InventoryUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class InventorySaver implements Listener {

    private WbInventory plugin;

    private Map<UUID, Location> playerWorkbenches = new HashMap<>();
    private Set<Location> usedWorkbenches = new HashSet<>();

    private YamlFileManager storageFile;
    private Map<Location, ItemStack[]> savedInventories = new HashMap<>();

    public InventorySaver(WbInventory plugin) {
        this.plugin = plugin;
    }

    public void load() {
        storageFile = new YamlFileManager(plugin.getDataFolder() + File.separator + "workbenches.yml");

        FileConfiguration config = storageFile.getConfig();

        for (String worldName : config.getKeys(false)) {
            for (String rawX : config.getConfigurationSection(worldName).getKeys(false)) {
                int x;
                try {
                    x = Integer.parseInt(rawX);
                } catch (Exception ex) {
                    continue;
                }

                for (String rawZ : config.getConfigurationSection(worldName + "." + rawX).getKeys(false)) {
                    int z;
                    try {
                        z = Integer.parseInt(rawZ);
                    } catch (Exception ex) {
                        continue;
                    }

                    for (String rawY : config.getConfigurationSection(worldName + "." + rawX + "." + rawZ).getKeys(false)) {
                        int y;
                        try {
                            y = Integer.parseInt(rawY);
                        } catch (Exception ex) {
                            continue;
                        }

                        SimpleLocation simpleLocation = new SimpleLocation(worldName, x, y, z);
                        Location location = simpleLocation.getBukkitLocation();
                        String rawPath = simpleLocation.toString();
                        List<ItemStack> items = InventoryUtils.getItemstackList(config.getList(rawPath));
                        if (items.size() != 10) {
                            plugin.getLogger().warning("Invalid item list in workbenches.yml for location: " + rawPath);
                            continue;
                        }

                        try {
                            savedInventories.put(location, items.toArray(new ItemStack[items.size()]));
                            plugin.getLogger().info("Loaded inventory...");
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Invalid item list in workbenches.yml for location: " + rawPath);
                        }
                    }
                }
            }
        }
    }

    public void save() {
        FileConfiguration config = storageFile.getConfig();
        for (Entry<Location, ItemStack[]> inventory : savedInventories.entrySet()) {
            config.set(new SimpleLocation(inventory.getKey()).toString(), inventory.getValue());
        }
        storageFile.saveFile();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.WORKBENCH) {
            ItemStack[] items = savedInventories.remove(e.getBlock().getLocation());
            if (items != null) {
                e.setCancelled(true);

                Block b = e.getBlock();
                List<ItemStack> drops = new ArrayList<>(b.getDrops());
                drops.addAll(Arrays.asList(items).subList(1, 10));
                b.setType(Material.AIR);

                Location l = b.getLocation();
                World w = l.getWorld();
                for (ItemStack item : drops) {
                    if (item.getType() == Material.AIR) continue;
                    w.dropItemNaturally(l, item);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!e.isCancelled() && e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.WORKBENCH) {
            if (usedWorkbenches.contains(e.getClickedBlock().getLocation())) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "This workbench is currently in use.");
            } else {
                playerWorkbenches.put(e.getPlayer().getUniqueId(), e.getClickedBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.isCancelled() || !(e.getPlayer() instanceof Player)) return;
        if (e.getView().getTopInventory().getType() != InventoryType.WORKBENCH) return;

        Player player = (Player) e.getPlayer();
        Location loc = playerWorkbenches.get(player.getUniqueId());
        if (loc != null) {
            usedWorkbenches.add(loc);

            ItemStack[] items = savedInventories.get(loc);
            if (items != null && containsSomething(items)) {
                e.getInventory().setContents(items);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() != InventoryType.WORKBENCH || !(e.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getPlayer();
        Location loc = playerWorkbenches.get(player.getUniqueId());
        if (loc == null) return;
        usedWorkbenches.remove(loc);
        playerWorkbenches.remove(player.getUniqueId());

        if (containsSomething(inventory.getContents()) || savedInventories.containsKey(loc)) {
            saveInventory(loc, inventory);
            e.getInventory().clear();
        }
    }

    private boolean containsSomething(ItemStack[] items) {
        for (ItemStack item : items) {
            if (item.getType() != Material.AIR) {
                return true;
            }
        }
        return false;
    }

    private void saveInventory(Location location, Inventory inventory) {
        savedInventories.put(location, inventory.getContents());
    }

}