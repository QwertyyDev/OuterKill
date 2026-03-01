# OuterKill

OuterKill creates lethal outer zones based on WorldEdit selections.  
After selecting an area, you can register it with a command. The plugin automatically assigns a numeric ID (1, 2, 3...) to each region.

If a player enters the configured outer buffer area of any registered region, they are instantly killed.

## Features
- Works with WorldEdit selections
- Automatic numeric region IDs
- Configurable outer buffer distance (default: 10 blocks)
- Kill system enabled by default
- Persistent region storage
- Lightweight and optimized

## Commands
/okill create  
Creates a region from the current WorldEdit selection.

/okill remove <id>  
Removes the specified region.

/okill list  
Lists all region IDs.

/okill toggle  
Enables or disables the kill system.

/okill reload  
Reloads configuration and saved regions.

## Configuration (config.yml)
distance: 10  
enabled: true  

distance → Number of blocks outside the selected region that become lethal  
enabled → Enables or disables the protection system (default: true)

## Requirements
- Minecraft 1.21+
- WorldEdit
