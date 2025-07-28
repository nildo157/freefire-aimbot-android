package com.example.freefireaimbot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AimbotManager {
    private static final String TAG = "AimbotManager";
    private static final String MODEL_FILE = "object_detection_model.tflite";
    private static final int INPUT_SIZE = 416;
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    
    private static AimbotManager instance;
    private Context context;
    private Interpreter tfliteInterpreter;
    private ImageProcessor imageProcessor;
    private boolean isActive = false;
    private boolean isInitialized = false;
    
    private MediaProjection mediaProjection;
    private ImageReader imageReader;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    
    private List<DetectedObject> detectedObjects = new ArrayList<>();
    private AimbotCallback callback;
    
    public interface AimbotCallback {
        void onTargetDetected(List<DetectedObject> targets);
        void onError(String error);
    }
    
    public static class DetectedObject {
        public float x, y, width, height;
        public float confidence;
        public String label;
        
        public DetectedObject(float x, float y, float width, float height, float confidence, String label) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.confidence = confidence;
            this.label = label;
        }
    }
    
    private AimbotManager() {}
    
    public static synchronized AimbotManager getInstance() {
        if (instance == null) {
            instance = new AimbotManager();
        }
        return instance;
    }
    
    public void initialize(Context context, AimbotCallback callback) {
        this.context = context;
        this.callback = callback;
        
        try {
            initializeTensorFlowLite();
            setupImageProcessor();
            setupScreenCapture();
            isInitialized = true;
            Log.d(TAG, "AimbotManager initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize AimbotManager", e);
            if (callback != null) {
                callback.onError("Falha na inicialização: " + e.getMessage());
            }
        }
    }
    
    private void initializeTensorFlowLite() throws IOException {
        // Carregar o modelo TensorFlow Lite
        ByteBuffer modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE);
        
        // Configurar opções do interpretador
        Interpreter.Options options = new Interpreter.Options();
        options.setNumThreads(4); // Usar 4 threads para melhor performance
        
        tfliteInterpreter = new Interpreter(modelBuffer, options);
        Log.d(TAG, "TensorFlow Lite model loaded successfully");
    }
    
    private void setupImageProcessor() {
        imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                .build();
    }
    
    private void setupScreenCapture() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        
        imageReader = ImageReader.newInstance(
                metrics.widthPixels,
                metrics.heightPixels,
                PixelFormat.RGBA_8888,
                2
        );
        
        backgroundThread = new HandlerThread("AimbotBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        
        imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundHandler);
    }
    
    private final ImageReader.OnImageAvailableListener imageAvailableListener = 
            new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            if (!isActive) return;
            
            Image image = reader.acquireLatestImage();
            if (image != null) {
                processImage(image);
                image.close();
            }
        }
    };
    
    private void processImage(Image image) {
        try {
            // Converter Image para Bitmap
            Bitmap bitmap = imageToBitmap(image);
            
            // Processar com TensorFlow Lite
            TensorImage tensorImage = new TensorImage();
            tensorImage.load(bitmap);
            tensorImage = imageProcessor.process(tensorImage);
            
            // Executar inferência
            runInference(tensorImage);
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
            if (callback != null) {
                callback.onError("Erro no processamento: " + e.getMessage());
            }
        }
    }
    
    private Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();
        
        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth() + rowPadding / pixelStride,
                image.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        bitmap.copyPixelsFromBuffer(buffer);
        
        return Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
    }
    
    private void runInference(TensorImage tensorImage) {
        // Preparar arrays de saída
        float[][][] outputLocations = new float[1][10][4]; // [batch, detections, coordinates]
        float[][] outputClasses = new float[1][10]; // [batch, detections]
        float[][] outputScores = new float[1][10]; // [batch, detections]
        float[] numDetections = new float[1];
        
        Object[] inputs = {tensorImage.getBuffer()};
        Object[] outputs = {outputLocations, outputClasses, outputScores, numDetections};
        
        // Executar inferência
        tfliteInterpreter.runForMultipleInputsOutputs(inputs, outputs);
        
        // Processar resultados
        processDetectionResults(outputLocations, outputClasses, outputScores, numDetections);
    }
    
    private void processDetectionResults(float[][][] locations, float[][] classes, 
                                       float[][] scores, float[] numDetections) {
        detectedObjects.clear();
        
        int numDet = (int) numDetections[0];
        for (int i = 0; i < numDet; i++) {
            float confidence = scores[0][i];
            
            if (confidence > CONFIDENCE_THRESHOLD) {
                float y1 = locations[0][i][0];
                float x1 = locations[0][i][1];
                float y2 = locations[0][i][2];
                float x2 = locations[0][i][3];
                
                float width = x2 - x1;
                float height = y2 - y1;
                
                String label = getClassLabel((int) classes[0][i]);
                
                // Filtrar apenas alvos relevantes (personagens)
                if (isValidTarget(label)) {
                    DetectedObject obj = new DetectedObject(x1, y1, width, height, confidence, label);
                    detectedObjects.add(obj);
                }
            }
        }
        
        // Notificar callback com os alvos detectados
        if (callback != null && !detectedObjects.isEmpty()) {
            callback.onTargetDetected(new ArrayList<>(detectedObjects));
        }
    }
    
    private String getClassLabel(int classIndex) {
        // Mapear índices de classe para labels
        String[] labels = {"person", "player", "enemy", "character"};
        if (classIndex >= 0 && classIndex < labels.length) {
            return labels[classIndex];
        }
        return "unknown";
    }
    
    private boolean isValidTarget(String label) {
        // Verificar se o objeto detectado é um alvo válido
        return label.equals("person") || label.equals("player") || 
               label.equals("enemy") || label.equals("character");
    }
    
    public boolean toggleAimbot() {
        if (!isInitialized) {
            Log.w(TAG, "AimbotManager not initialized");
            return false;
        }
        
        isActive = !isActive;
        Log.d(TAG, "Aimbot " + (isActive ? "activated" : "deactivated"));
        return isActive;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public List<DetectedObject> getDetectedObjects() {
        return new ArrayList<>(detectedObjects);
    }
    
    public void setMediaProjection(MediaProjection projection) {
        this.mediaProjection = projection;
        if (imageReader != null) {
            projection.createVirtualDisplay(
                    "ScreenCapture",
                    imageReader.getWidth(),
                    imageReader.getHeight(),
                    1,
                    0,
                    imageReader.getSurface(),
                    null,
                    backgroundHandler
            );
        }
    }
    
    public void cleanup() {
        isActive = false;
        
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
            tfliteInterpreter = null;
        }
        
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
        
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping background thread", e);
            }
            backgroundThread = null;
            backgroundHandler = null;
        }
        
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
        }
        
        isInitialized = false;
        Log.d(TAG, "AimbotManager cleaned up");
    }
}

