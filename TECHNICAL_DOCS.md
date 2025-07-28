# Documentação Técnica - Free Fire Aimbot

Esta documentação fornece detalhes técnicos sobre a arquitetura, implementação e funcionamento interno do Free Fire Aimbot.

## 🏗️ Arquitetura do Sistema

### Visão Geral
O Free Fire Aimbot é construído usando uma arquitetura modular que separa responsabilidades em componentes especializados:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   MainActivity  │    │ SettingsActivity│    │  OverlayService │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  AimbotManager  │    │ ScreenRecorder  │    │  ShizukuHelper  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │              ┌─────────────────┐              │
         │              │TargetOverlay    │              │
         │              └─────────────────┘              │
         │                                               │
┌─────────────────┐                            ┌─────────────────┐
│ TensorFlow Lite │                            │PrivilegedOps    │
└─────────────────┘                            └─────────────────┘
```

### Componentes Principais

#### 1. Interface de Usuário
- **MainActivity**: Tela principal com controles básicos
- **SettingsActivity**: Configurações avançadas e permissões
- **OverlayService**: Painel flutuante sobre outros aplicativos

#### 2. Core Engine
- **AimbotManager**: Lógica principal do aimbot
- **TargetOverlay**: Visualização de alvos detectados
- **ModelDownloader**: Gerenciamento de modelos TensorFlow

#### 3. Serviços de Sistema
- **ScreenRecorder**: Captura e gravação de tela
- **ShizukuHelper**: Interface com permissões privilegiadas
- **PrivilegedOperations**: Operações que requerem root/Shizuku

## 🧠 Sistema de Detecção de Alvos

### TensorFlow Lite Integration

#### Modelo de IA
```java
// Configuração do interpretador TensorFlow Lite
Interpreter.Options options = new Interpreter.Options();
options.setNumThreads(4);
options.setUseGPU(true); // Se disponível

