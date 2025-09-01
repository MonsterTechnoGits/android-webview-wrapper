package com.monstertechno.webview.core.clients;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.*;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebResourceErrorCompat;
import androidx.webkit.WebViewClientCompat;

import com.monstertechno.webview.R;
import com.monstertechno.webview.config.AppConfig;
import com.monstertechno.webview.managers.PermissionManager;

public class ModernWebViewClient extends WebViewClientCompat {
    
    private Context context;
    private PermissionManager permissionManager;
    
    public ModernWebViewClient(Context context) {
        this.context = context;
        this.permissionManager = new PermissionManager(context);
    }
    
    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        // Show loading indicator
        if (context instanceof WebViewListener) {
            ((WebViewListener) context).onPageLoadStarted(url);
        }
    }
    
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        // Hide loading indicator
        if (context instanceof WebViewListener) {
            ((WebViewListener) context).onPageLoadFinished(url);
        }
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        
        // Handle special URL schemes
        if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("sms:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
                return true;
            }
        }
        
        // Handle file downloads
        if (url.contains("download") || isDownloadableFile(url)) {
            if (AppConfig.isFileDownloadsEnabled() && context instanceof WebViewListener) {
                ((WebViewListener) context).onDownloadRequested(url);
            }
            return true;
        }
        
        // Check if URL should stay in WebView or open in Custom Tabs
        if (AppConfig.isExternalUrl(url)) {
            // Open external links in Chrome Custom Tabs
            openInCustomTabs(url);
            return true;
        }
        
        // Allow internal navigation
        return super.shouldOverrideUrlLoading(view, request);
    }
    
    private void openInCustomTabs(String url) {
        try {
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            
            // Customize the Custom Tab
            builder.setToolbarColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark));
            builder.setSecondaryToolbarColor(ContextCompat.getColor(context, android.R.color.holo_blue_bright));
            builder.setStartAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            builder.setExitAnimations(context, android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            builder.setShowTitle(true);
            builder.setUrlBarHidingEnabled(false);
            
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(url));
            
        } catch (Exception e) {
            // Fallback to regular browser if Custom Tabs not available
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
        }
    }
    
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // In production, show dialog to user
        // For now, proceed (not recommended for production)
        handler.proceed();
    }
    
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        
        if (context instanceof WebViewListener) {
            ((WebViewListener) context).onPageLoadError(failingUrl, errorCode, description);
        }
    }
    
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        // Handle ad blocking or custom resource loading here
        return super.shouldInterceptRequest(view, request);
    }
    
    private boolean isDownloadableFile(String url) {
        String[] downloadableExtensions = {
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".zip", ".rar", ".7z", ".apk", ".exe", ".dmg",
            ".mp4", ".avi", ".mov", ".mp3", ".wav", ".flac"
        };
        
        String urlLower = url.toLowerCase();
        for (String ext : downloadableExtensions) {
            if (urlLower.contains(ext)) {
                return true;
            }
        }
        return false;
    }
    
    public interface WebViewListener {
        void onPageLoadStarted(String url);
        void onPageLoadFinished(String url);
        void onPageLoadError(String url, int errorCode, String description);
        void onDownloadRequested(String url);
    }
}