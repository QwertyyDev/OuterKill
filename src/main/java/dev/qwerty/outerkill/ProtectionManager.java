package dev.qwerty.outerkill;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProtectionManager {

    private final OuterKill plugin;
    @Getter
    private final Map<Integer, RegionData> regions;
    private File regionsFile;
    private FileConfiguration regionsConfig;
    private int nextId;

    public ProtectionManager(OuterKill plugin) {
        this.plugin = plugin;
        this.regions = new HashMap<>();
        this.nextId = 1;
        setupRegionsFile();
        loadRegions();
    }

    private void setupRegionsFile() {
        regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        if (!regionsFile.exists()) {
            try {
                regionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create regions.yml file");
            }
        }
        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
    }

    public void reload() {
        regions.clear();
        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
        loadRegions();
    }

    private void loadRegions() {
        ConfigurationSection regionsSection = regionsConfig.getConfigurationSection("regions");
        if (regionsSection == null) {
            return;
        }

        int maxId = 0;
        for (String key : regionsSection.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                ConfigurationSection regionSection = regionsSection.getConfigurationSection(key);

                String worldName = regionSection.getString("world");
                World world = plugin.getServer().getWorld(worldName);

                if (world == null) {
                    plugin.getLogger().warning("World " + worldName + " not found for region " + id);
                    continue;
                }

                int minX = regionSection.getInt("min.x");
                int minY = regionSection.getInt("min.y");
                int minZ = regionSection.getInt("min.z");
                int maxX = regionSection.getInt("max.x");
                int maxY = regionSection.getInt("max.y");
                int maxZ = regionSection.getInt("max.z");

                Location min = new Location(world, minX, minY, minZ);
                Location max = new Location(world, maxX, maxY, maxZ);

                regions.put(id, new RegionData(min, max, world));

                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid region ID: " + key);
            }
        }

        nextId = maxId + 1;
        plugin.getLogger().info("Loaded " + regions.size() + " regions. Next ID: " + nextId);
    }

    private void saveRegions() {
        regionsConfig.set("regions", null);

        for (Map.Entry<Integer, RegionData> entry : regions.entrySet()) {
            int id = entry.getKey();
            RegionData region = entry.getValue();

            String path = "regions." + id;
            regionsConfig.set(path + ".world", region.world.getName());
            regionsConfig.set(path + ".min.x", region.min.getBlockX());
            regionsConfig.set(path + ".min.y", region.min.getBlockY());
            regionsConfig.set(path + ".min.z", region.min.getBlockZ());
            regionsConfig.set(path + ".max.x", region.max.getBlockX());
            regionsConfig.set(path + ".max.y", region.max.getBlockY());
            regionsConfig.set(path + ".max.z", region.max.getBlockZ());
        }

        try {
            regionsConfig.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save regions.yml");
        }
    }

    public int createRegionFromSelection(Player player) throws IncompleteRegionException {
        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
        Region selection = com.sk89q.worldedit.WorldEdit.getInstance()
                .getSessionManager()
                .get(wePlayer)
                .getSelection(wePlayer.getWorld());

        if (selection == null) {
            throw new IncompleteRegionException();
        }

        BlockVector3 min = selection.getMinimumPoint();
        BlockVector3 max = selection.getMaximumPoint();
        World world = BukkitAdapter.adapt(selection.getWorld());

        Location minLoc = new Location(world, min.getX(), min.getY(), min.getZ());
        Location maxLoc = new Location(world, max.getX(), max.getY(), max.getZ());

        int id = nextId++;
        regions.put(id, new RegionData(minLoc, maxLoc, world));
        saveRegions();

        return id;
    }

    public boolean removeRegion(int id) {
        if (regions.remove(id) != null) {
            saveRegions();
            return true;
        }
        return false;
    }

    public boolean isInDangerZone(Player player, Location location) {
        if (!plugin.getConfigManager().isProtectionEnabled()) {
            return false;
        }

        if (player.hasPermission("outerkill.bypass")) {
            return false;
        }

        int bufferDistance = plugin.getConfigManager().getBufferDistance();

        for (RegionData region : regions.values()) {
            if (!region.world.equals(location.getWorld())) {
                continue;
            }

            double distance = calculateDistanceToRegion(location, region);

            if (distance > 0 && distance <= bufferDistance) {
                return true;
            }
        }

        return false;
    }

    private double calculateDistanceToRegion(Location loc, RegionData region) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        double minX = Math.min(region.min.getX(), region.max.getX());
        double maxX = Math.max(region.min.getX(), region.max.getX());
        double minY = Math.min(region.min.getY(), region.max.getY());
        double maxY = Math.max(region.min.getY(), region.max.getY());
        double minZ = Math.min(region.min.getZ(), region.max.getZ());
        double maxZ = Math.max(region.min.getZ(), region.max.getZ());

        boolean insideX = x >= minX && x <= maxX;
        boolean insideY = y >= minY && y <= maxY;
        boolean insideZ = z >= minZ && z <= maxZ;

        if (insideX && insideY && insideZ) {
            return -1;
        }

        double dx = 0;
        double dy = 0;
        double dz = 0;

        if (x < minX) {
            dx = minX - x;
        } else if (x > maxX) {
            dx = x - maxX;
        }

        if (y < minY) {
            dy = minY - y;
        } else if (y > maxY) {
            dy = y - maxY;
        }

        if (z < minZ) {
            dz = minZ - z;
        } else if (z > maxZ) {
            dz = z - maxZ;
        }

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Getter
    public static class RegionData {
        private final Location min;
        private final Location max;
        private final World world;

        public RegionData(Location min, Location max, World world) {
            this.min = min;
            this.max = max;
            this.world = world;
        }
    }
}