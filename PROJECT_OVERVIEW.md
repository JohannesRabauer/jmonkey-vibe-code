# JMonkey Vibe Game - Project Overview

## 🎮 What is this?

A **completely free, open-source 2D top-down RPG** built with:
- **Java 17** & **jMonkeyEngine 3.6** for game engine
- **langchain4j** for AI-powered NPC conversations (OpenAI or local Ollama)
- **Maven** for build management
- **GitHub Actions** for automated cross-platform builds

## 📁 Complete Project Structure

```
jmonkey-vibe-code/
│
├── .github/
│   └── workflows/
│       └── build.yml                      # CI/CD for Windows/Linux/macOS builds
│
├── src/
│   ├── main/
│   │   ├── java/com/jmonkeyvibe/game/
│   │   │   ├── Main.java                  # Entry point, camera setup
│   │   │   │
│   │   │   ├── ai/                        # AI Dialogue System
│   │   │   │   ├── AIModelFactory.java    # OpenAI/Ollama provider factory
│   │   │   │   └── NPCConversationManager.java  # Manages NPC dialogues
│   │   │   │
│   │   │   ├── combat/                    # Combat System
│   │   │   │   └── CombatManager.java     # Handles projectiles, enemies, collisions
│   │   │   │
│   │   │   ├── entities/                  # Game Entities
│   │   │   │   ├── Player.java            # Player character
│   │   │   │   ├── NPC.java               # Non-player characters
│   │   │   │   ├── Enemy.java             # Enemy types (Goblin, Skeleton, etc.)
│   │   │   │   └── Projectile.java        # Bullets/projectiles
│   │   │   │
│   │   │   ├── quest/                     # Quest System
│   │   │   │   ├── Quest.java             # Quest data structure
│   │   │   │   └── QuestGenerator.java    # Procedural quest generation
│   │   │   │
│   │   │   ├── states/                    # Game States (AppStates)
│   │   │   │   ├── ExplorationState.java  # Top-down exploration mode
│   │   │   │   └── DungeonCombatState.java # Twin-stick combat mode
│   │   │   │
│   │   │   └── world/                     # Procedural Generation
│   │   │       ├── WorldGenerator.java    # Overworld map generation
│   │   │       └── DungeonGenerator.java  # Dungeon room/corridor generation
│   │   │
│   │   └── resources/
│   │       ├── Textures/                  # 2D sprites (add your own!)
│   │       │   └── README.md              # Asset instructions
│   │       └── ASSET_ATTRIBUTION.md       # Asset credits
│
├── pom.xml                                # Maven configuration
├── LICENSE                                # MIT License
├── README.md                              # Main documentation
├── SETUP.md                               # Development setup guide
├── .gitignore                             # Git ignore rules
├── run.bat                                # Windows quick start
└── run.sh                                 # Linux/macOS quick start
```

## 🎯 Core Features Implemented

### ✅ 1. Game Architecture
- **Main Application**: `Main.java` extends `SimpleApplication`
- **Orthographic Camera**: 2D top-down view on XZ plane
- **State Management**: Separate states for exploration and combat
- **Input Handling**: WASD movement, mouse controls, E for interaction

### ✅ 2. AI Dialogue System
- **Dual Provider Support**: OpenAI API or local Ollama
- **Environment-Based Config**: `AI_PROVIDER` environment variable
- **Conversation History**: Tracks dialogue context per NPC
- **Fallback Mode**: Works without AI with basic responses
- **NPC Personalities**: Each NPC has unique personality traits

### ✅ 3. Procedural Generation

#### Overworld (`WorldGenerator.java`)
- Tile-based random terrain
- Mix of grass, dirt, stone, water tiles
- 20x20 grid (configurable)
- Simple noise-based generation

#### Dungeons (`DungeonGenerator.java`)
- Room-based layout (4-10 tiles per room)
- Corridor connections between rooms
- Up to 15 rooms per dungeon
- Wall/floor distinction

#### Quests (`QuestGenerator.java`)
- 5 quest types: FETCH, KILL, ESCORT, EXPLORE, TALK
- Random objectives and rewards
- Quest progress tracking
- Procedural descriptions

### ✅ 4. Combat System

#### Twin-Stick Controls
- **WASD**: Player movement
- **Mouse**: Aim direction
- **Left Click**: Fire projectiles
- **ESC**: Exit dungeon

#### Combat Mechanics
- Projectile system with collision detection
- Enemy AI that chases player
- Health/damage system
- 4 enemy types with different stats:
  - Goblin (weak, fast)
  - Skeleton (balanced)
  - Orc (strong, slow)
  - Demon (boss-level)

### ✅ 5. Build & Distribution

#### Maven Build
- Clean compilation with Java 17
- Fat JAR generation (standalone executable)
- jpackage-ready structure
- All dependencies included

#### GitHub Actions CI/CD
- **Matrix Builds**: Windows, Linux, macOS simultaneously
- **Artifacts**: JAR, EXE, DEB, DMG files
- **Auto-Release**: Publishes to GitHub releases on tags
- **Parallel Execution**: Fast build times

