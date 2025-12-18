package com.thebridgetoai.voice;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class SpeechToText {
    
    private static final String WHISPER_ENDPOINT = "http://localhost:9000/asr";
    private final HttpClient httpClient;
    
    public SpeechToText() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    public String transcribe(byte[] audioData) {
        try {
            return transcribeWithWhisperAPI(audioData);
        } catch (Exception e) {
            System.err.println("Speech-to-text failed: " + e.getMessage());
            return fallbackTranscription(audioData);
        }
    }
    
    private String transcribeWithWhisperAPI(byte[] audioData) throws Exception {
        Path tempFile = Files.createTempFile("voice_", ".wav");
        
        try {
            writeWavFile(audioData, tempFile);
            
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            String body = buildMultipartBody(tempFile, boundary);
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(WHISPER_ENDPOINT))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseTranscriptionResponse(response.body());
            } else {
                throw new RuntimeException("Whisper API returned status: " + response.statusCode());
            }
            
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    
    private void writeWavFile(byte[] audioData, Path outputPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
            writeWavHeader(fos, audioData.length);
            fos.write(audioData);
        }
    }
    
    private void writeWavHeader(FileOutputStream fos, int dataLength) throws IOException {
        int sampleRate = 16000;
        int channels = 1;
        int bitsPerSample = 16;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int totalDataLen = dataLength + 36;
        
        fos.write("RIFF".getBytes());
        fos.write(intToByteArray(totalDataLen));
        fos.write("WAVE".getBytes());
        fos.write("fmt ".getBytes());
        fos.write(intToByteArray(16));
        fos.write(shortToByteArray((short) 1));
        fos.write(shortToByteArray((short) channels));
        fos.write(intToByteArray(sampleRate));
        fos.write(intToByteArray(byteRate));
        fos.write(shortToByteArray((short) blockAlign));
        fos.write(shortToByteArray((short) bitsPerSample));
        fos.write("data".getBytes());
        fos.write(intToByteArray(dataLength));
    }
    
    private byte[] intToByteArray(int value) {
        return new byte[] {
            (byte) (value & 0xff),
            (byte) ((value >> 8) & 0xff),
            (byte) ((value >> 16) & 0xff),
            (byte) ((value >> 24) & 0xff)
        };
    }
    
    private byte[] shortToByteArray(short value) {
        return new byte[] {
            (byte) (value & 0xff),
            (byte) ((value >> 8) & 0xff)
        };
    }
    
    private String buildMultipartBody(Path audioFile, String boundary) throws IOException {
        StringBuilder body = new StringBuilder();
        body.append("--").append(boundary).append("\r\n");
        body.append("Content-Disposition: form-data; name=\"audio_file\"; filename=\"audio.wav\"\r\n");
        body.append("Content-Type: audio/wav\r\n\r\n");
        body.append(Files.readString(audioFile));
        body.append("\r\n--").append(boundary).append("--\r\n");
        return body.toString();
    }
    
    private String parseTranscriptionResponse(String response) {
        try {
            if (response.contains("\"text\":")) {
                int textStart = response.indexOf("\"text\":\"") + 8;
                int textEnd = response.indexOf("\"", textStart);
                return response.substring(textStart, textEnd);
            }
            return response.trim();
        } catch (Exception e) {
            return response;
        }
    }
    
    private String fallbackTranscription(byte[] audioData) {
        System.out.println("Using fallback transcription method...");
        System.out.println("Note: This is a placeholder. For real speech-to-text, consider:");
        System.out.println("1. Running a local Whisper server (OpenAI Whisper)");
        System.out.println("2. Using Google Speech-to-Text API");
        System.out.println("3. Using Azure Cognitive Services Speech");
        System.out.println("4. Using Amazon Transcribe");
        
        return "I heard you say something, but speech recognition is not configured yet. Audio data: " 
            + audioData.length + " bytes";
    }
}