interpreter = new Interpreter(modelBuffer, options);
```

#### Pipeline de Processamento
1. **Captura de Frame**: Screenshot da tela atual
2. **Pré-processamento**: Redimensionamento e normalização
3. **Inferência**: Execução do modelo TensorFlow Lite
4. **Pós-processamento**: Filtragem e classificação de detecções
5. **Visualização**: Desenho de overlays e indicadores

#### Estrutura de Dados
```java
public static class DetectedObject {
    public float x, y, width, height;  // Coordenadas normalizadas (0-1)
    public float confidence;           // Confiança da detecção (0-1)
    public String label;              // Classe do objeto detectado
    public long timestamp;            // Timestamp da detecção
}
```

### Algoritmo de Detecção
```java
public void processFrame(Bitmap frame) {
    // 1. Pré-processamento
    Bitmap resized = Bitmap.createScaledBitmap(frame, INPUT_WIDTH, INPUT_HEIGHT, true);
    float[][][][] input = preprocessImage(resized);
    
    // 2. Inferência
    float[][][] output = new float[1][MAX_DETECTIONS][6];
    interpreter.run(input, output);
    
    // 3. Pós-processamento
    List<DetectedObject> detections = postprocessOutput(output[0]);
    
    // 4. Filtragem por confiança
    List<DetectedObject> filtered = detections.stream()
        .filter(obj -> obj.confidence >= confidenceThreshold)
        .collect(Collectors.toList());
    
    // 5. Callback para UI
    if (callback != null) {
        callback.onTargetDetected(filtered);
    }
}
```

## 🎯 Sistema de Aimbot

### Cálculo de Movimento
O aimbot calcula o movimento necessário para alinhar a mira com o alvo:

```java
public Point calculateAimMovement(DetectedObject target, Point screenCenter) {
    // Converter coordenadas normalizadas para pixels
    float targetX = target.x * screenWidth + (target.width * screenWidth) / 2;
    float targetY = target.y * screenHeight + (target.height * screenHeight) / 2;
    
    // Calcular diferença
    float deltaX = targetX - screenCenter.x;
    float deltaY = targetY - screenCenter.y;
    
    // Aplicar sensibilidade
    deltaX *= sensitivity;
    deltaY *= sensitivity;
    
    // Aplicar suavização
    deltaX = smoothMovement(deltaX, previousDeltaX);
    deltaY = smoothMovement(deltaY, previousDeltaY);
    
    return new Point((int)deltaX, (int)deltaY);
}
```

### Suavização de Movimento
```java
private float smoothMovement(float current, float previous) {
    float smoothingFactor = 0.3f; // Ajustável
    return previous + (current - previous) * smoothingFactor;
}
```

### Seleção de Alvo
```java
public DetectedObject selectBestTarget(List<DetectedObject> targets) {
    return targets.stream()
        .filter(target -> target.confidence >= confidenceThreshold)
        .min((t1, t2) -> {
            float dist1 = calculateDistance(t1, screenCenter);
            float dist2 = calculateDistance(t2, screenCenter);
            return Float.compare(dist1, dist2);
        })
        .orElse(null);
}
```

## 📱 Sistema de Overlay

### Criação do Overlay
```java
private void createOverlay() {
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
            WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    );
    
    windowManager.addView(overlayView, params);
}
```

### Desenho de Alvos
```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    
    for (DetectedObject target : targets) {
        // Converter coordenadas
        float left = target.x * getWidth();
        float top = target.y * getHeight();
        float right = left + (target.width * getWidth());
        float bottom = top + (target.height * getHeight());
        
        // Desenhar caixa
        canvas.drawRect(left, top, right, bottom, targetPaint);
        
        // Desenhar ponto central
        float centerX = left + (right - left) / 2;
        float centerY = top + (bottom - top) / 2;
        canvas.drawCircle(centerX, centerY, 5f, targetPaint);
        
        // Desenhar informações
        String info = String.format("%s (%.0f%%)", target.label, target.confidence * 100);
        canvas.drawText(info, left, top - 10, textPaint);
    }
}
```

## 📹 Sistema de Gravação

### MediaProjection Setup
```java
private void setupMediaRecorder() throws IOException {
    mediaRecorder = new MediaRecorder();
    
    // Configurar fontes
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    
    // Configurar formato
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    
    // Configurar qualidade
    mediaRecorder.setVideoSize(screenWidth, screenHeight);
    mediaRecorder.setVideoFrameRate(30);
    mediaRecorder.setVideoEncodingBitRate(5000000);
    
    // Configurar saída
    mediaRecorder.setOutputFile(outputFilePath);
    mediaRecorder.prepare();
}
```

### VirtualDisplay Creation
```java
private void createVirtualDisplay() {
    virtualDisplay = mediaProjection.createVirtualDisplay(
        "ScreenRecorder",
        screenWidth, screenHeight, screenDensity,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mediaRecorder.getSurface(),
        null, null
    );
}
```

## 🔐 Sistema Shizuku

### Inicialização
```java
public void initialize(Context context, ShizukuCallback callback) {
    this.context = context;
    this.callback = callback;
    
    // Registrar listeners
    Shizuku.addRequestPermissionResultListener(permissionListener);
    Shizuku.addBinderReceivedListener(binderListener);
    Shizuku.addBinderDeadListener(deathListener);
    
    // Verificar disponibilidade
    checkShizukuAvailability();
}
```

### Execução de Comandos
```java
public void executeCommand(String command, CommandCallback callback) {
    if (!hasPermission()) {
        callback.onError("Permissão Shizuku não concedida");
        return;
    }
    
    try {
        Process process = Shizuku.newProcess(
            new String[]{"sh", "-c", command}, 
            null, null
        );
        
        // Processar resultado em thread separada
        new Thread(() -> {
            try {
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    callback.onSuccess("Comando executado");
                } else {
                    callback.onError("Falha: " + exitCode);
                }
            } catch (InterruptedException e) {
                callback.onError("Interrompido: " + e.getMessage());
            }
        }).start();
        
    } catch (Exception e) {
        callback.onError("Erro: " + e.getMessage());
    }
}
```

## ⚡ Otimizações de Performance

### Threading Strategy
```java
// Thread principal: UI updates
// Thread de background: Processamento de IA
// Thread de I/O: Gravação de arquivos

private ExecutorService aiExecutor = Executors.newSingleThreadExecutor();
private ExecutorService ioExecutor = Executors.newCachedThreadPool();

