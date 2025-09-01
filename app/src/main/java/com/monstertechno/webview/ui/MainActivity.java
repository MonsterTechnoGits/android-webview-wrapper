package com.monstertechno.webview.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.monstertechno.webview.R;
import com.monstertechno.webview.bridge.JavaScriptBridge;
import com.monstertechno.webview.config.AppConfig;
import com.monstertechno.webview.core.WebViewManager;
import com.monstertechno.webview.core.clients.ModernWebChromeClient;
import com.monstertechno.webview.core.clients.ModernWebViewClient;
import com.monstertechno.webview.managers.PermissionManager;
import com.monstertechno.webview.managers.ThemeManager;

public class MainActivity extends AppCompatActivity implements 
        ModernWebViewClient.WebViewListener,
        ModernWebChromeClient.WebChromeListener,
        JavaScriptBridge.JavaScriptExecutor {
    
    // UI Components
    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorLayout;
    private LinearLayout splashLayout;
    private TextView errorTitle, errorMessage;
    
    // Managers
    private WebViewManager webViewManager;
    private PermissionManager permissionManager;
    private ThemeManager themeManager;
    
    // File chooser
    private ValueCallback<Uri[]> filePathCallback;
    private ActivityResultLauncher<Intent> fileChooserLauncher;
    
    // Media control receiver
    private BroadcastReceiver mediaControlReceiver;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        
        setContentView(R.layout.activity_main);
        
        initializeManagers();
        initializeUI();
        setupWebView();
        setupEventListeners();
        
        if (AppConfig.isMediaNotificationsEnabled()) {
            registerMediaReceiver();
        }
        
        // Show splash screen if enabled
        if (AppConfig.SHOW_SPLASH_SCREEN) {
            showSplashScreen();
        } else {
            loadTargetWebsite();
        }
        
        // Handle intent if app was opened with URL
        handleIntent(getIntent());
    }
    
    private void initializeManagers() {
        webViewManager = WebViewManager.getInstance();
        permissionManager = new PermissionManager(this);
    }
    
    private void initializeUI() {
        // Find views
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        errorLayout = findViewById(R.id.errorLayout);
        splashLayout = findViewById(R.id.splashLayout);
        errorTitle = findViewById(R.id.errorTitle);
        errorMessage = findViewById(R.id.errorMessage);
        
        // Setup file chooser launcher
        fileChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (filePathCallback != null) {
                    Uri[] results = null;
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        results = new Uri[]{result.getData().getData()};
                    }
                    filePathCallback.onReceiveValue(results);
                    filePathCallback = null;
                }
            }
        );
    }
    
    private void setupWebView() {
        webViewManager.setupWebView(webView, this);
        
        // Initialize theme manager after WebView setup
        if (AppConfig.isAutoThemeAdaptationEnabled()) {
            themeManager = new ThemeManager(this, webView);
        }
        
        // Add JavaScript bridge only if enabled
        if (!AppConfig.isJavaScriptBridgeEnabled()) {
            webView.removeJavascriptInterface("AndroidBridge");
        }
        
        // Enable debugging for development
        WebViewManager.getInstance().enableDebugging();
    }
    
    private void setupEventListeners() {
        // Error layout retry button
        findViewById(R.id.retryButton).setOnClickListener(v -> {
            hideError();
            loadTargetWebsite();
        });
    }
    
    private void showSplashScreen() {
        splashLayout.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        
        // Hide splash after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            hideSplashScreen();
            loadTargetWebsite();
        }, AppConfig.SPLASH_DURATION_MS);
    }
    
    private void hideSplashScreen() {
        if (splashLayout != null) {
            splashLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadTargetWebsite() {
        String url = AppConfig.getMainUrl();
        webView.loadUrl(url);
    }
    
    private void showError(String title, String message) {
        runOnUiThread(() -> {
            webView.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorTitle.setText(title);
            errorMessage.setText(message);
        });
    }
    
    private void hideError() {
        runOnUiThread(() -> {
            errorLayout.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        });
    }
    
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerMediaReceiver() {
        if (!AppConfig.isMediaNotificationsEnabled()) return;
        
        mediaControlReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getStringExtra("action");
                if ("play".equals(action) || "pause".equals(action)) {
                    // Execute JavaScript to control media playback
                    String jsCode = action.equals("play") ? 
                        "if(document.querySelector('video, audio')) { document.querySelector('video, audio').play(); }" :
                        "if(document.querySelector('video, audio')) { document.querySelector('video, audio').pause(); }";
                    webView.evaluateJavascript(jsCode, null);
                }
            }
        };
        
        IntentFilter filter = new IntentFilter("com.monstertechno.webview.MEDIA_CONTROL");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mediaControlReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(mediaControlReceiver, filter);
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }
    
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String url = data.toString();
                // Only load URLs from our target host, others open in Custom Tabs
                if (AppConfig.isAllowedHost(url)) {
                    webView.loadUrl(url);
                }
            }
        }
    }
    
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (themeManager != null) {
            themeManager.onConfigurationChanged(newConfig);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaControlReceiver != null) {
            unregisterReceiver(mediaControlReceiver);
        }
        if (webView != null) {
            webView.destroy();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    
    // WebViewListener implementations
    @Override
    public void onPageLoadStarted(String url) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            hideError();
            hideSplashScreen();
        });
    }
    
    @Override
    public void onPageLoadFinished(String url) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            
            // Adapt theme from website after page loads
            if (themeManager != null) {
                // Small delay to ensure page is fully rendered and scripts executed
                webView.postDelayed(() -> {
                    android.util.Log.d("MainActivity", "Page load finished, triggering theme adaptation for URL: " + url);
                    themeManager.adaptThemeFromWebsite();
                    
                    // Additional delay for testing - try again with more time
                    webView.postDelayed(() -> themeManager.forceThemeDetection(), 2000);
                }, 1000);
            }
        });
    }
    
    @Override
    public void onPageLoadError(String url, int errorCode, String description) {
        showError("Connection Error", description);
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
        });
    }
    
    @Override
    public void onDownloadRequested(String url) {
        if (AppConfig.isFileDownloadsEnabled()) {
            Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show();
        }
    }
    
    // WebChromeListener implementations
    @Override
    public void onProgressChanged(int progress) {
        runOnUiThread(() -> {
            progressBar.setProgress(progress);
            if (progress == 100) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    
    @Override
    public void onTitleChanged(String title) {
        // Update window title if needed
        runOnUiThread(() -> setTitle(title));
    }
    
    @Override
    public void onIconChanged(Bitmap icon) {
        // Icon changes handled automatically
    }
    
    @Override
    public void onFileChooserRequested(WebChromeClient.FileChooserParams params, ValueCallback<Uri[]> callback) {
        if (!AppConfig.isFileDownloadsEnabled()) {
            callback.onReceiveValue(null);
            return;
        }
        
        filePathCallback = callback;
        Intent intent = params.createIntent();
        try {
            fileChooserLauncher.launch(intent);
        } catch (Exception e) {
            filePathCallback = null;
            Toast.makeText(this, "File chooser not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onJsAlert(String url, String message, JsResult result) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(AppConfig.APP_NAME)
            .setMessage(message)
            .setPositiveButton("OK", (dialog, which) -> result.confirm())
            .setOnCancelListener(dialog -> result.cancel())
            .show();
    }
    
    @Override
    public void onJsConfirm(String url, String message, JsResult result) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(AppConfig.APP_NAME)
            .setMessage(message)
            .setPositiveButton("OK", (dialog, which) -> result.confirm())
            .setNegativeButton("Cancel", (dialog, which) -> result.cancel())
            .setOnCancelListener(dialog -> result.cancel())
            .show();
    }
    
    @Override
    public void onJsPrompt(String url, String message, String defaultValue, JsPromptResult result) {
        EditText input = new EditText(this);
        input.setText(defaultValue);
        
        new MaterialAlertDialogBuilder(this)
            .setTitle(AppConfig.APP_NAME)
            .setMessage(message)
            .setView(input)
            .setPositiveButton("OK", (dialog, which) -> result.confirm(input.getText().toString()))
            .setNegativeButton("Cancel", (dialog, which) -> result.cancel())
            .setOnCancelListener(dialog -> result.cancel())
            .show();
    }
    
    // JavaScriptExecutor implementation
    @Override
    public void executeJavaScript(String script) {
        runOnUiThread(() -> webView.evaluateJavascript(script, null));
    }
    
    /**
     * Test theme color changes - can be called from JavaScript bridge
     */
    public void testStatusBarColor(String color) {
        if (themeManager != null) {
            themeManager.testThemeColor(color);
        }
    }
}