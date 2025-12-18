package com.thebridgetoai.voice;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TextToSpeech {
    
    private static final String TTS_ENDPOINT = "http://localhost:5002/api/tts";
    private final HttpClient httpClient;
    private final ExecutorService executorService;
    
    public TextToSpeech() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public void speak(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                synthesizeAndPlay(text);
            } catch (Exception e) {
                System.err.println("Text-to-speech failed: " + e.getMessage());
                fallbackSpeech(text);
            }
        }, executorService);
    }
    
    private void synthesizeAndPlay(String text) throws Exception {
        String jsonPayload = String.format("{\"text\":\"%s\",\"voice\":\"en-us\"}", 
            text.replace("\"", "\\\""));
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(TTS_ENDPOINT))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .timeout(Duration.ofSeconds(15))
            .build();
        
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        
        if (response.statusCode() == 200) {
            playAudioData(response.body());
        } else {
            throw new RuntimeException("TTS API returned status: " + response.statusCode());
        }
    }
    
    private void playAudioData(byte[] audioData) throws Exception {
        Path tempFile = Files.createTempFile("tts_", ".wav");
        
        try {
            Files.write(tempFile, audioData);
            playWavFile(tempFile);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    
    private void playWavFile(Path wavFile) throws Exception {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile.toFile())) {
            AudioFormat audioFormat = audioInputStream.getFormat();
            
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                sourceDataLine.write(buffer, 0, bytesRead);
            }
            
            sourceDataLine.drain();
            sourceDataLine.close();
        }
    }
    
    private void fallbackSpeech(String text) {
        System.out.println("\n=== AI RESPONSE (Text-to-Speech not available) ===");
        System.out.println(text);
        System.out.println("===================================================");
        System.out.println("Note: For actual speech synthesis, consider:");
        System.out.println("1. Running a local TTS server (e.g., Coqui TTS, eSpeak-ng)");
        System.out.println("2. Using Google Text-to-Speech API");
        System.out.println("3. Using Azure Cognitive Services Speech");
        System.out.println("4. Using Amazon Polly");
        System.out.println("5. Using system TTS (macOS: say command, Windows: SAPI)");
        
        trySystemTTS(text);
    }
    
    private void trySystemTTS(String text) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"say", text});
                System.out.println("Using macOS 'say' command for speech...");
            } else if (os.contains("win")) {
                String vbsScript = "CreateObject(\"SAPI.SpVoice\").Speak \"" + text.replace("\"", "\\\"") + "\"";
                Runtime.getRuntime().exec(new String[]{"wscript", "/E:vbscript", "/NoLogo", vbsScript});
                System.out.println("Using Windows SAPI for speech...");
            } else if (os.contains("linux")) {
                if (isCommandAvailable("espeak")) {
                    Runtime.getRuntime().exec(new String[]{"espeak", text});
                    System.out.println("Using espeak for speech...");
                } else if (isCommandAvailable("festival")) {
                    String[] cmd = {"festival", "--tts"};
                    Process process = Runtime.getRuntime().exec(cmd);
                    try (PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                        writer.println(text);
                    }
                    System.out.println("Using festival for speech...");
                }
            }
        } catch (Exception e) {
            System.err.println("System TTS also failed: " + e.getMessage());
        }
    }
    
    private boolean isCommandAvailable(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"which", command});
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}