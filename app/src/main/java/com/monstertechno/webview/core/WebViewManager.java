package com.monstertechno.webview.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.webkit.*;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.monstertechno.webview.bridge.JavaScriptBridge;
import com.monstertechno.webview.core.clients.ModernWebChromeClient;
import com.monstertechno.webview.core.clients.ModernWebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewManager {
    
    private static volatile WebViewManager INSTANCE;
    
    public static void initialize(Context context) {
        if (INSTANCE == null) {
            synchronized (WebViewManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WebViewManager();
                    INSTANCE.initializeWebView(context);
                }
            }
        }
    }
    
    public static WebViewManager getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("WebViewManager not initialized");
        }
        return INSTANCE;
    }
    
    private void initializeWebView(Context context) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BASIC_USAGE)) {
            ServiceWorkerController swController = ServiceWorkerController.getInstance();
            swController.setServiceWorkerClient(new ServiceWorkerClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebResourceRequest request) {
                    return null;
                }
            });
        }
    }
    
    public WebView setupWebView(WebView webView, Context context) {
        WebSettings settings = webView.getSettings();
        
        // Enable JavaScript and modern web features
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        
        // DOM storage
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        
        // File access
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        
        // Enable hardware acceleration
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        
        // Media playback
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        // Geolocation
        settings.setGeolocationEnabled(true);
        
        // Mixed content
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        
        // Zoom and viewport
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        
        // User agent - modern browser
        settings.setUserAgentString(getModernUserAgent(settings.getUserAgentString()));
        
        // Text encoding
        settings.setDefaultTextEncodingName("UTF-8");
        
        // Enable modern WebView features if available
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_AUTO);
        }
        
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
            WebSettingsCompat.setSafeBrowsingEnabled(settings, true);
        }
        
        // Set up clients
        webView.setWebViewClient(new ModernWebViewClient(context));
        webView.setWebChromeClient(new ModernWebChromeClient(context));
        
        // Add JavaScript bridge
        webView.addJavascriptInterface(new JavaScriptBridge(context), "AndroidBridge");
        
        return webView;
    }
    
    private String getModernUserAgent(String originalUserAgent) {
        if (originalUserAgent.contains("Version/")) {
            return originalUserAgent.replaceAll("Version/[\\d.]+", "Version/4.0") 
                + " Chrome/120.0.0.0 Mobile Safari/537.36";
        } else {
            return originalUserAgent + " Chrome/120.0.0.0 Mobile Safari/537.36";
        }
    }
    
    public void clearCache(WebView webView, boolean includeDiskFiles) {
        webView.clearCache(includeDiskFiles);
        webView.clearHistory();
        webView.clearFormData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
        }
    }
    
    public void enableDebugging() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }
}