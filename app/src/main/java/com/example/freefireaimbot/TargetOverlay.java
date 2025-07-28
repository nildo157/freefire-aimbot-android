package com.example.freefireaimbot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class TargetOverlay extends View {
    private static final String TAG = "TargetOverlay";
    
    private Paint targetPaint;
    private Paint crosshairPaint;
    private Paint textPaint;
    private List<AimbotManager.DetectedObject> targets;
    private boolean showTargets = true;
    private boolean showCrosshair = true;
    
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private boolean isAttached = false;
    
    public TargetOverlay(Context context) {
        super(context);
        initializePaints();
        setupWindowManager(context);
    }
    
    private void initializePaints() {
        // Paint para desenhar caixas dos alvos
        targetPaint = new Paint();
        targetPaint.setColor(Color.RED);
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(3f);
        targetPaint.setAntiAlias(true);
        
        // Paint para desenhar crosshair
        crosshairPaint = new Paint();
        crosshairPaint.setColor(Color.GREEN);
        crosshairPaint.setStyle(Paint.Style.STROKE);
        crosshairPaint.setStrokeWidth(2f);
        crosshairPaint.setAntiAlias(true);
        
        // Paint para texto
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setAntiAlias(true);
        textPaint.setShadowLayer(2f, 1f, 1f, Color.BLACK);
    }
    
    private void setupWindowManager(Context context) {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (showCrosshair) {
            drawCrosshair(canvas);
        }
        
        if (showTargets && targets != null) {
            drawTargets(canvas);
        }
    }
    
    private void drawCrosshair(Canvas canvas) {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int crosshairSize = 20;
        
        // Desenhar crosshair central
        canvas.drawLine(centerX - crosshairSize, centerY, 
                        centerX + crosshairSize, centerY, crosshairPaint);
        canvas.drawLine(centerX, centerY - crosshairSize, 
                        centerX, centerY + crosshairSize, crosshairPaint);
        
        // Desenhar círculo central
        canvas.drawCircle(centerX, centerY, 3f, crosshairPaint);
    }
    
    private void drawTargets(Canvas canvas) {
        for (AimbotManager.DetectedObject target : targets) {
            // Converter coordenadas normalizadas para pixels da tela
            float left = target.x * getWidth();
            float top = target.y * getHeight();
            float right = left + (target.width * getWidth());
            float bottom = top + (target.height * getHeight());
            
            // Desenhar caixa do alvo
            Rect targetRect = new Rect((int) left, (int) top, (int) right, (int) bottom);
            canvas.drawRect(targetRect, targetPaint);
            
            // Desenhar ponto central do alvo
            float centerX = left + (target.width * getWidth()) / 2;
            float centerY = top + (target.height * getHeight()) / 2;
            canvas.drawCircle(centerX, centerY, 5f, targetPaint);
            
            // Desenhar linha do crosshair para o alvo (se estiver próximo)
            float screenCenterX = getWidth() / 2f;
            float screenCenterY = getHeight() / 2f;
            float distance = (float) Math.sqrt(
                Math.pow(centerX - screenCenterX, 2) + Math.pow(centerY - screenCenterY, 2)
            );
            
            if (distance < 200) { // Desenhar linha apenas se o alvo estiver próximo
                Paint linePaint = new Paint(crosshairPaint);
                linePaint.setAlpha(128);
                canvas.drawLine(screenCenterX, screenCenterY, centerX, centerY, linePaint);
            }
            
            // Desenhar informações do alvo
            String targetInfo = String.format("%s (%.0f%%)", 
                target.label, target.confidence * 100);
            canvas.drawText(targetInfo, left, top - 10, textPaint);
            
            // Desenhar distância
            String distanceText = String.format("Dist: %.0fpx", distance);
            canvas.drawText(distanceText, left, bottom + 25, textPaint);
        }
    }
    
    public void updateTargets(List<AimbotManager.DetectedObject> newTargets) {
        this.targets = newTargets;
        invalidate(); // Redesenhar a view
    }
    
    public void setShowTargets(boolean show) {
        this.showTargets = show;
        invalidate();
    }
    
    public void setShowCrosshair(boolean show) {
        this.showCrosshair = show;
        invalidate();
    }
    
    public void setTargetColor(int color) {
        targetPaint.setColor(color);
        invalidate();
    }
    
    public void setCrosshairColor(int color) {
        crosshairPaint.setColor(color);
        invalidate();
    }
    
    public void show() {
        if (!isAttached && windowManager != null) {
            try {
                windowManager.addView(this, layoutParams);
                isAttached = true;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error showing target overlay", e);
            }
        }
    }
    
    public void hide() {
        if (isAttached && windowManager != null) {
            try {
                windowManager.removeView(this);
                isAttached = false;
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error hiding target overlay", e);
            }
        }
    }
    
    public boolean isShowing() {
        return isAttached;
    }
    
    public AimbotManager.DetectedObject getClosestTarget() {
        if (targets == null || targets.isEmpty()) {
            return null;
        }
        
        float screenCenterX = getWidth() / 2f;
        float screenCenterY = getHeight() / 2f;
        
        AimbotManager.DetectedObject closestTarget = null;
        float minDistance = Float.MAX_VALUE;
        
        for (AimbotManager.DetectedObject target : targets) {
            float targetCenterX = (target.x + target.width / 2) * getWidth();
            float targetCenterY = (target.y + target.height / 2) * getHeight();
            
            float distance = (float) Math.sqrt(
                Math.pow(targetCenterX - screenCenterX, 2) + 
                Math.pow(targetCenterY - screenCenterY, 2)
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                closestTarget = target;
            }
        }
        
        return closestTarget;
    }
}

