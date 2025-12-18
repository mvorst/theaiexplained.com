package com.thebridgetoai.voice;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AudioCapture {
    
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    private TargetDataLine targetDataLine;
    private volatile boolean recording = false;
    
    public byte[] recordAudio() throws LineUnavailableException, IOException {
        AudioFormat audioFormat = new AudioFormat(
            SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN
        );
        
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        
        if (!AudioSystem.isLineSupported(dataLineInfo)) {
            throw new IllegalStateException("Audio line not supported");
        }
        
        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        
        recording = true;
        
        ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        
        while (recording) {
            int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                audioStream.write(buffer, 0, bytesRead);
            }
        }
        
        targetDataLine.stop();
        targetDataLine.close();
        
        return audioStream.toByteArray();
    }
    
    public void stopRecording() {
        recording = false;
    }
    
    public static void listAvailableMicrophones() {
        System.out.println("Available audio input devices:");
        
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            
            Line.Info[] targetLineInfos = mixer.getTargetLineInfo();
            if (targetLineInfos.length > 0) {
                System.out.println("- " + mixerInfo.getName() + " (" + mixerInfo.getDescription() + ")");
            }
        }
    }
}