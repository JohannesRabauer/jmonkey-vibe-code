# Development Setup Guide

## Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
   - Download: https://adoptium.net/
   - Verify: `java -version`

2. **Apache Maven 3.6+**
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn -version`

3. **Git**
   - Download: https://git-scm.com/
   - Verify: `git --version`

4. **AI Provider (Optional but recommended)**
   - **Ollama** (recommended, free): https://ollama.ai
   - **OpenAI** (paid): https://platform.openai.com

## Quick Setup

### 1. Clone the Repository

```bash
git clone https://github.com/JohannesRabauer/jmonkey-vibe-code.git
cd jmonkey-vibe-code
```

### 2. Build the Project

```bash
mvn clean package
```

### 3. Run the Game

**Windows:**
```cmd
run.bat
```

**Linux/macOS:**
```bash
chmod +x run.sh
./run.sh
```

**Or directly with Java:**
```bash
java -jar target/jmonkey-vibe-game-1.0.0-standalone.jar
```

## AI Configuration

### Using Ollama (Free, Local)

1. Install Ollama from https://ollama.ai
2. Pull a model:
   ```bash
   ollama pull llama3.1
   ```
3. Start Ollama (usually starts automatically):
   ```bash
   ollama serve
   ```
4. The game will use Ollama by default

### Using OpenAI (Paid)

1. Get an API key from https://platform.openai.com
2. Set environment variables:

**Windows (Command Prompt):**
```cmd
set AI_PROVIDER=OPENAI
set OPENAI_API_KEY=your-api-key-here
```

**Windows (PowerShell):**
```powershell
$env:AI_PROVIDER="OPENAI"
$env:OPENAI_API_KEY="your-api-key-here"
```

**Linux/macOS:**
```bash
export AI_PROVIDER=OPENAI
export OPENAI_API_KEY=your-api-key-here
```

## IDE Setup

### IntelliJ IDEA

1. Open IntelliJ IDEA
2. File → Open → Select `jmonkey-vibe-code` directory
3. IntelliJ will detect the Maven project automatically
4. Wait for dependencies to download
5. Right-click `Main.java` → Run

### Eclipse

1. Open Eclipse
2. File → Import → Maven → Existing Maven Projects
3. Select `jmonkey-vibe-code` directory
4. Wait for dependencies to download
5. Right-click project → Run As → Java Application → Select `Main`

### VS Code

1. Install Java Extension Pack
2. Open folder `jmonkey-vibe-code`
3. VS Code will detect Maven project
4. Press F5 to run or use the Run button

## Building Platform-Specific Installers

Requires **JDK 21+** for jpackage.

### Windows (.exe)

```bash
mvn clean package
jpackage --input target/jpackage-input \
  --main-jar jmonkey-vibe-game-1.0.0.jar \
  --main-class com.jmonkeyvibe.game.Main \
  --name "JMonkey Vibe Game" \
  --type exe \
  --app-version 1.0.0
```

### Linux (.deb)

```bash
mvn clean package
jpackage --input target/jpackage-input \
  --main-jar jmonkey-vibe-game-1.0.0.jar \
  --main-class com.jmonkeyvibe.game.Main \
  --name jmonkey-vibe-game \
  --type deb \
  --app-version 1.0.0
```

### macOS (.dmg)

```bash
mvn clean package
jpackage --input target/jpackage-input \
  --main-jar jmonkey-vibe-game-1.0.0.jar \
  --main-class com.jmonkeyvibe.game.Main \
  --name "JMonkey Vibe Game" \
  --type dmg \
  --app-version 1.0.0
```

## Common Issues

### "Maven not found"
- Install Maven and ensure it's in your PATH
- Verify with `mvn -version`

### "Java version mismatch"
- Ensure you have JDK 17 or higher
- Set JAVA_HOME environment variable

### "AI model not responding"
- For Ollama: Check if `ollama serve` is running
- For OpenAI: Verify your API key is set correctly
- Check internet connection

### "jpackage not found"
- jpackage requires JDK 21+
- It's included in the JDK, not a separate download

### "Build fails with dependency errors"
- Clear Maven cache: `mvn clean`
- Delete `~/.m2/repository` and rebuild

## Testing

Run tests with:
```bash
mvn test
```

## Contributing

1. Create a feature branch: `git checkout -b feature/my-feature`
2. Make your changes
3. Test thoroughly
4. Commit: `git commit -m "Add my feature"`
5. Push: `git push origin feature/my-feature`
6. Create a Pull Request

## Performance Tuning

### Increase Memory (if game runs slowly)

Edit run scripts or run directly:
```bash
java -Xmx4g -Xms1g -jar target/jmonkey-vibe-game-1.0.0-standalone.jar
```

### Enable Debug Logging

```bash
java -Djava.util.logging.config.file=logging.properties -jar target/*.jar
```

## Project Structure

```
src/main/java/
├── com.jmonkeyvibe.game/
│   ├── Main.java              # Entry point
│   ├── ai/                    # AI dialogue system
│   ├── combat/                # Combat mechanics
│   ├── entities/              # Game entities
│   ├── quest/                 # Quest system
│   ├── states/                # Game states
│   └── world/                 # Procedural generation
```

## Useful Maven Commands

- `mvn clean` - Clean build artifacts
- `mvn compile` - Compile source code
- `mvn package` - Build JAR file
- `mvn test` - Run tests
- `mvn dependency:tree` - Show dependency tree
- `mvn versions:display-dependency-updates` - Check for updates

## Resources

- jMonkeyEngine Docs: https://wiki.jmonkeyengine.org/
- langchain4j Docs: https://docs.langchain4j.dev/
- Maven Guide: https://maven.apache.org/guides/

## License

MIT License - See LICENSE file for details
