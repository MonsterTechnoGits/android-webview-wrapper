package com.monstertechno.webview.core.clients;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.webkit.*;

import androidx.core.content.ContextCompat;

import com.monstertechno.webview.managers.FileManager;
import com.monstertechno.webview.managers.NotificationManager;
import com.monstertechno.webview.managers.PermissionManager;

public class ModernWebChromeClient extends WebChromeClient {
    
    private Context context;
    private PermissionManager permissionManager;
    private FileManager fileManager;
    private NotificationManager notificationManager;
    private ValueCallback<Uri[]> filePathCallback;
    
    public ModernWebChromeClient(Context context) {
        this.context = context;
        this.permissionManager = new PermissionManager(context);
        this.fileManager = new FileManager(context);
        this.notificationManager = new NotificationManager(context);
    }
    
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        if (context instanceof WebChromeListener) {
            ((WebChromeListener) context).onProgressChanged(newProgress);
        }
    }
    
    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (context instanceof WebChromeListener) {
            ((WebChromeListener) context).onTitleChanged(title);
        }
    }
    
    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
        if (context instanceof WebChromeListener) {
            ((WebChromeListener) context).onIconChanged(icon);
        }
    }
    
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, 
                                                   GeolocationPermissions.Callback callback) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            callback.invoke(origin, true, false);
        } else {
            permissionManager.requestLocationPermission(() -> {
                callback.invoke(origin, true, false);
            }, () -> {
                callback.invoke(origin, false, false);
            });
        }
    }
    
    @Override
    public void onPermissionRequest(PermissionRequest request) {
        String[] requestedResources = request.getResources();

        for (String resource : requestedResources) {
            switch (resource) {
                case PermissionRequest.RESOURCE_AUDIO_CAPTURE:
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{resource});
                        return;
                    } else {
                        permissionManager.requestAudioPermission(
                            () -> request.grant(new String[]{resource}),
                            request::deny
                        );
                        return;
                    }

                case PermissionRequest.RESOURCE_VIDEO_CAPTURE:
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[]{resource});
                        return;
                    } else {
                        permissionManager.requestCameraPermission(
                            () -> request.grant(new String[]{resource}),
                            request::deny
                        );
                        return;
                    }

                case PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID:
                    request.grant(new String[]{resource});
                    return;
            }
        }

        request.deny();
    }
    
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        this.filePathCallback = filePathCallback;
        
        if (context instanceof WebChromeListener) {
            ((WebChromeListener) context).onFileChooserRequested(fileChooserParams, filePathCallback);
            return true;
        }
        
        return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }
    
    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        if (context instanceof WebChromeListener) {
            ((WebChromeListener) context).onJsAlert(url, message, result);
            return true;
        }
        return super.onJsAlert(view, url, message, result);
    }
    
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        if (context instanceof WebChromeListener) {
            ((WebChromeListener) context).onJsConfirm(url, message, result);
            return true;
        }
        return super.onJsConfirm(view, url, message, result);
    }
    
    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        if (context instanceof WebChromeListener) {
            ((WebChromeListener) context).onJsPrompt(url, message, defaultValue, result);
            return true;
        }
        return super.onJsPrompt(view, url, message, defaultValue, result);
    }
    
    public interface WebChromeListener {
        void onProgressChanged(int progress);
        void onTitleChanged(String title);
        void onIconChanged(Bitmap icon);
        void onFileChooserRequested(FileChooserParams params, ValueCallback<Uri[]> callback);
        void onJsAlert(String url, String message, JsResult result);
        void onJsConfirm(String url, String message, JsResult result);
        void onJsPrompt(String url, String message, String defaultValue, JsPromptResult result);
    }
}