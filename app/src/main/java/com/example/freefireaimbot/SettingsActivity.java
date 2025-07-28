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
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity implements 
        ShizukuHelper.ShizukuCallback, 
        PrivilegedOperations.OperationCallback {
    
    private static final int SCREEN_CAPTURE_REQUEST_CODE = 1004;
    
    // UI Components
    private Switch aimbotEnabledSwitch;
    private SeekBar sensitivitySeekBar;
    private SeekBar confidenceSeekBar;
    private CheckBox showTargetsCheckBox;
    private CheckBox showCrosshairCheckBox;
    private Spinner videoQualitySpinner;
    private CheckBox audioEnabledCheckBox;
    private Button requestShizukuButton;
    private Button requestScreenCaptureButton;
    private Button optimizePerformanceButton;
    private Button grantPermissionsButton;
    private TextView statusText;
    
    // Helpers
    private ShizukuHelper shizukuHelper;
    private PrivilegedOperations privilegedOps;
    private ScreenRecorder screenRecorder;
    private SharedPreferences preferences;
    
    // Recording receiver
    private BroadcastReceiver recordingReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initializeComponents();
        setupListeners();
        loadSettings();
        registerRecordingReceiver();
    }
    
    private void initializeComponents() {
        // Initialize helpers
        shizukuHelper = ShizukuHelper.getInstance();
        privilegedOps = PrivilegedOperations.getInstance();
        screenRecorder = ScreenRecorder.getInstance();
        preferences = getSharedPreferences("aimbot_settings", MODE_PRIVATE);
        
        // Initialize UI components
        aimbotEnabledSwitch = findViewById(R.id.aimbot_enabled_switch);
        sensitivitySeekBar = findViewById(R.id.sensitivity_seekbar);
        confidenceSeekBar = findViewById(R.id.confidence_seekbar);
        showTargetsCheckBox = findViewById(R.id.show_targets_checkbox);
        showCrosshairCheckBox = findViewById(R.id.show_crosshair_checkbox);
        videoQualitySpinner = findViewById(R.id.video_quality_spinner);
        audioEnabledCheckBox = findViewById(R.id.audio_enabled_checkbox);
        requestShizukuButton = findViewById(R.id.request_shizuku_button);
        requestScreenCaptureButton = findViewById(R.id.request_screen_capture_button);
        optimizePerformanceButton = findViewById(R.id.optimize_performance_button);
        grantPermissionsButton = findViewById(R.id.grant_permissions_button);
        statusText = findViewById(R.id.status_text);
        
        // Initialize helpers with callbacks
        shizukuHelper.initialize(this, this);
        privilegedOps.initialize(this);
        screenRecorder.initialize(this, null);
    }
    
    private void setupListeners() {
        aimbotEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("aimbot_enabled", isChecked);
            updateStatus("Aimbot " + (isChecked ? "habilitado" : "desabilitado"));
        });
        
        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    savePreference("sensitivity", progress);
                    TextView label = findViewById(R.id.sensitivity_label);
                    label.setText("Sensibilidade: " + progress + "%");
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        confidenceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    savePreference("confidence", progress);
                    TextView label = findViewById(R.id.confidence_label);
                    label.setText("Confiança: " + progress + "%");
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        showTargetsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("show_targets", isChecked);
        });
        
        showCrosshairCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("show_crosshair", isChecked);
        });
        
        audioEnabledCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("audio_enabled", isChecked);
            screenRecorder.setAudioEnabled(isChecked);
        });
        
        requestShizukuButton.setOnClickListener(v -> {
            updateStatus("Solicitando permissões Shizuku...");
            privilegedOps.requestPermissions(this);
        });
        
        requestScreenCaptureButton.setOnClickListener(v -> {
            requestScreenCapturePermission();
        });
        
        optimizePerformanceButton.setOnClickListener(v -> {
            updateStatus("Otimizando performance...");
            privilegedOps.optimizeGamePerformance(this);
        });
        
        grantPermissionsButton.setOnClickListener(v -> {
            updateStatus("Concedendo permissões...");
            privilegedOps.grantAllPermissions(this);
        });
    }
    
    private void loadSettings() {
        aimbotEnabledSwitch.setChecked(preferences.getBoolean("aimbot_enabled", false));
        
        int sensitivity = preferences.getInt("sensitivity", 50);
        sensitivitySeekBar.setProgress(sensitivity);
        TextView sensitivityLabel = findViewById(R.id.sensitivity_label);
        sensitivityLabel.setText("Sensibilidade: " + sensitivity + "%");
        
        int confidence = preferences.getInt("confidence", 70);
        confidenceSeekBar.setProgress(confidence);
        TextView confidenceLabel = findViewById(R.id.confidence_label);
        confidenceLabel.setText("Confiança: " + confidence + "%");
        
        showTargetsCheckBox.setChecked(preferences.getBoolean("show_targets", true));
        showCrosshairCheckBox.setChecked(preferences.getBoolean("show_crosshair", true));
        audioEnabledCheckBox.setChecked(preferences.getBoolean("audio_enabled", true));
        
        // Update status based on current state
        updatePermissionStatus();
    }
    
    private void savePreference(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }
    
    private void savePreference(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }
    
    private void updateStatus(String message) {
        statusText.setText(message);
    }
    
    private void updatePermissionStatus() {
        StringBuilder status = new StringBuilder();
        
        if (OverlayPermissionHelper.hasOverlayPermission(this)) {
            status.append("✓ Overlay ");
        } else {
            status.append("✗ Overlay ");
        }
        
        if (shizukuHelper.hasPermission()) {
            status.append("✓ Shizuku ");
        } else {
            status.append("✗ Shizuku ");
        }
        
        updateStatus(status.toString());
    }
    
    private void requestScreenCapturePermission() {
        MediaProjectionManager projectionManager = 
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        
        if (projectionManager != null) {
            Intent captureIntent = projectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, SCREEN_CAPTURE_REQUEST_CODE);
        }
    }
    
    private void registerRecordingReceiver() {
        recordingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("com.example.freefireaimbot.RECORDING_STARTED".equals(action)) {
                    updateStatus("Gravação iniciada");
                } else if ("com.example.freefireaimbot.RECORDING_STOPPED".equals(action)) {
                    String filePath = intent.getStringExtra("filePath");
                    updateStatus("Gravação salva: " + filePath);
                } else if ("com.example.freefireaimbot.RECORDING_ERROR".equals(action)) {
                    String error = intent.getStringExtra("error");
                    updateStatus("Erro na gravação: " + error);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                screenRecorder.setMediaProjection(resultCode, data);
                updateStatus("Permissão de captura de tela concedida");
                Toast.makeText(this, "Agora você pode gravar a tela!", Toast.LENGTH_SHORT).show();
            } else {
                updateStatus("Permissão de captura de tela negada");
                Toast.makeText(this, "Permissão necessária para gravação", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    // ShizukuHelper.ShizukuCallback implementation
    @Override
    public void onShizukuReady() {
        runOnUiThread(() -> {
            updateStatus("Shizuku pronto");
            updatePermissionStatus();
        });
    }
    
    @Override
    public void onShizukuPermissionGranted() {
        runOnUiThread(() -> {
            updateStatus("Permissão Shizuku concedida");
            updatePermissionStatus();
            Toast.makeText(this, "Shizuku habilitado!", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onShizukuPermissionDenied() {
        runOnUiThread(() -> {
            updateStatus("Permissão Shizuku negada");
            updatePermissionStatus();
            Toast.makeText(this, "Permissão Shizuku necessária", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onShizukuError(String error) {
        runOnUiThread(() -> {
            updateStatus("Erro Shizuku: " + error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
    
    // PrivilegedOperations.OperationCallback implementation
    @Override
    public void onSuccess(String message) {
        runOnUiThread(() -> {
            updateStatus(message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            updateStatus("Erro: " + error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (recordingReceiver != null) {
            unregisterReceiver(recordingReceiver);
        }
    }
}

