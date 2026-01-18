# Textures Directory

Place your 2D sprite assets here.

## Recommended Structure

```
Textures/
├── characters/
│   ├── player.png
│   └── npcs/
├── enemies/
│   ├── goblin.png
│   ├── skeleton.png
│   └── orc.png
├── tiles/
│   ├── grass.png
│   ├── stone.png
│   └── water.png
├── items/
│   └── weapons/
└── ui/
    └── dialogue/
```

## Getting Assets

Download free CC0 assets from:
- https://kenney.nl/assets (Recommended - CC0 license)
- https://opengameart.org (Various licenses - check each)

## Usage in Code

```java
Texture playerTexture = assetManager.loadTexture("Textures/characters/player.png");
Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
mat.setTexture("ColorMap", playerTexture);
```