public void processFrameAsync(Bitmap frame) {
    aiExecutor.submit(() -> {
        List<DetectedObject> targets = processFrame(frame);
        
        // Update UI na thread principal
        handler.post(() -> updateTargetOverlay(targets));
    });
}
```

### Memory Management
```java
// Pool de bitmaps para reutilização
private Queue<Bitmap> bitmapPool = new LinkedList<>();

public Bitmap getBitmap(int width, int height) {
    Bitmap bitmap = bitmapPool.poll();
    if (bitmap == null || bitmap.getWidth() != width || bitmap.getHeight() != height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }
    return bitmap;
}

public void recycleBitmap(Bitmap bitmap) {
    if (bitmap != null && !bitmap.isRecycled()) {
        bitmapPool.offer(bitmap);
    }
}
```

### GPU Acceleration
```java
// Usar GPU para TensorFlow Lite quando disponível
private boolean setupGPUDelegate() {
    try {
        GpuDelegate gpuDelegate = new GpuDelegate();
        Interpreter.Options options = new Interpreter.Options();
        options.addDelegate(gpuDelegate);
        
        interpreter = new Interpreter(modelBuffer, options);
        return true;
    } catch (Exception e) {
        Log.w(TAG, "GPU delegate not available, using CPU");
        return false;
    }
}
```

## 📊 Monitoramento e Métricas

### Performance Metrics
```java
public class PerformanceMonitor {
    private long frameCount = 0;
    private long totalProcessingTime = 0;
    private long lastFpsUpdate = 0;
    
    public void recordFrameProcessing(long processingTime) {
        frameCount++;
        totalProcessingTime += processingTime;
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFpsUpdate >= 1000) {
            float fps = frameCount;
            float avgProcessingTime = totalProcessingTime / (float) frameCount;
            
            // Reset counters
            frameCount = 0;
            totalProcessingTime = 0;
            lastFpsUpdate = currentTime;
            
            // Update UI
            updatePerformanceDisplay(fps, avgProcessingTime);
        }
    }
}
```

### Memory Monitoring
```java
private void monitorMemoryUsage() {
    Runtime runtime = Runtime.getRuntime();
    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
    long maxMemory = runtime.maxMemory();
    
    float memoryUsagePercent = (usedMemory / (float) maxMemory) * 100;
    
    if (memoryUsagePercent > 80) {
        // Trigger garbage collection
        System.gc();
        
        // Reduce quality if necessary
        if (memoryUsagePercent > 90) {
            reduceProcessingQuality();
        }
    }
}
```

## 🔧 Configurações e Persistência

### SharedPreferences Management
```java
public class SettingsManager {
    private static final String PREFS_NAME = "aimbot_settings";
    private SharedPreferences prefs;
    
    public void saveSettings(AimbotSettings settings) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("aimbot_enabled", settings.enabled);
        editor.putInt("sensitivity", settings.sensitivity);
        editor.putInt("confidence", settings.confidence);
        editor.putBoolean("show_targets", settings.showTargets);
        editor.apply();
    }
    
    public AimbotSettings loadSettings() {
        AimbotSettings settings = new AimbotSettings();
        settings.enabled = prefs.getBoolean("aimbot_enabled", false);
        settings.sensitivity = prefs.getInt("sensitivity", 50);
        settings.confidence = prefs.getInt("confidence", 70);
        settings.showTargets = prefs.getBoolean("show_targets", true);
        return settings;
    }
}
```

## 🛡️ Segurança e Validação

### Input Validation
```java
public boolean validateSettings(AimbotSettings settings) {
    if (settings.sensitivity < 0 || settings.sensitivity > 100) {
        return false;
    }
    if (settings.confidence < 0 || settings.confidence > 100) {
        return false;
    }
    return true;
}
```

### Permission Checks
```java
private boolean hasAllRequiredPermissions() {
    return hasOverlayPermission() && 
           hasScreenCapturePermission() && 
           hasShizukuPermission();
}

