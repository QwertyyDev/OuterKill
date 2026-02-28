package dev.qwerty.outerkill;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class OuterKill extends JavaPlugin {

    private ConfigManager configManager;
    private ProtectionManager protectionManager;
    private CommandHandler commandHandler;
    private MovementListener movementListener;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.protectionManager = new ProtectionManager(this);
        this.commandHandler = new CommandHandler(this);
        this.movementListener = new MovementListener(this);

        getCommand("okill").setExecutor(commandHandler);
        getServer().getPluginManager().registerEvents(movementListener, this);

        getLogger().info("OuterKill v1.0.0 has been enabled");
        getLogger().info("Protection status: " + (configManager.isProtectionEnabled() ? "ENABLED" : "DISABLED"));
    }

    @Override
    public void onDisable() {
        getLogger().info("OuterKill has been disabled");
    }

    public void reload() {
        configManager.reload();
        protectionManager.reload();
        getLogger().info("OuterKill configuration reloaded");
    }
}