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
import android.provider.Settings;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
    private ScrollView errorLayout;
    private FrameLayout splashLayout;
    private TextView errorTitle, errorMessage, errorCode;
    
    // Managers
    private WebViewManager webViewManager;
    private PermissionManager permissionManager;
    private ThemeManager themeManager;
    private JavaScriptBridge jsBridge;
    
    // File chooser
    private ValueCallback<Uri[]> filePathCallback;
    private ActivityResultLauncher<Intent> fileChooserLauncher;
    
    // Media control receiver
    private BroadcastReceiver mediaControlReceiver;

    // True while a retry load is in progress — suppresses WebView flash on onPageLoadStarted
    private boolean isRetrying = false;
    // True while splash is showing — WebView stays hidden until onPageLoadFinished
    private boolean isSplashing = false;
    
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

        // If the activity was recreated by a DayNight mode change (savedInstanceState != null)
        // the WebView already has a page loaded — re-inject the theme observer immediately
        // rather than waiting for onPageLoadFinished which won't fire again.
        if (savedInstanceState != null && themeManager != null) {
            webView.post(() -> themeManager.injectThemeObserver());
        }

        // Show splash screen only on a fresh launch, not after recreation
        if (savedInstanceState == null) {
            if (AppConfig.SHOW_SPLASH_SCREEN) {
                showSplashScreen();
            } else {
                loadTargetWebsite();
            }
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
        errorCode = findViewById(R.id.errorCode);
        
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
        jsBridge = new JavaScriptBridge(this);
        webViewManager.setupWebView(webView, this, jsBridge);

        // Initialize theme manager and wire it to the bridge for real-time updates
        if (AppConfig.isAutoThemeAdaptationEnabled()) {
            themeManager = new ThemeManager(this, webView);
            jsBridge.setThemeManager(themeManager);
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
            isRetrying = true;
            errorLayout.setVisibility(View.GONE);
            // WebView stays GONE until onPageLoadFinished confirms a successful load
            loadTargetWebsite();
        });

        // Open device network settings so the user can fix connectivity
        findViewById(R.id.openSettingsButton).setOnClickListener(v ->
            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)));
    }
    
    private void showSplashScreen() {
        isSplashing = true;
        splashLayout.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);

        // After the delay, hide splash and kick off the load.
        // WebView stays GONE until onPageLoadFinished confirms the page is ready.
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashLayout.setVisibility(View.GONE);
            loadTargetWebsite();
        }, AppConfig.SPLASH_DURATION_MS);
    }

    private void hideSplashScreen() {
        if (splashLayout != null) {
            splashLayout.setVisibility(View.GONE);
            // Only reveal WebView here for non-splash paths (e.g. page started mid-session)
            if (!isSplashing) {
                webView.setVisibility(View.VISIBLE);
            }
        }
    }
    
    private void loadTargetWebsite() {
        String url = AppConfig.getMainUrl();
        webView.loadUrl(url);
    }
    
    private void showError(int webViewErrorCode, String rawDescription) {
        String[] friendly = friendlyErrorText(webViewErrorCode, rawDescription);
        runOnUiThread(() -> {
            webView.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorTitle.setText(friendly[0]);
            errorMessage.setText(friendly[1]);
            if (errorCode != null) {
                errorCode.setText("Error code: " + rawDescription);
                errorCode.setVisibility(View.VISIBLE);
            }
        });
    }

    private String[] friendlyErrorText(int code, String raw) {
        switch (code) {
            case WebViewClient.ERROR_HOST_LOOKUP:
                return new String[]{
                    "Can't find this website",
                    "The website address couldn't be found. This usually means you're not connected to the internet, or the address may have changed."
                };
            case WebViewClient.ERROR_CONNECT:
                return new String[]{
                    "No internet connection",
                    "Your device isn't connected to the internet. Please turn on Wi-Fi or mobile data and try again."
                };
            case WebViewClient.ERROR_TIMEOUT:
                return new String[]{
                    "The page is taking too long",
                    "The website took too long to respond. It might be busy or your connection is slow. Try again in a moment."
                };
            case WebViewClient.ERROR_FAILED_SSL_HANDSHAKE:
            case WebViewClient.ERROR_BAD_URL:
                return new String[]{
                    "Secure connection failed",
                    "We couldn't open a secure connection to this website. The site's security certificate may be outdated or invalid."
                };
            case WebViewClient.ERROR_FILE_NOT_FOUND:
                return new String[]{
                    "Page not found",
                    "The page you're looking for doesn't exist or may have been moved. Try going back and navigating again."
                };
            case WebViewClient.ERROR_TOO_MANY_REQUESTS:
                return new String[]{
                    "Too many requests",
                    "You've made too many requests in a short time. Please wait a moment and then try again."
                };
            case WebViewClient.ERROR_PROXY_AUTHENTICATION:
                return new String[]{
                    "Network access blocked",
                    "Your network requires a login or proxy authentication before you can access the internet."
                };
            default:
                return new String[]{
                    "Something went wrong",
                    "We couldn't load the page. Please check your internet connection and try again. If the problem continues, contact support."
                };
        }
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
            // During a retry the WebView stays hidden until load succeeds — skip hideError
            if (!isRetrying) {
                hideError();
                hideSplashScreen();
            }
        });
    }

    @Override
    public void onPageLoadFinished(String url) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            if (isRetrying || isSplashing) {
                isRetrying = false;
                isSplashing = false;
                webView.setVisibility(View.VISIBLE);
            }

            // Inject the MutationObserver so theme changes are pushed to us in real time.
            // Also fires once immediately to sync the status bar on first load.
            if (themeManager != null) {
                themeManager.injectThemeObserver();
            }
        });
    }

    @Override
    public void onPageLoadError(String url, int errorCode, String description) {
        isRetrying = false;
        isSplashing = false;
        showError(errorCode, description);
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