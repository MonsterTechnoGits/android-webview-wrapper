package com.monstertechno.webview.bridge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.monstertechno.webview.managers.BiometricManager;
import com.monstertechno.webview.managers.NotificationManager;
import com.monstertechno.webview.managers.FileManager;

import java.util.concurrent.Executor;

public class JavaScriptBridge {
    
    private Context context;
    private Gson gson;
    private BiometricManager biometricManager;
    private NotificationManager notificationManager;
    private FileManager fileManager;
    
    public JavaScriptBridge(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.biometricManager = new BiometricManager(context);
        this.notificationManager = new NotificationManager(context);
        this.fileManager = new FileManager(context);
    }
    
    @JavascriptInterface
    public String getDeviceInfo() {
        JsonObject deviceInfo = new JsonObject();
        deviceInfo.addProperty("platform", "Android");
        deviceInfo.addProperty("version", Build.VERSION.RELEASE);
        deviceInfo.addProperty("sdk", Build.VERSION.SDK_INT);
        deviceInfo.addProperty("manufacturer", Build.MANUFACTURER);
        deviceInfo.addProperty("model", Build.MODEL);
        deviceInfo.addProperty("brand", Build.BRAND);
        
        return gson.toJson(deviceInfo);
    }
    
    @JavascriptInterface
    public void showToast(String message) {
        ((Activity) context).runOnUiThread(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
    
    @JavascriptInterface
    public void vibrate(int milliseconds) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(milliseconds, android.os.VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milliseconds);
            }
        }
    }
    
    @JavascriptInterface
    public void authenticateFingerprint(String callback) {
        if (context instanceof FragmentActivity) {
            biometricManager.authenticate((FragmentActivity) context, 
                () -> {
                    // Success
                    ((Activity) context).runOnUiThread(() -> {
                        executeJavaScript(callback + "(true, 'Authentication successful')");
                    });
                },
                (error) -> {
                    // Error
                    ((Activity) context).runOnUiThread(() -> {
                        executeJavaScript(callback + "(false, '" + error + "')");
                    });
                });
        }
    }
    
    @JavascriptInterface
    public void showNotification(String title, String message, String iconUrl) {
        notificationManager.showWebNotification(title, message, iconUrl);
    }
    
    @JavascriptInterface
    public void downloadFile(String url, String filename) {
        fileManager.downloadFile(url, filename);
    }
    
    @JavascriptInterface
    public void shareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(shareIntent, "Share"));
    }
    
    @JavascriptInterface
    public void shareFile(String filePath) {
        fileManager.shareFile(filePath);
    }
    
    @JavascriptInterface
    public void openExternalApp(String packageName) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        }
    }
    
    @JavascriptInterface
    public void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    
    @JavascriptInterface
    public String getAppInfo() {
        JsonObject appInfo = new JsonObject();
        try {
            String packageName = context.getPackageName();
            android.content.pm.PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            
            appInfo.addProperty("packageName", packageName);
            appInfo.addProperty("versionName", packageInfo.versionName);
            appInfo.addProperty("versionCode", packageInfo.versionCode);
            
        } catch (Exception e) {
            appInfo.addProperty("error", e.getMessage());
        }
        
        return gson.toJson(appInfo);
    }
    
    @JavascriptInterface
    public boolean isFeatureSupported(String feature) {
        switch (feature.toLowerCase()) {
            case "fingerprint":
                return biometricManager.isBiometricAvailable();
            case "camera":
                return context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA);
            case "microphone":
                return context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_MICROPHONE);
            case "location":
                return context.getPackageManager().hasSystemFeature(android.content.pm.PackageManager.FEATURE_LOCATION);
            case "vibrator":
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                return vibrator != null && vibrator.hasVibrator();
            default:
                return false;
        }
    }
    
    @JavascriptInterface
    public void setClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("WebView", text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }
    
    @JavascriptInterface
    public String getClipboard() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.getPrimaryClip() != null) {
            android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            return item.getText().toString();
        }
        return "";
    }
    
    private void executeJavaScript(String script) {
        if (context instanceof Activity && context instanceof JavaScriptExecutor) {
            ((JavaScriptExecutor) context).executeJavaScript(script);
        }
    }
    
    public interface JavaScriptExecutor {
        void executeJavaScript(String script);
    }
}