private void requestMissingPermissions() {
    if (!hasOverlayPermission()) {
        requestOverlayPermission();
    }
    if (!hasShizukuPermission()) {
        requestShizukuPermission();
    }
}
```

## 🐛 Debug e Logging

### Logging Strategy
```java
public class Logger {
    private static final String TAG = "FreeFireAimbot";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    
    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }
    
    public static void e(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
        // Em produção, enviar para crash reporting
    }
    
    public static void performance(String operation, long duration) {
        if (DEBUG) {
            Log.d(TAG, operation + " took " + duration + "ms");
        }
    }
}
```

### Debug Overlays
```java
private void drawDebugInfo(Canvas canvas) {
    if (!DEBUG_MODE) return;
    
    // FPS counter
    canvas.drawText("FPS: " + currentFps, 10, 30, debugPaint);
    
    // Memory usage
    canvas.drawText("Memory: " + memoryUsage + "%", 10, 60, debugPaint);
    
    // Processing time
    canvas.drawText("Process: " + avgProcessingTime + "ms", 10, 90, debugPaint);
    
    // Detection count
    canvas.drawText("Targets: " + targetCount, 10, 120, debugPaint);
}
```

## 📈 Análise de Dados

### Target Detection Analytics
```java
public class DetectionAnalytics {
    private List<DetectionEvent> events = new ArrayList<>();
    
    public void recordDetection(DetectedObject target) {
        DetectionEvent event = new DetectionEvent();
        event.timestamp = System.currentTimeMillis();
        event.confidence = target.confidence;
        event.label = target.label;
        event.position = new Point((int)target.x, (int)target.y);
        
        events.add(event);
        
        // Manter apenas últimos 1000 eventos
        if (events.size() > 1000) {
            events.remove(0);
        }
    }
    
    public DetectionStats getStats() {
        if (events.isEmpty()) return new DetectionStats();
        
        DetectionStats stats = new DetectionStats();
        stats.totalDetections = events.size();
        stats.avgConfidence = events.stream()
            .mapToDouble(e -> e.confidence)
            .average()
            .orElse(0.0);
        stats.detectionRate = calculateDetectionRate();
        
        return stats;
    }
}
```

## 🔄 Lifecycle Management

### Service Lifecycle
```java
@Override
public void onCreate() {
    super.onCreate();
    initializeComponents();
    startForeground(NOTIFICATION_ID, createNotification());
}

@Override
public int onStartCommand(Intent intent, int flags, int startId) {
    String action = intent != null ? intent.getAction() : null;
    
    switch (action) {
        case ACTION_START_AIMBOT:
            startAimbot();
            break;
        case ACTION_STOP_AIMBOT:
            stopAimbot();
            break;
        case ACTION_TOGGLE_RECORDING:
            toggleRecording();
            break;
    }
    
    return START_STICKY; // Restart if killed
}

@Override
public void onDestroy() {
    super.onDestroy();
    cleanup();
}
```

### Resource Cleanup
```java
private void cleanup() {
    // Stop AI processing
    if (aimbotManager != null) {
        aimbotManager.cleanup();
    }
    
    // Stop recording
    if (screenRecorder != null) {
        screenRecorder.destroy();
    }
    
    // Remove overlays
    if (targetOverlay != null) {
        targetOverlay.hide();
    }
    
    // Cleanup Shizuku
    if (shizukuHelper != null) {
        shizukuHelper.cleanup();
    }
    
    // Release resources
    if (interpreter != null) {
        interpreter.close();
    }
}
```

## 📋 Testing Strategy

### Unit Tests
```java
@Test
public void testTargetDetection() {
    // Arrange
    Bitmap testFrame = createTestFrame();
    AimbotManager aimbot = new AimbotManager();
    
    // Act
    List<DetectedObject> targets = aimbot.processFrame(testFrame);
    
    // Assert
    assertNotNull(targets);
    assertTrue(targets.size() > 0);
    assertTrue(targets.get(0).confidence > 0.5f);
}
```

### Integration Tests
```java
@Test
public void testOverlayIntegration() {
    // Test overlay creation and interaction
    OverlayService service = new OverlayService();
    service.onCreate();
    
    // Verify overlay is created
    assertTrue(service.isOverlayVisible());
    
    // Test button interactions
    service.toggleAimbot();
    assertTrue(service.isAimbotActive());
}
```

---

Esta documentação técnica fornece uma visão abrangente da implementação do Free Fire Aimbot. Para informações específicas sobre instalação e uso, consulte os arquivos README.md e INSTALL.md.

