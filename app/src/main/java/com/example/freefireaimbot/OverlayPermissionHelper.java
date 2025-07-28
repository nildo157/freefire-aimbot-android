package com.example.freefireaimbot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class OverlayPermissionHelper {
    
    public static final int OVERLAY_PERMISSION_REQUEST_CODE = 1001;
    
    public static boolean hasOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true; // Permissão não necessária em versões anteriores
    }
    
    public static void requestOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    public static void startOverlayService(Context context) {
        if (hasOverlayPermission(context)) {
            Intent serviceIntent = new Intent(context, OverlayService.class);
            context.startService(serviceIntent);
        }
    }
    
    public static void stopOverlayService(Context context) {
        Intent serviceIntent = new Intent(context, OverlayService.class);
        context.stopService(serviceIntent);
    }
}

