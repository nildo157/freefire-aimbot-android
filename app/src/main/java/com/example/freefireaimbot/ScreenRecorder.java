package com.example.freefireaimbot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScreenRecorder {
    private static final String TAG = "ScreenRecorder";
    private static final int SCREEN_RECORD_REQUEST_CODE = 1003;
    
    private static ScreenRecorder instance;
    private Context context;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private MediaRecorder mediaRecorder;
    private VirtualDisplay virtualDisplay;
    
    private boolean isRecording = false;
    private String outputFilePath;
    private RecordingCallback callback;
    
    // Configurações de gravação
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;
    private int videoBitRate = 5000000; // 5 Mbps
    private int videoFrameRate = 30;
    private int audioSampleRate = 44100;
    private int audioBitRate = 128000;
    
    public interface RecordingCallback {
        void onRecordingStarted(String filePath);
        void onRecordingStopped(String filePath);
        void onRecordingError(String error);
    }
    
    private ScreenRecorder() {}
    
    public static synchronized ScreenRecorder getInstance() {
        if (instance == null) {
            instance = new ScreenRecorder();
        }
        return instance;
    }
    
    public void initialize(Context context, RecordingCallback callback) {
        this.context = context;
        this.callback = callback;
        
        projectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        // Obter dimensões da tela
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.densityDpi;
        
        Log.d(TAG, "Screen dimensions: " + screenWidth + "x" + screenHeight + " @ " + screenDensity + " dpi");
    }
    
    public Intent createScreenCaptureIntent() {
        if (projectionManager != null) {
            return projectionManager.createScreenCaptureIntent();
        }
        return null;
    }
    
    public void setMediaProjection(int resultCode, Intent data) {
        if (projectionManager != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            Log.d(TAG, "MediaProjection obtained");
        }
    }
    
    public boolean toggleRecording() {
        if (isRecording) {
            stopRecording();
            return false;
        } else {
            startRecording();
            return true;
        }
    }
    
    public void startRecording() {
        if (isRecording) {
            Log.w(TAG, "Recording already in progress");
            return;
        }
        
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection not available");
            if (callback != null) {
                callback.onRecordingError("Permissão de captura de tela não concedida");
            }
            return;
        }
        
        try {
            setupMediaRecorder();
            setupVirtualDisplay();
            
            mediaRecorder.start();
            isRecording = true;
            
            Log.d(TAG, "Screen recording started: " + outputFilePath);
            if (callback != null) {
                callback.onRecordingStarted(outputFilePath);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting screen recording", e);
            if (callback != null) {
                callback.onRecordingError("Erro ao iniciar gravação: " + e.getMessage());
            }
            cleanup();
        }
    }
    
    public void stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "No recording in progress");
            return;
        }
        
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            
            isRecording = false;
            
            Log.d(TAG, "Screen recording stopped: " + outputFilePath);
            if (callback != null) {
                callback.onRecordingStopped(outputFilePath);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error stopping screen recording", e);
            if (callback != null) {
                callback.onRecordingError("Erro ao parar gravação: " + e.getMessage());
            }
        } finally {
            cleanup();
        }
    }
    
    private void setupMediaRecorder() throws IOException {
        mediaRecorder = new MediaRecorder();
        
        // Configurar fonte de áudio e vídeo
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        
        // Configurar formato de saída
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        
        // Configurar codecs
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        
        // Configurar qualidade de vídeo
        mediaRecorder.setVideoSize(screenWidth, screenHeight);
        mediaRecorder.setVideoFrameRate(videoFrameRate);
        mediaRecorder.setVideoEncodingBitRate(videoBitRate);
        
        // Configurar qualidade de áudio
        mediaRecorder.setAudioSamplingRate(audioSampleRate);
        mediaRecorder.setAudioEncodingBitRate(audioBitRate);
        
        // Configurar arquivo de saída
        outputFilePath = generateOutputFilePath();
        mediaRecorder.setOutputFile(outputFilePath);
        
        // Preparar o recorder
        mediaRecorder.prepare();
        
        Log.d(TAG, "MediaRecorder configured for " + screenWidth + "x" + screenHeight + " @ " + videoFrameRate + "fps");
    }
    
    private void setupVirtualDisplay() {
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenRecorder",
                screenWidth,
                screenHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder.getSurface(),
                null,
                null
        );
        
        Log.d(TAG, "VirtualDisplay created");
    }
    
    private String generateOutputFilePath() {
        // Criar diretório de gravações se não existir
        File recordingsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "FreeFireAimbot");
        if (!recordingsDir.exists()) {
            recordingsDir.mkdirs();
        }
        
        // Gerar nome do arquivo com timestamp
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String timestamp = formatter.format(new Date());
        String fileName = "FreeFire_" + timestamp + ".mp4";
        
        return new File(recordingsDir, fileName).getAbsolutePath();
    }
    
    private void cleanup() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
        }
        
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaRecorder", e);
            }
            mediaRecorder = null;
        }
    }
    
    public boolean isRecording() {
        return isRecording;
    }
    
    public String getCurrentRecordingPath() {
        return isRecording ? outputFilePath : null;
    }
    
    // Configurações de qualidade
    public void setVideoQuality(VideoQuality quality) {
        switch (quality) {
            case LOW:
                videoBitRate = 2000000; // 2 Mbps
                videoFrameRate = 24;
                break;
            case MEDIUM:
                videoBitRate = 5000000; // 5 Mbps
                videoFrameRate = 30;
                break;
            case HIGH:
                videoBitRate = 10000000; // 10 Mbps
                videoFrameRate = 60;
                break;
            case ULTRA:
                videoBitRate = 20000000; // 20 Mbps
                videoFrameRate = 60;
                break;
        }
        Log.d(TAG, "Video quality set to " + quality + ": " + videoBitRate + " bps @ " + videoFrameRate + " fps");
    }
    
    public void setAudioEnabled(boolean enabled) {
        // Esta configuração será aplicada na próxima gravação
        if (enabled) {
            audioBitRate = 128000;
        } else {
            audioBitRate = 0; // Desabilitar áudio
        }
    }
    
    public enum VideoQuality {
        LOW, MEDIUM, HIGH, ULTRA
    }
    
    public void destroy() {
        if (isRecording) {
            stopRecording();
        }
        
        cleanup();
        
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        
        context = null;
        callback = null;
        
        Log.d(TAG, "ScreenRecorder destroyed");
    }
    
    // Métodos utilitários
    public long getRecordingDuration() {
        if (!isRecording || outputFilePath == null) {
            return 0;
        }
        
        File recordingFile = new File(outputFilePath);
        if (recordingFile.exists()) {
            return recordingFile.length();
        }
        
        return 0;
    }
    
    public File[] getRecordingFiles() {
        File recordingsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "FreeFireAimbot");
        if (recordingsDir.exists() && recordingsDir.isDirectory()) {
            return recordingsDir.listFiles((dir, name) -> name.endsWith(".mp4"));
        }
        return new File[0];
    }
    
    public boolean deleteRecording(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, "Recording deleted: " + filePath + " (success: " + deleted + ")");
            return deleted;
        }
        return false;
    }
}

