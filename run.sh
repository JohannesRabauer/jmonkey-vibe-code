#!/bin/bash
# Quick start script for JMonkey Vibe Game on Linux/macOS

echo "========================================"
echo "  JMonkey Vibe Game - Quick Start"
echo "========================================"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found! Please install Maven first."
    echo "Visit: https://maven.apache.org/download.cgi"
    exit 1
fi

echo "Building the game..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Build failed!"
    exit 1
fi

echo ""
echo "========================================"
echo "  Starting JMonkey Vibe Game..."
echo "========================================"
echo ""
echo "AI Provider: ${AI_PROVIDER:-OLLAMA (default)}"
echo "To use OpenAI: export AI_PROVIDER=OPENAI"
echo "To use Ollama: export AI_PROVIDER=OLLAMA"
echo ""

# Run the standalone JAR
java -jar target/jmonkey-vibe-game-1.0.0-standalone.jar
