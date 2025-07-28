package com.example.freefireaimbot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ScreenRecordingService extends Service implements ScreenRecorder.RecordingCallback {
    private static final String TAG = "ScreenRecordingService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "screen_recording_channel";
    
    private ScreenRecorder screenRecorder;
    private NotificationManager notificationManager;
    private boolean isServiceRunning = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        screenRecorder = ScreenRecorder.getInstance();
        screenRecorder.initialize(this, this);
        
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        
        Log.d(TAG, "ScreenRecordingService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            
            if ("START_RECORDING".equals(action)) {
                startRecordingWithProjection(intent);
            } else if ("STOP_RECORDING".equals(action)) {
                stopRecording();
            } else if ("TOGGLE_RECORDING".equals(action)) {
                toggleRecording();
            }
        }
        
        return START_STICKY;
    }
    
    private void startRecordingWithProjection(Intent intent) {
        if (intent.hasExtra("resultCode") && intent.hasExtra("data")) {
            int resultCode = intent.getIntExtra("resultCode", -1);
            Intent data = intent.getParcelableExtra("data");
            
            screenRecorder.setMediaProjection(resultCode, data);
            screenRecorder.startRecording();
        } else {
            Log.e(TAG, "Missing MediaProjection data");
        }
    }
    
    private void stopRecording() {
        screenRecorder.stopRecording();
        stopForeground(true);
        isServiceRunning = false;
    }
    
    private void toggleRecording() {
        if (screenRecorder.isRecording()) {
            stopRecording();
        } else {
            // Para toggle, precisamos que a MediaProjection já esteja configurada
            if (screenRecorder != null) {
                screenRecorder.startRecording();
            }
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Gravação de Tela",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Notificações para gravação de tela");
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification createRecordingNotification(String status, String filePath) {
        Intent stopIntent = new Intent(this, ScreenRecordingService.class);
        stopIntent.setAction("STOP_RECORDING");
        PendingIntent stopPendingIntent = PendingIntent.getService(
                this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Free Fire Aimbot")
                .setContentText(status)
                .setContentIntent(mainPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        
        if (screenRecorder.isRecording()) {
            builder.addAction(android.R.drawable.ic_media_pause, "Parar", stopPendingIntent);
            builder.setSmallIcon(android.R.drawable.ic_media_play);
        }
        
        if (filePath != null) {
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(status + "\nArquivo: " + filePath));
        }
        
        return builder.build();
    }
    
    @Override
    public void onRecordingStarted(String filePath) {
        Log.d(TAG, "Recording started: " + filePath);
        
        isServiceRunning = true;
        Notification notification = createRecordingNotification("Gravando tela...", filePath);
        startForeground(NOTIFICATION_ID, notification);
        
        // Notificar outros componentes
        Intent broadcast = new Intent("com.example.freefireaimbot.RECORDING_STARTED");
        broadcast.putExtra("filePath", filePath);
        sendBroadcast(broadcast);
    }
    
    @Override
    public void onRecordingStopped(String filePath) {
        Log.d(TAG, "Recording stopped: " + filePath);
        
        // Mostrar notificação de conclusão
        Notification notification = createRecordingNotification("Gravação concluída", filePath);
        notificationManager.notify(NOTIFICATION_ID, notification);
        
        stopForeground(true);
        isServiceRunning = false;
        
        // Notificar outros componentes
        Intent broadcast = new Intent("com.example.freefireaimbot.RECORDING_STOPPED");
        broadcast.putExtra("filePath", filePath);
        sendBroadcast(broadcast);
        
        // Parar o serviço após um delay
        new android.os.Handler().postDelayed(() -> {
            if (!isServiceRunning) {
                stopSelf();
            }
        }, 5000);
    }
    
    @Override
    public void onRecordingError(String error) {
        Log.e(TAG, "Recording error: " + error);
        
        Notification notification = createRecordingNotification("Erro na gravação: " + error, null);
        notificationManager.notify(NOTIFICATION_ID, notification);
        
        stopForeground(true);
        isServiceRunning = false;
        
        // Notificar outros componentes
        Intent broadcast = new Intent("com.example.freefireaimbot.RECORDING_ERROR");
        broadcast.putExtra("error", error);
        sendBroadcast(broadcast);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (screenRecorder != null) {
            screenRecorder.destroy();
        }
        
        Log.d(TAG, "ScreenRecordingService destroyed");
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    // Métodos estáticos para controlar o serviço
    public static void startRecording(android.content.Context context, int resultCode, Intent data) {
        Intent serviceIntent = new Intent(context, ScreenRecordingService.class);
        serviceIntent.setAction("START_RECORDING");
        serviceIntent.putExtra("resultCode", resultCode);
        serviceIntent.putExtra("data", data);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
    
    public static void stopRecording(android.content.Context context) {
        Intent serviceIntent = new Intent(context, ScreenRecordingService.class);
        serviceIntent.setAction("STOP_RECORDING");
        context.startService(serviceIntent);
    }
    
    public static void toggleRecording(android.content.Context context) {
        Intent serviceIntent = new Intent(context, ScreenRecordingService.class);
        serviceIntent.setAction("TOGGLE_RECORDING");
        context.startService(serviceIntent);
    }
}

