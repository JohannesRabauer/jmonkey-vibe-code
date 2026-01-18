# JMonkey Vibe Game 🎮

A completely free, open-source 2D top-down RPG built with Java and jMonkeyEngine, featuring AI-powered NPC dialogues, procedural generation, dungeon combat with twin-stick controls, and automatic cross-platform builds.

![Build Status](https://github.com/JohannesRabauer/jmonkey-vibe-code/workflows/Build%20and%20Package%20Game/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)

## ✨ Features

- 🤖 **AI-Powered NPCs**: Dynamic conversations using OpenAI or local Ollama models via langchain4j
- 🗺️ **Procedural Generation**: Randomly generated overworld maps and dungeons
- ⚔️ **Twin-Stick Combat**: Action-packed dungeon battles with WASD movement + mouse aiming
- 📜 **Quest System**: Procedurally generated quests with various objectives
- 👥 **Teammate Recruitment**: Build your party with NPC companions
- 🎨 **2D Top-Down Graphics**: Classic RPG perspective using jMonkeyEngine
- 🌍 **Cross-Platform**: Runs on Windows, Linux, and macOS
- 🚀 **Automated Builds**: GitHub Actions creates installers for all platforms

## 🎮 Gameplay

### Exploration Mode
- Navigate a procedurally generated overworld using **WASD** keys
- Interact with NPCs by pressing **E** when nearby
- Engage in AI-powered conversations that adapt to your choices
- Accept quests and recruit teammates

### Dungeon Combat Mode
- **WASD**: Move your character
- **Mouse**: Aim your weapon
- **Left Click**: Fire projectiles
- **ESC**: Exit dungeon
- Fight procedurally spawned enemies in randomly generated dungeons

## 🚀 Quick Start

### Download & Play (Easiest)

1. Go to [Releases](https://github.com/JohannesRabauer/jmonkey-vibe-code/releases)
2. Download the installer for your platform:
   - **Windows**: `JMonkeyVibeGame.exe`
   - **Linux**: `jmonkey-vibe-game.deb`
   - **macOS**: `JMonkey Vibe Game.dmg`
3. Install and run!

### Run from JAR

```bash
# Download the standalone JAR from releases
java -jar jmonkey-vibe-game-1.0.0-standalone.jar
```

### Build from Source

```bash
# Clone the repository
git clone https://github.com/JohannesRabauer/jmonkey-vibe-code.git
cd jmonkey-vibe-code

# Build with Maven
mvn clean package

# Run the game
java -jar target/jmonkey-vibe-game-1.0.0-standalone.jar
```

## 🤖 AI Configuration

The game supports two AI providers for NPC dialogues:

### Option 1: Ollama (Recommended - Free & Local)

1. Install Ollama: https://ollama.ai
2. Pull a model:
   ```bash
   ollama pull llama3.1
   ```
3. Start Ollama:
   ```bash
   ollama serve
   ```
4. Set environment variable (optional, default is Ollama):
   ```bash
   export AI_PROVIDER=OLLAMA
   ```

### Option 2: OpenAI API

1. Get an API key from https://platform.openai.com
2. Set environment variables:
   ```bash
   export AI_PROVIDER=OPENAI
   export OPENAI_API_KEY=your-api-key-here
   ```

### Fallback Mode

If no AI provider is configured, the game uses simple fallback dialogues.

## 🛠️ Development

### Prerequisites

- **Java 17+** (JDK 21 recommended for building installers)
- **Maven 3.6+**
- **Git**

### Project Structure

```
jmonkey-vibe-code/
├── src/main/java/com/jmonkeyvibe/game/
│   ├── Main.java                  # Entry point
│   ├── ai/                        # AI dialogue system
│   │   ├── AIModelFactory.java
│   │   └── NPCConversationManager.java
│   ├── combat/                    # Combat mechanics
│   │   └── CombatManager.java
│   ├── entities/                  # Game entities
│   │   ├── Player.java
│   │   ├── NPC.java
│   │   ├── Enemy.java
│   │   └── Projectile.java
│   ├── quest/                     # Quest system
│   │   ├── Quest.java
│   │   └── QuestGenerator.java
│   ├── states/                    # Game states
│   │   ├── ExplorationState.java
│   │   └── DungeonCombatState.java
│   └── world/                     # Procedural generation
│       ├── WorldGenerator.java
│       └── DungeonGenerator.java
├── src/main/resources/
│   ├── Textures/                  # Sprite assets (add your own)
│   └── ASSET_ATTRIBUTION.md
├── .github/workflows/
│   └── build.yml                  # CI/CD pipeline
├── pom.xml                        # Maven configuration
├── LICENSE                        # MIT License
└── README.md
```

### Building

```bash
# Compile and run
mvn clean compile exec:java

# Package standalone JAR
mvn clean package

# Create platform-specific installer (requires JDK 21+)
# Windows: Creates .exe
# Linux: Creates .deb  
# macOS: Creates .dmg
mvn clean package
jpackage --input target/jpackage-input \
  --main-jar jmonkey-vibe-game-1.0.0.jar \
  --main-class com.jmonkeyvibe.game.Main \
  --name "JMonkey Vibe Game" \
  --app-version 1.0.0
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `AI_PROVIDER` | AI provider: `OPENAI` or `OLLAMA` | `OLLAMA` |
| `OPENAI_API_KEY` | Your OpenAI API key | None |
| `OPENAI_BASE_URL` | Custom OpenAI endpoint | `https://api.openai.com/v1` |
| `OLLAMA_BASE_URL` | Ollama server URL | `http://localhost:11434` |
| `OLLAMA_MODEL` | Ollama model name | `llama3.1` |

## 🎨 Adding Custom Assets

The game currently uses procedurally generated colored shapes. To add custom sprites:

1. Download CC0 assets from [Kenney.nl](https://kenney.nl/assets)
2. Extract PNG files to `src/main/resources/Textures/`
3. Update entity classes to load textures:

```java
Texture playerTexture = assetManager.loadTexture("Textures/characters/player.png");
Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
mat.setTexture("ColorMap", playerTexture);
```

See [ASSET_ATTRIBUTION.md](src/main/resources/ASSET_ATTRIBUTION.md) for details.

## 🤝 Contributing

Contributions are welcome! This is a completely open-source project.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📋 Roadmap

- [ ] Enhanced AI dialogue with memory and context
- [ ] Save/load game system
- [ ] More enemy types and boss battles
- [ ] Inventory and equipment system
- [ ] Multiplayer support
- [ ] Custom sprite assets integration
- [ ] Sound effects and music
- [ ] Advanced quest branching
- [ ] Player character customization

## 🐛 Known Issues

- Mouse aiming in dungeon combat uses simplified direction calculation
- No collision detection with dungeon walls yet
- AI dialogue requires active internet connection (OpenAI) or local Ollama server

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **jMonkeyEngine** - 3D game engine adapted for 2D rendering
- **langchain4j** - Java framework for AI integration
- **Kenney.nl** - Free CC0 game assets
- **OpenAI & Ollama** - AI language models for NPC dialogues

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/JohannesRabauer/jmonkey-vibe-code/issues)
- **Discussions**: [GitHub Discussions](https://github.com/JohannesRabauer/jmonkey-vibe-code/discussions)

---

**Made with ❤️ using Java, jMonkeyEngine, and AI**
