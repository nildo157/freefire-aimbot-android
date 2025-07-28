package com.example.freefireaimbot;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class OverlayService extends Service implements AimbotManager.AimbotCallback {
    private WindowManager windowManager;
    private View overlayView;
    private TargetOverlay targetOverlay;
    private boolean isOverlayVisible = false;
    private TextView statusText;
    private TextView fpsCounter;
    
    private AimbotManager aimbotManager;
    private long lastFpsUpdate = 0;
    private int frameCount = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // Inicializar AimbotManager
        aimbotManager = AimbotManager.getInstance();
        aimbotManager.initialize(this, this);
        
        // Criar overlays
        createOverlayView();
        createTargetOverlay();
    }

    private void createOverlayView() {
        // Criar o layout do overlay
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);
        
        // Configurar parâmetros da janela
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 100;
        params.y = 100;

        // Configurar botões do overlay
        setupOverlayButtons();
    }
    
    private void createTargetOverlay() {
        targetOverlay = new TargetOverlay(this);
    }

    private void setupOverlayButtons() {
        Button toggleButton = overlayView.findViewById(R.id.toggle_aimbot);
        Button settingsButton = overlayView.findViewById(R.id.settings_button);
        Button recordButton = overlayView.findViewById(R.id.record_button);
        statusText = overlayView.findViewById(R.id.status_text);
        fpsCounter = overlayView.findViewById(R.id.fps_counter);

        toggleButton.setOnClickListener(v -> {
            // Toggle aimbot functionality
            boolean isActive = aimbotManager.toggleAimbot();
            updateStatus(isActive);
            
            if (isActive) {
                targetOverlay.show();
                // Verificar se o modelo está disponível
                if (!ModelDownloader.isModelDownloaded(this)) {
                    downloadModel();
                }
            } else {
                targetOverlay.hide();
            }
        });

        settingsButton.setOnClickListener(v -> {
            // Abrir configurações
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        recordButton.setOnClickListener(v -> {
            // Toggle gravação de tela
            ScreenRecorder.getInstance().toggleRecording();
        });
    }
    
    private void updateStatus(boolean isActive) {
        if (statusText != null) {
            statusText.setText(isActive ? "Aimbot: ATIVO" : "Aimbot: INATIVO");
        }
    }
    
    private void downloadModel() {
        Toast.makeText(this, "Baixando modelo de IA...", Toast.LENGTH_SHORT).show();
        
        ModelDownloader.downloadModel(this, new ModelDownloader.DownloadCallback() {
            @Override
            public void onDownloadStart() {
                if (statusText != null) {
                    statusText.setText("Baixando modelo...");
                }
            }

            @Override
            public void onDownloadProgress(int progress) {
                if (statusText != null) {
                    statusText.setText("Download: " + progress + "%");
                }
            }

            @Override
            public void onDownloadComplete(String modelPath) {
                if (statusText != null) {
                    statusText.setText("Modelo carregado!");
                }
                Toast.makeText(OverlayService.this, "Modelo baixado com sucesso!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDownloadError(String error) {
                if (statusText != null) {
                    statusText.setText("Erro no download");
                }
                Toast.makeText(OverlayService.this, "Erro: " + error, Toast.LENGTH_LONG).show();
                
                // Criar modelo dummy para testes
                ModelDownloader.createDummyModel(OverlayService.this);
            }
        });
    }

    public void showOverlay() {
        if (!isOverlayVisible && overlayView != null) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayView.getLayoutParams();
            windowManager.addView(overlayView, params);
            isOverlayVisible = true;
        }
    }

    public void hideOverlay() {
        if (isOverlayVisible && overlayView != null) {
            windowManager.removeView(overlayView);
            isOverlayVisible = false;
        }
        
        if (targetOverlay != null && targetOverlay.isShowing()) {
            targetOverlay.hide();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showOverlay();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideOverlay();
        
        if (aimbotManager != null) {
            aimbotManager.cleanup();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    // Implementação do AimbotCallback
    @Override
    public void onTargetDetected(List<AimbotManager.DetectedObject> targets) {
        // Atualizar overlay de alvos
        if (targetOverlay != null) {
            targetOverlay.updateTargets(targets);
        }
        
        // Atualizar contador de FPS
        updateFpsCounter();
    }
    
    @Override
    public void onError(String error) {
        if (statusText != null) {
            statusText.setText("Erro: " + error);
        }
        Toast.makeText(this, "Erro no aimbot: " + error, Toast.LENGTH_SHORT).show();
    }
    
    private void updateFpsCounter() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastFpsUpdate >= 1000) { // Atualizar a cada segundo
            int fps = frameCount;
            frameCount = 0;
            lastFpsUpdate = currentTime;
            
            if (fpsCounter != null) {
                fpsCounter.setText("FPS: " + fps);
            }
        }
    }
}

