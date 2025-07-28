package com.example.freefireaimbot;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class ShizukuHelper {
    private static final String TAG = "ShizukuHelper";
    private static final int SHIZUKU_REQUEST_CODE = 1002;
    
    private static ShizukuHelper instance;
    private Context context;
    private boolean isShizukuAvailable = false;
    private boolean isPermissionGranted = false;
    private ShizukuCallback callback;
    
    public interface ShizukuCallback {
        void onShizukuReady();
        void onShizukuPermissionGranted();
        void onShizukuPermissionDenied();
        void onShizukuError(String error);
    }
    
    private ShizukuHelper() {}
    
    public static synchronized ShizukuHelper getInstance() {
        if (instance == null) {
            instance = new ShizukuHelper();
        }
        return instance;
    }
    
    public void initialize(Context context, ShizukuCallback callback) {
        this.context = context;
        this.callback = callback;
        
        // Verificar se Shizuku está disponível
        checkShizukuAvailability();
        
        // Registrar listener para mudanças de permissão
        Shizuku.addRequestPermissionResultListener(requestPermissionResultListener);
        Shizuku.addBinderReceivedListener(binderReceivedListener);
        Shizuku.addBinderDeadListener(binderDeadListener);
    }
    
    private void checkShizukuAvailability() {
        try {
            // Verificar se o Shizuku está instalado
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("moe.shizuku.privileged.api", 0);
            
            // Verificar se o serviço Shizuku está rodando
            if (Shizuku.pingBinder()) {
                isShizukuAvailable = true;
                Log.d(TAG, "Shizuku is available and running");
                
                // Verificar permissões
                checkPermissions();
                
                if (callback != null) {
                    callback.onShizukuReady();
                }
            } else {
                Log.w(TAG, "Shizuku is installed but not running");
                if (callback != null) {
                    callback.onShizukuError("Shizuku não está rodando. Inicie o serviço Shizuku.");
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "Shizuku is not installed");
            if (callback != null) {
                callback.onShizukuError("Shizuku não está instalado. Instale o app Shizuku.");
            }
        }
    }
    
    private void checkPermissions() {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = true;
            Log.d(TAG, "Shizuku permission already granted");
            if (callback != null) {
                callback.onShizukuPermissionGranted();
            }
        } else {
            Log.d(TAG, "Shizuku permission not granted");
        }
    }
    
    public void requestPermission() {
        if (!isShizukuAvailable) {
            if (callback != null) {
                callback.onShizukuError("Shizuku não está disponível");
            }
            return;
        }
        
        if (isPermissionGranted) {
            if (callback != null) {
                callback.onShizukuPermissionGranted();
            }
            return;
        }
        
        if (Shizuku.shouldShowRequestPermissionRationale()) {
            Log.d(TAG, "Should show permission rationale");
        }
        
        Shizuku.requestPermission(SHIZUKU_REQUEST_CODE);
    }
    
    public boolean hasPermission() {
        return isPermissionGranted && isShizukuAvailable;
    }
    
    public boolean isAvailable() {
        return isShizukuAvailable;
    }
    
    // Executar comando com privilégios elevados
    public void executeCommand(String command, CommandCallback commandCallback) {
        if (!hasPermission()) {
            if (commandCallback != null) {
                commandCallback.onError("Permissão Shizuku não concedida");
            }
            return;
        }
        
        try {
            // Usar Shizuku para executar comandos com privilégios de sistema
            Process process = Shizuku.newProcess(new String[]{"sh", "-c", command}, null, null);
            
            // Ler resultado em thread separada
            new Thread(() -> {
                try {
                    process.waitFor();
                    int exitCode = process.exitValue();
                    
                    if (commandCallback != null) {
                        if (exitCode == 0) {
                            commandCallback.onSuccess("Comando executado com sucesso");
                        } else {
                            commandCallback.onError("Comando falhou com código: " + exitCode);
                        }
                    }
                } catch (InterruptedException e) {
                    if (commandCallback != null) {
                        commandCallback.onError("Comando interrompido: " + e.getMessage());
                    }
                }
            }).start();
            
        } catch (Exception e) {
            Log.e(TAG, "Error executing command with Shizuku", e);
            if (commandCallback != null) {
                commandCallback.onError("Erro ao executar comando: " + e.getMessage());
            }
        }
    }
    
    public interface CommandCallback {
        void onSuccess(String result);
        void onError(String error);
    }
    
    // Obter acesso a serviços do sistema com privilégios elevados
    public IBinder getSystemService(String serviceName) {
        if (!hasPermission()) {
            Log.w(TAG, "No Shizuku permission for system service access");
            return null;
        }
        
        try {
            return SystemServiceHelper.getSystemService(serviceName);
        } catch (Exception e) {
            Log.e(TAG, "Error getting system service: " + serviceName, e);
            return null;
        }
    }
    
    // Listeners para eventos do Shizuku
    private final Shizuku.OnRequestPermissionResultListener requestPermissionResultListener = 
            new Shizuku.OnRequestPermissionResultListener() {
        @Override
        public void onRequestPermissionResult(int requestCode, int grantResult) {
            if (requestCode == SHIZUKU_REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    isPermissionGranted = true;
                    Log.d(TAG, "Shizuku permission granted");
                    if (callback != null) {
                        callback.onShizukuPermissionGranted();
                    }
                } else {
                    isPermissionGranted = false;
                    Log.d(TAG, "Shizuku permission denied");
                    if (callback != null) {
                        callback.onShizukuPermissionDenied();
                    }
                }
            }
        }
    };
    
    private final Shizuku.OnBinderReceivedListener binderReceivedListener = 
            new Shizuku.OnBinderReceivedListener() {
        @Override
        public void onBinderReceived() {
            Log.d(TAG, "Shizuku binder received");
            checkPermissions();
        }
    };
    
    private final Shizuku.OnBinderDeadListener binderDeadListener = 
            new Shizuku.OnBinderDeadListener() {
        @Override
        public void onBinderDead() {
            Log.w(TAG, "Shizuku binder died");
            isShizukuAvailable = false;
            isPermissionGranted = false;
            if (callback != null) {
                callback.onShizukuError("Conexão com Shizuku perdida");
            }
        }
    };
    
    public void cleanup() {
        Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener);
        Shizuku.removeBinderReceivedListener(binderReceivedListener);
        Shizuku.removeBinderDeadListener(binderDeadListener);
        
        isShizukuAvailable = false;
        isPermissionGranted = false;
        context = null;
        callback = null;
        
        Log.d(TAG, "ShizukuHelper cleaned up");
    }
    
    // Métodos utilitários para operações específicas
    public void enableAccessibilityService(String packageName, CommandCallback callback) {
        String command = "settings put secure enabled_accessibility_services " + packageName;
        executeCommand(command, callback);
    }
    
    public void grantPermission(String packageName, String permission, CommandCallback callback) {
        String command = "pm grant " + packageName + " " + permission;
        executeCommand(command, callback);
    }
    
    public void setSystemProperty(String key, String value, CommandCallback callback) {
        String command = "setprop " + key + " " + value;
        executeCommand(command, callback);
    }
    
    public void killProcess(String packageName, CommandCallback callback) {
        String command = "am force-stop " + packageName;
        executeCommand(command, callback);
    }
}

