package dev.qwerty.outerkill;

import com.sk89q.worldedit.IncompleteRegionException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class CommandHandler implements CommandExecutor {

    private final OuterKill plugin;

    public CommandHandler(OuterKill plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("outerkill.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "create":
                handleCreate(sender);
                break;

            case "remove":
                handleRemove(sender, args);
                break;

            case "list":
                handleList(sender);
                break;

            case "toggle":
                handleToggle(sender);
                break;

            case "reload":
                handleReload(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleCreate(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return;
        }

        try {
            int id = plugin.getProtectionManager().createRegionFromSelection(player);
            sender.sendMessage("§aRegion created with ID: §e" + id);
        } catch (IncompleteRegionException e) {
            sender.sendMessage("§cYou must make a WorldEdit selection first!");
        } catch (Exception e) {
            sender.sendMessage("§cFailed to create region. Check console for details.");
            plugin.getLogger().severe("Error creating region: " + e.getMessage());
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUsage: /okill remove <id>");
            return;
        }

        try {
            int id = Integer.parseInt(args[1]);
            if (plugin.getProtectionManager().removeRegion(id)) {
                sender.sendMessage("§aRegion §e" + id + "§a has been removed.");
            } else {
                sender.sendMessage("§cRegion with ID §e" + id + "§c does not exist.");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid region ID. Must be a number.");
        }
    }

    private void handleList(CommandSender sender) {
        Map<Integer, ProtectionManager.RegionData> regions = plugin.getProtectionManager().getRegions();

        if (regions.isEmpty()) {
            sender.sendMessage("§eNo regions have been created yet.");
            return;
        }

        sender.sendMessage("§6§lRegistered Regions:");
        for (Map.Entry<Integer, ProtectionManager.RegionData> entry : regions.entrySet()) {
            int id = entry.getKey();
            ProtectionManager.RegionData region = entry.getValue();
            sender.sendMessage("§e" + id + " §7- World: §f" + region.getWorld().getName() +
                    " §7| Min: §f" + formatLocation(region.getMin()) +
                    " §7| Max: §f" + formatLocation(region.getMax()));
        }
    }

    private String formatLocation(org.bukkit.Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private void handleToggle(CommandSender sender) {
        boolean currentState = plugin.getConfigManager().isProtectionEnabled();
        boolean newState = !currentState;

        plugin.getConfigManager().setProtectionEnabled(newState);

        sender.sendMessage(newState ? "ENABLED" : "DISABLED");
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage("§aOuterKill configuration has been reloaded");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lOuterKill Commands:");
        sender.sendMessage("§e/okill create §7- Create region from WorldEdit selection");
        sender.sendMessage("§e/okill remove <id> §7- Remove region by ID");
        sender.sendMessage("§e/okill list §7- List all regions");
        sender.sendMessage("§e/okill toggle §7- Toggle protection system");
        sender.sendMessage("§e/okill reload §7- Reload configuration and regions");
    }
}