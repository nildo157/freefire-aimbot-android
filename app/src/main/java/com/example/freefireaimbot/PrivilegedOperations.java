package com.example.freefireaimbot;

import android.content.Context;
import android.util.Log;

public class PrivilegedOperations {
    private static final String TAG = "PrivilegedOperations";
    private static final String FREE_FIRE_PACKAGE = "com.dts.freefireth";
    
    private static PrivilegedOperations instance;
    private Context context;
    private ShizukuHelper shizukuHelper;
    private boolean isInitialized = false;
    
    public interface OperationCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    private PrivilegedOperations() {}
    
    public static synchronized PrivilegedOperations getInstance() {
        if (instance == null) {
            instance = new PrivilegedOperations();
        }
        return instance;
    }
    
    public void initialize(Context context) {
        this.context = context;
        this.shizukuHelper = ShizukuHelper.getInstance();
        
        shizukuHelper.initialize(context, new ShizukuHelper.ShizukuCallback() {
            @Override
            public void onShizukuReady() {
                Log.d(TAG, "Shizuku ready for privileged operations");
                isInitialized = true;
            }

            @Override
            public void onShizukuPermissionGranted() {
                Log.d(TAG, "Shizuku permission granted for privileged operations");
                isInitialized = true;
            }

            @Override
            public void onShizukuPermissionDenied() {
                Log.w(TAG, "Shizuku permission denied");
                isInitialized = false;
            }

            @Override
            public void onShizukuError(String error) {
                Log.e(TAG, "Shizuku error: " + error);
                isInitialized = false;
            }
        });
    }
    
    public boolean isReady() {
        return isInitialized && shizukuHelper.hasPermission();
    }
    
    public void requestPermissions(OperationCallback callback) {
        if (!shizukuHelper.isAvailable()) {
            if (callback != null) {
                callback.onError("Shizuku não está disponível");
            }
            return;
        }
        
        shizukuHelper.requestPermission();
    }
    
    // Operações específicas para o aimbot
    public void enableSystemOverlay(OperationCallback callback) {
        if (!isReady()) {
            if (callback != null) {
                callback.onError("Permissões Shizuku não disponíveis");
            }
            return;
        }
        
        String packageName = context.getPackageName();
        String command = "appops set " + packageName + " SYSTEM_ALERT_WINDOW allow";
        
        shizukuHelper.executeCommand(command, new ShizukuHelper.CommandCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "System overlay enabled successfully");
                if (callback != null) {
                    callback.onSuccess("Overlay do sistema habilitado");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to enable system overlay: " + error);
                if (callback != null) {
                    callback.onError("Falha ao habilitar overlay: " + error);
                }
            }
        });
    }
    
    public void enableScreenCapture(OperationCallback callback) {
        if (!isReady()) {
            if (callback != null) {
                callback.onError("Permissões Shizuku não disponíveis");
            }
            return;
        }
        
        String packageName = context.getPackageName();
        String command = "appops set " + packageName + " PROJECT_MEDIA allow";
        
        shizukuHelper.executeCommand(command, new ShizukuHelper.CommandCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Screen capture enabled successfully");
                if (callback != null) {
                    callback.onSuccess("Captura de tela habilitada");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to enable screen capture: " + error);
                if (callback != null) {
                    callback.onError("Falha ao habilitar captura: " + error);
                }
            }
        });
    }
    
    public void optimizeGamePerformance(OperationCallback callback) {
        if (!isReady()) {
            if (callback != null) {
                callback.onError("Permissões Shizuku não disponíveis");
            }
            return;
        }
        
        // Comandos para otimizar performance do Free Fire
        String[] commands = {
            "cmd package compile -m speed " + FREE_FIRE_PACKAGE,
            "cmd thermalservice override-status 0",
            "settings put global animator_duration_scale 0.5",
            "settings put global transition_animation_scale 0.5",
            "settings put global window_animation_scale 0.5"
        };
        
        executeMultipleCommands(commands, 0, callback);
    }
    
    private void executeMultipleCommands(String[] commands, int index, OperationCallback callback) {
        if (index >= commands.length) {
            if (callback != null) {
                callback.onSuccess("Otimização de performance concluída");
            }
            return;
        }
        
        shizukuHelper.executeCommand(commands[index], new ShizukuHelper.CommandCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Command " + (index + 1) + " executed successfully");
                executeMultipleCommands(commands, index + 1, callback);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Command " + (index + 1) + " failed: " + error);
                // Continuar com o próximo comando mesmo se um falhar
                executeMultipleCommands(commands, index + 1, callback);
            }
        });
    }
    
    public void enableDeveloperOptions(OperationCallback callback) {
        if (!isReady()) {
            if (callback != null) {
                callback.onError("Permissões Shizuku não disponíveis");
            }
            return;
        }
        
        String command = "settings put global development_settings_enabled 1";
        
        shizukuHelper.executeCommand(command, new ShizukuHelper.CommandCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Developer options enabled");
                if (callback != null) {
                    callback.onSuccess("Opções de desenvolvedor habilitadas");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to enable developer options: " + error);
                if (callback != null) {
                    callback.onError("Falha ao habilitar opções: " + error);
                }
            }
        });
    }
    
    public void setHighPerformanceMode(OperationCallback callback) {
        if (!isReady()) {
            if (callback != null) {
                callback.onError("Permissões Shizuku não disponíveis");
            }
            return;
        }
        
        String[] commands = {
            "settings put global low_power 0",
            "settings put system screen_brightness_mode 0",
            "settings put system screen_brightness 255",
            "cmd power set-adaptive-power-saver-enabled false"
        };
        
        executeMultipleCommands(commands, 0, callback);
    }
    
    public void disableGameOptimizations(OperationCallback callback) {
        if (!isReady()) {
            if (callback != null) {
                callback.onError("Permissões Shizuku não disponíveis");
            }
            return;
        }
        
        String command = "cmd package bg-dexopt-job " + FREE_FIRE_PACKAGE;
        
        shizukuHelper.executeCommand(command, new ShizukuHelper.CommandCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Game optimizations disabled");
                if (callback != null) {
                    callback.onSuccess("Otimizações do jogo desabilitadas");
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to disable game optimizations: " + error);
                if (callback != null) {
                    callback.onError("Falha ao desabilitar otimizações: " + error);
                }
            }
        });
    }
    
    public void grantAllPermissions(OperationCallback callback) {
        if (!isReady()) {
            if (callback != null) {
                callback.onError("Permissões Shizuku não disponíveis");
            }
            return;
        }
        
        String packageName = context.getPackageName();
        String[] permissions = {
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"
        };
        
        grantPermissions(packageName, permissions, 0, callback);
    }
    
    private void grantPermissions(String packageName, String[] permissions, int index, OperationCallback callback) {
        if (index >= permissions.length) {
            if (callback != null) {
                callback.onSuccess("Todas as permissões concedidas");
            }
            return;
        }
        
        shizukuHelper.grantPermission(packageName, permissions[index], new ShizukuHelper.CommandCallback() {
            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "Permission granted: " + permissions[index]);
                grantPermissions(packageName, permissions, index + 1, callback);
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Failed to grant permission " + permissions[index] + ": " + error);
                // Continuar com a próxima permissão
                grantPermissions(packageName, permissions, index + 1, callback);
            }
        });
    }
    
    public void cleanup() {
        if (shizukuHelper != null) {
            shizukuHelper.cleanup();
        }
        isInitialized = false;
        context = null;
    }
}

