# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Run Commands

```bash
# Compile and run directly
mvn clean compile exec:java

# Package standalone JAR (creates fat JAR with all dependencies)
mvn clean package

# Run the packaged game
java -jar target/jmonkey-vibe-game-1.0.0-standalone.jar
```

No test framework is currently configured in the project.

## AI Provider Configuration

The game requires an AI provider for NPC dialogues. Set environment variables before running:

**OpenAI (default):**
```bash
export AI_PROVIDER=OPENAI
export OPENAI_API_KEY=your-api-key
```

**Ollama (local/free):**
```bash
export AI_PROVIDER=OLLAMA
# Ensure Ollama is running: ollama serve
# Pull model if needed: ollama pull llama3.1
```

If no AI provider is configured, NPCs use fallback dialogue.

## Architecture Overview

This is a 2D top-down RPG using jMonkeyEngine 3D engine with orthographic projection. The game has two distinct modes managed via jMonkeyEngine's AppState system.

### State Machine Pattern

`Main.java` extends `SimpleApplication` and manages two game states:
- **ExplorationState**: Overworld navigation, NPC interaction, dialog system
- **DungeonCombatState**: Twin-stick combat with WASD movement + mouse aiming

States are switched via `Main.enterDungeon()` / `Main.exitDungeon()` which detach/attach the appropriate AppState.

### Key Subsystems

**AI Integration (`ai/` package):**
- `AIModelFactory` - Factory that creates langchain4j chat models based on `AI_PROVIDER` env var. Supports both regular and streaming models for OpenAI and Ollama.
- `NPCConversationManager` - Manages conversation state per NPC with history, streaming responses, and dynamic response option generation.

**Game States (`states/` package):**
- Both states implement `BaseAppState` and `ActionListener` for input handling
- Each state manages its own input mappings, scene graph node, and update loop
- Camera follows player in both modes (orthographic projection)

**World Generation (`world/` package):**
- `WorldGenerator` - Procedural overworld tile generation
- `DungeonGenerator` - Procedural dungeon layout generation

**Combat (`combat/` package):**
- `CombatManager` - Handles projectile firing, enemy spawning, and combat updates

**Entities (`entities/` package):**
- `Player`, `NPC`, `Enemy`, `Projectile` - Game entity classes with their own Spatial representations

### Rendering Approach

The game uses jMonkeyEngine's 3D scene graph for 2D rendering:
- Orthographic camera looking down (Y=100) at XZ plane
- Entities are Quads rotated to face camera (`-FastMath.HALF_PI` on X axis)
- Materials use `Common/MatDefs/Misc/Unshaded.j3md` with solid colors

### Dialog System

NPC dialogs in ExplorationState use:
1. Streaming AI responses displayed token-by-token via `DialogUI`
2. Three numbered response choices (keys 1-3) + custom text input (T key)
3. Conversation history maintained per NPC in `NPCConversationManager`
