# Voice Conversation Program

A Java application that enables voice conversations with a local LLM by capturing microphone input, converting speech to text, sending it to an LLM, and responding with text-to-speech.

## Features

- **Microphone Audio Capture**: Records audio from your default microphone
- **Speech-to-Text**: Converts recorded audio to text (with Whisper API support)
- **Local LLM Integration**: Sends prompts to a local LLM server (Ollama compatible)
- **Text-to-Speech**: Converts LLM responses back to speech
- **Cross-platform**: Works on macOS, Windows, and Linux with fallback TTS options

## Usage

### Prerequisites

1. **Local LLM Server**: Run a local LLM server like Ollama:
   ```bash
   # Install Ollama
   curl -fsSL https://ollama.ai/install.sh | sh
   
   # Pull a model (e.g., Llama 3)
   ollama pull llama3
   
   # Start the server (default: http://localhost:11434)
   ollama serve
   ```

2. **Optional Speech Services**:
   - **Speech-to-Text**: Run a local Whisper server at `http://localhost:9000/asr`
   - **Text-to-Speech**: Run a local TTS server at `http://localhost:5002/api/tts`

### Running the Program

1. **Compile and run**:
   ```bash
   # From the project root
   ./gradlew bootRun -Pmain-class=com.thebridgetoai.voice.VoiceConversationMain
   ```

2. **Or run with custom parameters**:
   ```bash
   # Custom LLM endpoint and model
   java -cp build/classes/java/main com.thebridgetoai.voice.VoiceConversationMain \
     "http://localhost:11434/api/generate" "llama3"
   ```

### Controls

- **Press Enter**: Start recording audio
- **Press Enter again**: Stop recording and process
- **Type 'q' and Enter**: Quit the application

## Architecture

### Classes

- **`VoiceConversationMain`**: Main application class with conversation loop
- **`AudioCapture`**: Handles microphone audio recording using Java Sound API
- **`SpeechToText`**: Converts audio to text with Whisper API integration
- **`TextToSpeech`**: Converts text responses to speech with multiple fallback options

### Audio Format

- **Sample Rate**: 16 kHz
- **Bit Depth**: 16-bit
- **Channels**: Mono (1 channel)
- **Format**: PCM, signed, little-endian

## Fallback Options

### Speech-to-Text Fallbacks
- If Whisper API is unavailable, displays a placeholder message with audio data size

### Text-to-Speech Fallbacks
1. **macOS**: Uses built-in `say` command
2. **Windows**: Uses Windows Speech API (SAPI)
3. **Linux**: Tries `espeak` or `festival` if available
4. **All platforms**: Displays text response if no TTS is available

## Configuration

### Default Endpoints
- **LLM**: `http://localhost:11434/api/generate` (Ollama)
- **Speech-to-Text**: `http://localhost:9000/asr`
- **Text-to-Speech**: `http://localhost:5002/api/tts`

### Default Model
- **LLM Model**: `llama3`

## Dependencies

All dependencies are managed through Gradle and use standard Java libraries:
- Java Sound API (javax.sound.sampled)
- Java HTTP Client (java.net.http)
- Standard I/O and file operations

## Troubleshooting

### Common Issues

1. **No microphone detected**:
   - Run `AudioCapture.listAvailableMicrophones()` to see available devices
   - Check system audio permissions

2. **LLM connection failed**:
   - Ensure Ollama or your LLM server is running
   - Verify the endpoint URL and model name
   - Check firewall settings

3. **No speech output**:
   - Check if system TTS commands are available
   - Verify audio output device is working
   - Check volume settings

### System Requirements

- Java 17 or higher
- Microphone access permissions
- Audio output device
- Network access to LLM server

## Example Session

```
Voice Conversation App Starting...
LLM Endpoint: http://localhost:11434/api/generate
Model: llama3
Press 'q' and Enter to quit, or just press Enter to start listening...

Press Enter to start recording (or 'q' to quit):
[Press Enter]
Listening... Press Enter to stop recording.
[Speak into microphone, then press Enter]
Recording stopped. Processing...
Converting speech to text...
You said: Hello, how are you today?
AI Response: Hello! I'm doing well, thank you for asking. How can I help you today?
[AI response is spoken through speakers/headphones]
```