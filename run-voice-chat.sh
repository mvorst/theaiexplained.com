#!/bin/bash

# Voice Conversation Program Runner
# This script compiles and runs the voice conversation program

echo "Compiling Java classes..."
./gradlew compileJava

if [ $? -eq 0 ]; then
    echo "Starting Voice Conversation Program..."
    echo "Make sure you have:"
    echo "1. A local LLM server running (e.g., Ollama at localhost:11434)"
    echo "2. Microphone permissions enabled"
    echo "3. Audio output working"
    echo ""
    
    # Create classpath with all dependencies
    CLASSPATH="build/classes/java/main"
    
    # Add Spring Boot and other dependencies
    for jar in $(find ~/.gradle/caches -name "*.jar" 2>/dev/null | grep -E "(spring-boot|jackson|httpclient)" | head -10); do
        CLASSPATH="$CLASSPATH:$jar"
    done
    
    java -cp "$CLASSPATH" com.thebridgetoai.voice.VoiceConversationMain "$@"
else
    echo "Compilation failed!"
    exit 1
fi