package dev.qwerty.outerkill;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final OuterKill plugin;
    private FileConfiguration config;

    public ConfigManager(OuterKill plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public boolean isProtectionEnabled() {
        return config.getBoolean("protection-enabled", true);
    }

    public void setProtectionEnabled(boolean enabled) {
        config.set("protection-enabled", enabled);
        plugin.saveConfig();
    }

    public int getBufferDistance() {
        return config.getInt("buffer-distance", 10);
    }
}