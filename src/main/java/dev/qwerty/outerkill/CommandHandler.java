package dev.qwerty.outerkill;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            case "toggle":
                handleToggle(sender);
                break;

            case "reload":
                handleReload(sender);
                break;

            case "set":
                handleSet(sender);
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleToggle(CommandSender sender) {
        boolean currentState = plugin.getConfigManager().isProtectionEnabled();
        boolean newState = !currentState;

        plugin.getConfigManager().setProtectionEnabled(newState);

        String status = newState ? "§aENABLED" : "§cDISABLED";
        sender.sendMessage("§7Protection system is now " + status);
    }

    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage("§aOuterKill configuration has been reloaded");
    }

    private void handleSet(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return;
        }

        plugin.getProtectionManager().setRegionFromSelection(player);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§lOuterKill Commands:");
        sender.sendMessage("§e/okill toggle §7- Toggle protection system");
        sender.sendMessage("§e/okill reload §7- Reload configuration");
        sender.sendMessage("§e/okill set §7- Set region from WorldEdit selection");
    }
}