package dev.qwerty.outerkill;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.SessionManager;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectionManager {

    private final OuterKill plugin;
    @Getter
    private final Map<UUID, RegionData> playerRegions;

    public ProtectionManager(OuterKill plugin) {
        this.plugin = plugin;
        this.playerRegions = new HashMap<>();
    }

    public void reload() {
        playerRegions.clear();
    }

    public void setRegionFromSelection(Player player) {
        try {
            SessionManager sessionManager = WorldEdit.getInstance().getSessionManager();
            LocalSession session = sessionManager.get(BukkitAdapter.adapt(player));
            Region region = session.getSelection(BukkitAdapter.adapt(player.getWorld()));

            if (region != null) {
                BlockVector3 min = region.getMinimumPoint();
                BlockVector3 max = region.getMaximumPoint();

                Location minLoc = new Location(player.getWorld(), min.getX(), min.getY(), min.getZ());
                Location maxLoc = new Location(player.getWorld(), max.getX(), max.getY(), max.getZ());

                playerRegions.put(player.getUniqueId(), new RegionData(minLoc, maxLoc, player.getWorld()));

                player.sendMessage("§aRegion boundaries set from WorldEdit selection");
                player.sendMessage("§7Min: §f" + min.getX() + ", " + min.getY() + ", " + min.getZ());
                player.sendMessage("§7Max: §f" + max.getX() + ", " + max.getY() + ", " + max.getZ());
            }
        } catch (IncompleteRegionException e) {
            player.sendMessage("§cNo WorldEdit selection found. Please select a region first.");
        }
    }

    public boolean isInDangerZone(Player player, Location location) {
        if (!plugin.getConfigManager().isProtectionEnabled()) {
            return false;
        }

        if (player.hasPermission("outerkill.bypass")) {
            return false;
        }

        RegionData regionData = playerRegions.get(player.getUniqueId());
        if (regionData == null) {
            return false;
        }

        if (!regionData.world.equals(location.getWorld())) {
            return false;
        }

        double distance = calculateDistanceToRegion(location, regionData);

        if (distance == 0) {
            return false;
        }

        int bufferDistance = plugin.getConfigManager().getBufferDistance();
        return distance > 0 && distance <= bufferDistance;
    }

    private double calculateDistanceToRegion(Location loc, RegionData region) {
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        double minX = region.min.getX();
        double minY = region.min.getY();
        double minZ = region.min.getZ();

        double maxX = region.max.getX();
        double maxY = region.max.getY();
        double maxZ = region.max.getZ();

        if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
            return 0;
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
    private static class RegionData {
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