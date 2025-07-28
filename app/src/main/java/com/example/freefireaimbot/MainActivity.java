package com.example.freefireaimbot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 1005;
    
    private Button startOverlayButton;
    private Button stopOverlayButton;
    private Button settingsButton;
    private Button recordButton;
    private TextView statusText;
    private TextView recordingStatusText;
    
    private SharedPreferences preferences;
    private BroadcastReceiver recordingReceiver;
    private boolean isRecording = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        preferences = getSharedPreferences("aimbot_settings", MODE_PRIVATE);
        
        initializeViews();
        setupClickListeners();
        checkOverlayPermission();
        registerRecordingReceiver();
        updateUI();
    }
    
    private void initializeViews() {
        startOverlayButton = findViewById(R.id.start_overlay_button);
        stopOverlayButton = findViewById(R.id.stop_overlay_button);
        settingsButton = findViewById(R.id.settings_button);
        recordButton = findViewById(R.id.record_button);
        statusText = findViewById(R.id.status_text);
        recordingStatusText = findViewById(R.id.recording_status_text);
    }
    
    private void setupClickListeners() {
        startOverlayButton.setOnClickListener(v -> {
            if (OverlayPermissionHelper.hasOverlayPermission(this)) {
                OverlayPermissionHelper.startOverlayService(this);
                Toast.makeText(this, "Overlay iniciado", Toast.LENGTH_SHORT).show();
                updateUI();
            } else {
                OverlayPermissionHelper.requestOverlayPermission(this);
            }
        });
        
        stopOverlayButton.setOnClickListener(v -> {
            OverlayPermissionHelper.stopOverlayService(this);
            Toast.makeText(this, "Overlay parado", Toast.LENGTH_SHORT).show();
            updateUI();
        });
        
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });
        
        recordButton.setOnClickListener(v -> {
            if (isRecording) {
                ScreenRecordingService.stopRecording(this);
            } else {
                requestScreenCapture();
            }
        });
    }
    
    private void requestScreenCapture() {
        MediaProjectionManager projectionManager = 
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        if (projectionManager != null) {
            Intent captureIntent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, SCREEN_CAPTURE_REQUEST_CODE);
        }
    }
    
    private void checkOverlayPermission() {
        if (!OverlayPermissionHelper.hasOverlayPermission(this)) {
            Toast.makeText(this, "Permissão de overlay necessária", Toast.LENGTH_LONG).show();
        }
    }
    
    private void updateUI() {
        // Update status based on current settings
        boolean aimbotEnabled = preferences.getBoolean("aimbot_enabled", false);
        int sensitivity = preferences.getInt("sensitivity", 50);
        int confidence = preferences.getInt("confidence", 70);
        
        StringBuilder status = new StringBuilder();
        status.append("Aimbot: ").append(aimbotEnabled ? "HABILITADO" : "DESABILITADO").append("\n");
        status.append("Sensibilidade: ").append(sensitivity).append("%\n");
        status.append("Confiança: ").append(confidence).append("%");
        
        statusText.setText(status.toString());
        
        // Update recording button
        recordButton.setText(isRecording ? "Parar Gravação" : "Iniciar Gravação");
    }
    
    private void registerRecordingReceiver() {
        recordingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("com.example.freefireaimbot.RECORDING_STARTED".equals(action)) {
                    isRecording = true;
                    String filePath = intent.getStringExtra("filePath");
                    recordingStatusText.setText("Gravando: " + filePath);
                    recordingStatusText.setVisibility(View.VISIBLE);
                    updateUI();
                } else if ("com.example.freefireaimbot.RECORDING_STOPPED".equals(action)) {
                    isRecording = false;
                    String filePath = intent.getStringExtra("filePath");
                    recordingStatusText.setText("Gravação salva: " + filePath);
                    updateUI();
                    
                    // Hide recording status after 3 seconds
                    new android.os.Handler().postDelayed(() -> {
                        recordingStatusText.setVisibility(View.GONE);
                    }, 3000);
                } else if ("com.example.freefireaimbot.RECORDING_ERROR".equals(action)) {
                    isRecording = false;
                    String error = intent.getStringExtra("error");
                    recordingStatusText.setText("Erro: " + error);
                    recordingStatusText.setVisibility(View.VISIBLE);
                    updateUI();
                }
            }
        };
        
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.freefireaimbot.RECORDING_STARTED");
        filter.addAction("com.example.freefireaimbot.RECORDING_STOPPED");
        filter.addAction("com.example.freefireaimbot.RECORDING_ERROR");
        registerReceiver(recordingReceiver, filter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == OverlayPermissionHelper.OVERLAY_PERMISSION_REQUEST_CODE) {
            if (OverlayPermissionHelper.hasOverlayPermission(this)) {
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                ScreenRecordingService.startRecording(this, resultCode, data);
                Toast.makeText(this, "Iniciando gravação...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão de captura negada", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (recordingReceiver != null) {
            unregisterReceiver(recordingReceiver);
        }
    }
}

