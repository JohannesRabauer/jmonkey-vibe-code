@echo off
REM Quick start script for JMonkey Vibe Game on Windows

echo ========================================
echo  JMonkey Vibe Game - Quick Start
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found! Please install Maven first.
    echo Download from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo Building the game...
call mvn clean package -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo  Starting JMonkey Vibe Game...
echo ========================================
echo.
echo AI Provider: %AI_PROVIDER%
echo To use OpenAI: set AI_PROVIDER=OPENAI
echo To use Ollama: set AI_PROVIDER=OLLAMA
echo.

REM Run the standalone JAR
java -jar target\jmonkey-vibe-game-1.0.0-standalone.jar

pause
