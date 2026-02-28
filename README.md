# OuterKill

OuterKill is a lightweight Minecraft 1.21+ plugin that creates a lethal external boundary around a WorldEdit selected region.  
Players who enter the configured outer zone will be instantly killed.

## Features
- Works with WorldEdit selections
- Protection applies only outside the selected region
- Configurable outer buffer distance (default: 10 blocks)
- Kill system enabled by default
- Toggle system with command
- Reload configuration without restarting the server
- Optimized movement checks
- Auto-generated config file

## Commands
/okill toggle  
Enables or disables the kill protection system.

/okill reload  
Reloads the configuration file.

## Configuration (config.yml)
distance: 10
enabled: true

distance → Number of blocks outside the selected region that become lethal  
enabled → Enables or disables the protection system (default: true)

## Requirements
- Minecraft 1.21+
- WorldEdit