#### Cross-Platform Installers
- **Windows**: .exe installer with JRE bundled
- **Linux**: .deb package
- **macOS**: .dmg disk image
- **All Platforms**: Standalone JAR

## 🔧 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 17+ |
| Game Engine | jMonkeyEngine | 3.6.1-stable |
| Graphics Backend | LWJGL3 | 3.3.2+ |
| AI Framework | langchain4j | 1.0.0-alpha1 |
| AI Providers | OpenAI / Ollama | Latest |
| Build Tool | Maven | 3.6+ |
| CI/CD | GitHub Actions | Latest |
| Packaging | jpackage (JDK 21+) | Built-in |

## 🎨 Visual Design (Current)

Currently uses **procedurally generated colored shapes**:
- **Player**: Blue square
- **NPCs**: Green squares
- **Enemies**: Red/White/Green/Dark-red squares (by type)
- **Projectiles**: Yellow squares
- **Terrain**: Color-coded tiles (grass=green, water=blue, etc.)

**Ready for sprites**: Structure supports easy texture integration from Kenney.nl or other sources.

## 🚀 Getting Started (3 Options)

### Option 1: Download Release (Easiest)
1. Download installer from Releases page
2. Install and run
3. (Optional) Configure AI provider

### Option 2: Run JAR (Quickest)
```bash
java -jar jmonkey-vibe-game-1.0.0-standalone.jar
```

### Option 3: Build from Source (Developers)
```bash
git clone https://github.com/JohannesRabauer/jmonkey-vibe-code.git
cd jmonkey-vibe-code
mvn clean package
java -jar target/jmonkey-vibe-game-1.0.0-standalone.jar
```

## 🤖 AI Setup (Optional but Recommended)

### Ollama (Free, Local) - Default
```bash
# Install Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# Pull model
ollama pull llama3.1

# Start server
ollama serve

# Run game (no env vars needed, Ollama is default)
```

### OpenAI (Paid)
```bash
# Set provider and API key
export AI_PROVIDER=OPENAI
export OPENAI_API_KEY=sk-your-key-here

# Run game
java -jar jmonkey-vibe-game-1.0.0-standalone.jar
```

## 📊 Performance Characteristics

- **JAR Size**: ~15-20 MB (with dependencies)
- **Memory Usage**: 512 MB - 2 GB (configurable)
- **Startup Time**: 2-5 seconds
- **FPS**: 60 (vsync enabled)
- **World Size**: Configurable (default 20x20 tiles)
- **Dungeon Size**: Up to 15 rooms (procedural)

## 🎯 Key Design Decisions

1. **2D in 3D Engine**: Uses jMonkeyEngine's 3D capabilities with orthographic camera for true 2D gameplay
2. **Modular AI**: Supports multiple AI providers via environment config
3. **State-Based**: Clean separation between exploration and combat
4. **Procedural First**: All content generated algorithmically
5. **Asset-Ready**: Easy to swap colored shapes for real sprites
6. **Cross-Platform**: Single codebase, multiple platform builds
7. **Open Source**: MIT licensed, community-driven

## 🔮 Future Enhancements (Not Yet Implemented)

- [ ] Actual sprite integration (currently colored shapes)
- [ ] Save/load game persistence
- [ ] Inventory and equipment system
- [ ] Dialogue UI overlay (currently console-based)
- [ ] Sound effects and background music
- [ ] Multiplayer/co-op support
- [ ] More sophisticated AI memory
- [ ] Minimap and HUD
- [ ] Character creation/customization
- [ ] Skill trees and progression
- [ ] Boss battles with unique mechanics
- [ ] Biome variety in procedural generation

## 🐛 Known Limitations

1. **Mouse Aiming**: Uses simplified direction calculation (needs cursor-to-world coordinate conversion)
2. **No Collision Detection**: Player can walk through walls (physics system needed)
3. **Basic Enemy AI**: Simple chase behavior (needs pathfinding)
4. **Console Dialogue**: NPC conversations print to console (needs UI overlay)
5. **No Persistence**: Game state not saved between sessions
6. **Placeholder Graphics**: Colored squares instead of sprites

## 📝 Code Quality

- ✅ Compiles cleanly with no errors
- ✅ Java 17 compliance
- ✅ Proper package structure
- ✅ Maven best practices
- ✅ Documented classes
- ✅ Extensible architecture
- ⚠️ No unit tests yet
- ⚠️ Limited error handling
- ⚠️ Basic logging

## 🎓 Learning Outcomes

This project demonstrates:
- Game engine integration (jMonkeyEngine)
- AI/LLM integration (langchain4j)
- Procedural content generation
- State machine patterns
- Entity-component design
- Cross-platform build automation
- GitHub Actions CI/CD
- Maven project management
- Open source project structure

## 📄 License

**MIT License** - Free for commercial and non-commercial use.

## 🤝 Contributing

See [README.md](README.md) for contribution guidelines.

---

**Status**: ✅ **Fully Implemented & Buildable**
**Last Updated**: January 18, 2026
