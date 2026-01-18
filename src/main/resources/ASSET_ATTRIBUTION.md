# Asset Attribution

This project uses open source assets from the following sources:

## Graphics & Sprites

### Kenney.nl (CC0 License - Public Domain)
- Website: https://kenney.nl/assets
- License: CC0 1.0 Universal (Public Domain)
- Assets: 2D sprites, tiles, UI elements

The following asset packs are recommended for use with this game:
- **Platformer Pack Redux**: Top-down characters and objects
- **Roguelike/RPG Pack**: Fantasy characters, items, and tiles
- **Pixel UI Pack**: User interface elements
- **Topdown Shooter**: Weapons, projectiles, and effects

While CC0 license does not require attribution, we gratefully acknowledge Kenney for providing these high-quality assets to the community.

## Audio (Future)
Audio assets will be sourced from CC0 or compatible open source libraries.

## How to Add Assets

1. Download asset packs from https://kenney.nl/assets
2. Extract PNG files to appropriate subdirectories under `src/main/resources/Textures/`
3. Reference textures in code using AssetManager:
   ```java
   Texture texture = assetManager.loadTexture("Textures/YourAsset.png");
   ```

## Current Status

The game currently uses procedurally generated colored shapes. To use actual sprites:
- Download desired Kenney.nl asset packs
- Extract to `src/main/resources/Textures/`
- Update entity classes to load textures instead of solid colors
