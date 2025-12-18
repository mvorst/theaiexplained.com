package com.thebridgetoai.voice;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceConversationMain {
    
    private static final String DEFAULT_LLM_ENDPOINT = "http://localhost:11434/api/generate";
    private static final String DEFAULT_MODEL = "llama3";
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNELS = 1;
    
    private final HttpClient httpClient;
    private final ExecutorService executorService;
    private final AudioCapture audioCapture;
    private final SpeechToText speechToText;
    private final TextToSpeech textToSpeech;
    private final String llmEndpoint;
    private final String modelName;
    
    public VoiceConversationMain(String llmEndpoint, String modelName) {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.executorService = Executors.newCachedThreadPool();
        this.audioCapture = new AudioCapture();
        this.speechToText = new SpeechToText();
        this.textToSpeech = new TextToSpeech();
        this.llmEndpoint = llmEndpoint != null ? llmEndpoint : DEFAULT_LLM_ENDPOINT;
        this.modelName = modelName != null ? modelName : DEFAULT_MODEL;
    }
    
    public static void main(String[] args) {
        String llmEndpoint = args.length > 0 ? args[0] : null;
        String modelName = args.length > 1 ? args[1] : null;
        
        VoiceConversationMain app = new VoiceConversationMain(llmEndpoint, modelName);
        
        System.out.println("Voice Conversation App Starting...");
        System.out.println("LLM Endpoint: " + app.llmEndpoint);
        System.out.println("Model: " + app.modelName);
        System.out.println("Press 'q' and Enter to quit, or just press Enter to start listening...");
        
        try {
            app.startConversationLoop();
        } catch (Exception e) {
            System.err.println("Error during conversation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            app.shutdown();
        }
    }
    
    private void startConversationLoop() throws IOException {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        
        while (true) {
            System.out.println("\nPress Enter to start recording (or 'q' to quit):");
            String input = consoleReader.readLine();
            
            if ("q".equalsIgnoreCase(input)) {
                break;
            }
            
            try {
                System.out.println("Listening... Press Enter to stop recording.");
                
                CompletableFuture<byte[]> audioFuture = CompletableFuture.supplyAsync(() -> {
                    try {
                        return audioCapture.recordAudio();
                    } catch (Exception e) {
                        throw new RuntimeException("Audio capture failed", e);
                    }
                }, executorService);
                
                consoleReader.readLine();
                audioCapture.stopRecording();
                
                byte[] audioData = audioFuture.get();
                System.out.println("Recording stopped. Processing...");
                
                String transcription = speechToText(audioData);
                if (transcription == null || transcription.trim().isEmpty()) {
                    System.out.println("No speech detected. Try again.");
                    continue;
                }
                
                System.out.println("You said: " + transcription);
                
                String llmResponse = callLocalLLM(transcription);
                System.out.println("AI Response: " + llmResponse);
                
                textToSpeech.speak(llmResponse);
                
            } catch (Exception e) {
                System.err.println("Error processing conversation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private String speechToText(byte[] audioData) {
        System.out.println("Converting speech to text...");
        return speechToText.transcribe(audioData);
    }
    
    private String callLocalLLM(String prompt) throws Exception {
        String jsonPayload = String.format(
            "{\"model\":\"%s\",\"prompt\":\"%s\",\"stream\":false}",
            modelName,
            prompt.replace("\"", "\\\"")
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(llmEndpoint))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .timeout(Duration.ofSeconds(30))
            .build();
        
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                return extractResponseFromJson(responseBody);
            } else {
                throw new RuntimeException("LLM API returned status: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to LLM. Is your local LLM server running?");
            return "I'm sorry, I couldn't process your request. Please check if the LLM server is running.";
        }
    }
    
    private String extractResponseFromJson(String jsonResponse) {
        try {
            int responseStart = jsonResponse.indexOf("\"response\":\"") + 12;
            int responseEnd = jsonResponse.indexOf("\",\"done\":", responseStart);
            if (responseEnd == -1) {
                responseEnd = jsonResponse.lastIndexOf("\"");
            }
            return jsonResponse.substring(responseStart, responseEnd)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
        } catch (Exception e) {
            System.err.println("Failed to parse LLM response: " + e.getMessage());
            return "I received a response but couldn't parse it properly.";
        }
    }
    
    private void shutdown() {
        System.out.println("Shutting down...");
        executorService.shutdown();
        textToSpeech.shutdown();
    }
}