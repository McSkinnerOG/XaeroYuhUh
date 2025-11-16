# XaeroNuhUh

XaeroNuhUh is a simple server-side utility mod designed to **disable Xaero’s World Map** for all players, regardless of their client settings.  
Perfect for servers that want fair exploration, no free world info, and a more immersive gameplay experience.

## ✨ Features
- Fully **server-enforced** disabling of:
  - **Xaero’s Minimap**  
  - **Xaero’s World Map Cave mode**
- Works automatically when players join.
- No client installation required.
- Lightweight, plug-and-play.

## 🛠️ Added Gamerules
XaeroNuhUh introduces two gamerules you can configure:

| Gamerule | Type | Description |
|----------|-------|-------------|
| `DisableMiniMap` | boolean | When `true`, disables Xaero’s Minimap for all players. |
| `EnableFairMode` | boolean | When `true`, enforces fair-mode restrictions for both Xaero’s Minimap and World Map. |

### Example Commands
```mcfunction
/gamerule DisableMiniMap true
/gamerule EnableFairMode true
```

## 📦 Supported Mods

XaeroNuhUh targets the following mods:

-   Xaero's Minimap: [https://modrinth.com/mod/xaeros-minimap](https://modrinth.com/mod/xaeros-minimap)
    
-   Xaero's World Map: [https://modrinth.com/mod/xaeros-world-map](https://modrinth.com/mod/xaeros-world-map)
    

## 📀 Installation

1.  Drop **XaeroNuhUh** into your server's `mods/` folder.
    
2.  Restart the server.
    
3.  (Optional) Adjust the gamerules as needed.
    

## 📌 Notes

-   This mod is **server-side only**—players do not need to install it.
    
-   Works out of the box; no config file required.
    

Enjoy a fairer adventure!
