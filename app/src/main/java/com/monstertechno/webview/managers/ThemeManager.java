package com.monstertechno.webview.managers;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.monstertechno.webview.config.AppConfig;

public class ThemeManager {
    
    private Activity activity;
    private WebView webView;
    private WindowInsetsControllerCompat windowInsetsController;

    // Tracks last applied mode so we don't recreate the activity on every notify()
    private Boolean lastAppliedDark = null;

    // Default colors
    private static final int DEFAULT_LIGHT_STATUS_BAR = Color.parseColor("#FFFFFF");
    private static final int DEFAULT_DARK_STATUS_BAR = Color.parseColor("#000000");
    private static final int DEFAULT_ACCENT_COLOR = Color.parseColor("#2196F3");
    
    public ThemeManager(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
        
        // Initialize WindowInsetsController for all supported versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            windowInsetsController = WindowCompat.getInsetsController(activity.getWindow(), webView);
        }
        
        setupInitialTheme();
    }
    
    /**
     * Setup initial theme based on system dark/light mode before the page loads.
     */
    private void setupInitialTheme() {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        boolean sysDark = isSystemInDarkMode();
        lastAppliedDark = sysDark;
        updateStatusBarColor(
            sysDark ? DEFAULT_DARK_STATUS_BAR : DEFAULT_LIGHT_STATUS_BAR,
            !sysDark
        );
    }
    
    /**
     * Auto-detect and adapt theme from website
     */
    public void adaptThemeFromWebsite() {
        if (!AppConfig.isAutoThemeAdaptationEnabled()) return;
        
        // JavaScript to detect website theme
        String themeDetectionScript = 
            "(function() {" +
                "try {" +
                    "var result = {" +
                        "isDark: false," +
                        "themeColor: null," +
                        "backgroundColor: null," +
                        "primaryColor: null" +
                    "};" +
                    
                    // Check for explicit dark mode indicators
                    "var htmlElement = document.documentElement;" +
                    "var bodyElement = document.body;" +
                    
                    // Check for dark mode class names
                    "var darkClasses = ['dark', 'dark-mode', 'dark-theme', 'night-mode', 'theme-dark'];" +
                    "for (var i = 0; i < darkClasses.length; i++) {" +
                        "if (htmlElement.classList && htmlElement.classList.contains(darkClasses[i]) || " +
                            "bodyElement.classList && bodyElement.classList.contains(darkClasses[i])) {" +
                            "result.isDark = true;" +
                            "break;" +
                        "}" +
                    "}" +
                    
                    // Check CSS variables for theme colors
                    "var computedStyle = window.getComputedStyle(htmlElement);" +
                    "var themeColorMeta = document.querySelector('meta[name=\"theme-color\"]');" +
                    "if (themeColorMeta && themeColorMeta.content) {" +
                        "result.themeColor = themeColorMeta.content;" +
                    "}" +
                    
                    // Get background color
                    "var bgColor = computedStyle.backgroundColor || " +
                                "window.getComputedStyle(bodyElement).backgroundColor;" +
                    "if (bgColor && bgColor !== 'rgba(0, 0, 0, 0)' && bgColor !== 'transparent') {" +
                        "result.backgroundColor = bgColor;" +
                    "}" +
                    
                    // Auto-detect dark mode from background color
                    "if (!result.isDark && bgColor) {" +
                        "var rgb = bgColor.match(/\\d+/g);" +
                        "if (rgb && rgb.length >= 3) {" +
                            "var brightness = (parseInt(rgb[0]) * 299 + parseInt(rgb[1]) * 587 + parseInt(rgb[2]) * 114) / 1000;" +
                            "result.isDark = brightness < 128;" +
                        "}" +
                    "}" +
                    
                    // Check for CSS custom properties
                    "var cssVars = ['--primary-color', '--accent-color', '--theme-color', '--brand-color'];" +
                    "for (var i = 0; i < cssVars.length; i++) {" +
                        "var color = computedStyle.getPropertyValue(cssVars[i]).trim();" +
                        "if (color && color !== '') {" +
                            "result.primaryColor = color;" +
                            "break;" +
                        "}" +
                    "}" +
                    
                    "console.log('Theme detection result:', result);" +
                    "return JSON.stringify(result);" +
                "} catch(e) {" +
                    "console.error('Theme detection error:', e);" +
                    "return '{\"error\":\"' + e.toString() + '\"}';" +
                "}" +
            "})()";
        
        android.util.Log.d("ThemeManager", "Running theme detection script...");
        
        webView.evaluateJavascript(themeDetectionScript, result -> {
            android.util.Log.d("ThemeManager", "Theme detection result: " + result);
            
            if (result != null && !result.equals("null") && !result.equals("\"null\"")) {
                try {
                    // Remove outer quotes if present
                    String cleanResult = result;
                    if (cleanResult.startsWith("\"") && cleanResult.endsWith("\"")) {
                        cleanResult = cleanResult.substring(1, cleanResult.length() - 1);
                        // Unescape JSON string
                        cleanResult = cleanResult.replace("\\\"", "\"");
                    }
                    
                    android.util.Log.d("ThemeManager", "Processing theme data: " + cleanResult);
                    processThemeData(cleanResult);
                } catch (Exception e) {
                    android.util.Log.e("ThemeManager", "Error processing theme data", e);
                    adaptToSystemTheme();
                }
            } else {
                android.util.Log.d("ThemeManager", "No theme data returned, using system theme");
                adaptToSystemTheme();
            }
        });
    }
    
    /**
     * Process theme data from website
     */
    private void processThemeData(String jsonData) {
        try {
            android.util.Log.d("ThemeManager", "Processing JSON data: " + jsonData);
            
            // Simple JSON parsing (avoiding external dependencies)
            boolean isDark = jsonData.contains("\"isDark\":true") || jsonData.contains("isDark\":true") || 
                           jsonData.contains("\"isDark\": true") || jsonData.contains("isDark\": true");
            String themeColor = extractJsonValue(jsonData, "themeColor");
            String backgroundColor = extractJsonValue(jsonData, "backgroundColor");
            String primaryColor = extractJsonValue(jsonData, "primaryColor");
            
            android.util.Log.d("ThemeManager", String.format("Extracted values - isDark: %b, themeColor: %s, backgroundColor: %s, primaryColor: %s", 
                isDark, themeColor, backgroundColor, primaryColor));
            
            // Determine the best color to use
            final String finalColor;
            if (themeColor != null && !themeColor.isEmpty() && !themeColor.equals("null")) {
                finalColor = themeColor;
                android.util.Log.d("ThemeManager", "Using theme color: " + finalColor);
            } else if (primaryColor != null && !primaryColor.isEmpty() && !primaryColor.equals("null")) {
                finalColor = primaryColor;
                android.util.Log.d("ThemeManager", "Using primary color: " + finalColor);
            } else if (backgroundColor != null && !backgroundColor.isEmpty() && !backgroundColor.equals("null")) {
                finalColor = backgroundColor;
                android.util.Log.d("ThemeManager", "Using background color: " + finalColor);
            } else {
                finalColor = null;
                android.util.Log.d("ThemeManager", "No color found, using default");
            }
            
            // Apply theme
            final boolean finalIsDark = isDark;
            activity.runOnUiThread(() -> {
                if (finalColor != null) {
                    int color = parseColor(finalColor);
                    android.util.Log.d("ThemeManager", "Parsed color: #" + Integer.toHexString(color));
                    if (color != 0) {
                        updateStatusBarColor(color, !finalIsDark);
                        return;
                    }
                }
                
                // Fallback based on dark/light detection
                if (finalIsDark) {
                    android.util.Log.d("ThemeManager", "Applying dark theme");
                    updateStatusBarColor(DEFAULT_DARK_STATUS_BAR, false);
                } else {
                    android.util.Log.d("ThemeManager", "Applying light theme");
                    updateStatusBarColor(DEFAULT_LIGHT_STATUS_BAR, true);
                }
            });
            
        } catch (Exception e) {
            android.util.Log.e("ThemeManager", "Error processing theme data", e);
            adaptToSystemTheme();
        }
    }
    
    /**
     * Extract value from simple JSON string
     */
    private String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) {
                searchKey = key + ":";
                startIndex = json.indexOf(searchKey);
                if (startIndex == -1) return null;
                startIndex += searchKey.length();
            } else {
                startIndex += searchKey.length();
            }
            
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = json.indexOf("}", startIndex);
                }
            }
            
            if (endIndex > startIndex) {
                return json.substring(startIndex, endIndex).trim();
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return null;
    }
    
    /**
     * Parse color string to integer
     */
    private int parseColor(String colorStr) {
        try {
            if (colorStr == null || colorStr.isEmpty() || colorStr.equals("null")) {
                return 0;
            }
            
            colorStr = colorStr.trim();
            
            // Handle hex colors
            if (colorStr.startsWith("#")) {
                return Color.parseColor(colorStr);
            }
            
            // Handle rgb/rgba colors
            if (colorStr.startsWith("rgb")) {
                String[] values = colorStr.replaceAll("[rgba() ]", "").split(",");
                if (values.length >= 3) {
                    int r = Math.min(255, Math.max(0, Integer.parseInt(values[0].trim())));
                    int g = Math.min(255, Math.max(0, Integer.parseInt(values[1].trim())));
                    int b = Math.min(255, Math.max(0, Integer.parseInt(values[2].trim())));
                    return Color.rgb(r, g, b);
                }
            }
            
            // Handle named colors (basic set)
            switch (colorStr.toLowerCase()) {
                case "white": return Color.WHITE;
                case "black": return Color.BLACK;
                case "red": return Color.RED;
                case "blue": return Color.BLUE;
                case "green": return Color.GREEN;
                case "yellow": return Color.YELLOW;
                case "cyan": return Color.CYAN;
                case "magenta": return Color.MAGENTA;
                default: return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Update status bar color and text color
     */
    public void updateStatusBarColor(int color, boolean lightStatusBar) {
        android.util.Log.d("ThemeManager", String.format("Updating status bar color to #%08X, light text: %b", color, lightStatusBar));
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                Window window = activity.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
                
                android.util.Log.d("ThemeManager", "Status bar color set successfully");
                
                // Set status bar text color
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    View decorView = window.getDecorView();
                    int systemUiVisibility = decorView.getSystemUiVisibility();
                    
                    if (lightStatusBar) {
                        // Dark text on light background
                        systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        android.util.Log.d("ThemeManager", "Setting dark status bar text");
                    } else {
                        // Light text on dark background
                        systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        android.util.Log.d("ThemeManager", "Setting light status bar text");
                    }
                    
                    decorView.setSystemUiVisibility(systemUiVisibility);
                    
                    // Also try with WindowInsetsController if available
                    if (windowInsetsController != null) {
                        windowInsetsController.setAppearanceLightStatusBars(lightStatusBar);
                    }
                } else {
                    android.util.Log.d("ThemeManager", "Status bar text color not supported on this API level");
                }
                
            } catch (Exception e) {
                android.util.Log.e("ThemeManager", "Error updating status bar color", e);
            }
        } else {
            android.util.Log.d("ThemeManager", "Status bar color not supported on this API level");
        }
    }
    
    /**
     * Adapt to system theme
     */
    public void adaptToSystemTheme() {
        boolean isDarkMode = isSystemInDarkMode();
        activity.runOnUiThread(() -> {
            if (isDarkMode) {
                updateStatusBarColor(DEFAULT_DARK_STATUS_BAR, false);
            } else {
                updateStatusBarColor(DEFAULT_LIGHT_STATUS_BAR, true);
            }
        });
    }
    
    /**
     * Check if system is in dark mode
     */
    private boolean isSystemInDarkMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            int nightModeFlags = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }
        return false;
    }
    
    /**
     * Handle configuration changes (day/night mode)
     */
    public void onConfigurationChanged(Configuration newConfig) {
        if (!AppConfig.isAutoThemeAdaptationEnabled()) return;
        
        // Re-detect theme after configuration change
        webView.post(() -> adaptThemeFromWebsite());
    }
    
    /**
     * Test method to manually trigger theme adaptation with a specific color
     */
    public void testThemeColor(String color) {
        android.util.Log.d("ThemeManager", "Testing theme color: " + color);
        int parsedColor = parseColor(color);
        if (parsedColor != 0) {
            updateStatusBarColor(parsedColor, false);
        } else {
            android.util.Log.e("ThemeManager", "Failed to parse test color: " + color);
        }
    }
    
    /**
     * Force theme detection (for debugging)
     */
    public void forceThemeDetection() {
        android.util.Log.d("ThemeManager", "Forcing theme detection...");
        webView.post(() -> adaptThemeFromWebsite());
    }

    /**
     * Apply theme from a JS bridge callback.
     *
     * @param isDark        true if the page is in dark mode
     * @param statusBarColor the resolved status-bar color from the page (hex, rgb, or null)
     *
     * Uses statusBarColor when provided (meta theme-color, background, etc.).
     * Falls back to plain black/white when the page provides no color.
     * Also drives AppCompatDelegate so DayNight resources (dialogs, error screen)
     * match — only when the mode actually changes to avoid unnecessary recreation.
     */
    public void applyTheme(boolean isDark, String statusBarColor) {
        // 1. Status bar color
        int color = (statusBarColor != null && !statusBarColor.isEmpty())
                ? parseColor(statusBarColor) : 0;
        if (color != 0) {
            // Use the page's actual color; derive icon tint from its brightness
            updateStatusBarColor(color, !isDark);
        } else {
            updateStatusBarColor(
                isDark ? DEFAULT_DARK_STATUS_BAR : DEFAULT_LIGHT_STATUS_BAR,
                !isDark
            );
        }

        // 2. DayNight mode — only flip when it actually changes
        if (lastAppliedDark == null || lastAppliedDark != isDark) {
            lastAppliedDark = isDark;
            int nightMode = isDark
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO;
            activity.runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(nightMode));
        }
    }

    /**
     * Inject a universal theme observer that works on ANY website.
     *
     * Detection priority (first match wins):
     *  1. <meta name="theme-color"> — explicit PWA/site brand color (Google, Twitter, etc.)
     *  2. data-theme / data-color-scheme / color-scheme attributes on <html> or <body>
     *  3. Framework dark-mode class on <html> or <body>
     *     (Tailwind: "dark", Bootstrap: "dark", MUI: "dark", etc.)
     *  4. Computed background color of <html> / <body> — brightness heuristic
     *  5. prefers-color-scheme media query — OS-level fallback
     *
     * A MutationObserver watches <html> attributes + <head> child-list changes
     * (for dynamic <meta theme-color> swaps), so any in-page toggle is caught
     * immediately without polling.
     */
    public void injectThemeObserver() {
        // language=JavaScript
        String script =
            "(function() {" +
            "  if (window.__androidThemeObserverInstalled) return;" +
            "  window.__androidThemeObserverInstalled = true;" +

            // ── helpers ──────────────────────────────────────────────────────
            "  function brightness(r, g, b) {" +
            "    return (r * 299 + g * 587 + b * 114) / 1000;" +
            "  }" +

            "  function parseRgb(css) {" +
            "    var m = css.match(/\\d+/g);" +
            "    return m && m.length >= 3 ? [+m[0], +m[1], +m[2]] : null;" +
            "  }" +

            "  function isTransparent(css) {" +
            "    return !css || css === 'transparent' || css === 'rgba(0, 0, 0, 0)';" +
            "  }" +

            // ── main detector ────────────────────────────────────────────────
            "  function detect() {" +
            "    var html = document.documentElement;" +
            "    var body = document.body;" +
            "    var isDark = null;" +
            "    var statusColor = null;" +

            // 1. <meta name="theme-color"> — highest authority, use its color directly
            "    var meta = document.querySelector('meta[name=\"theme-color\"]');" +
            "    if (meta && meta.content && meta.content.trim()) {" +
            "      statusColor = meta.content.trim();" +
            "      var tmp = document.createElement('div');" +
            "      tmp.style.color = statusColor;" +
            "      document.body && document.body.appendChild(tmp);" +
            "      var mc = parseRgb(window.getComputedStyle(tmp).color);" +
            "      document.body && document.body.removeChild(tmp);" +
            "      if (mc) isDark = brightness(mc[0], mc[1], mc[2]) < 128;" +
            "    }" +

            // 2. data-theme / data-color-scheme / color-scheme / data-mode attributes
            "    if (isDark === null) {" +
            "      var els = [html, body];" +
            "      var attrs = ['data-theme','data-color-scheme','color-scheme','data-mode'];" +
            "      outer2: for (var ei = 0; ei < els.length; ei++) {" +
            "        if (!els[ei]) continue;" +
            "        for (var ai = 0; ai < attrs.length; ai++) {" +
            "          var val = (els[ei].getAttribute(attrs[ai]) || '').toLowerCase();" +
            "          if (val.indexOf('dark') !== -1 || val === 'night') { isDark = true; break outer2; }" +
            "          if (val.indexOf('light') !== -1) { isDark = false; break outer2; }" +
            "        }" +
            "      }" +
            "    }" +

            // 3. Framework class names on <html> or <body>
            "    if (isDark === null) {" +
            "      var darkC = ['dark','dark-mode','dark-theme','night-mode','theme-dark','bp4-dark','chakra-ui-dark'];" +
            "      var lightC = ['light','light-mode','light-theme','theme-light'];" +
            "      var cls = ((html.className||'') + ' ' + (body ? body.className||'' : '')).toLowerCase();" +
            "      for (var di = 0; di < darkC.length; di++) { if (cls.indexOf(darkC[di]) !== -1) { isDark = true; break; } }" +
            "      if (isDark === null) { for (var li = 0; li < lightC.length; li++) { if (cls.indexOf(lightC[li]) !== -1) { isDark = false; break; } } }" +
            "    }" +

            // 4. Computed background-color — use as statusColor AND derive isDark
            "    if (isDark === null || statusColor === null) {" +
            "      var targets = [html, body];" +
            "      for (var ti = 0; ti < targets.length; ti++) {" +
            "        if (!targets[ti]) continue;" +
            "        var bg = window.getComputedStyle(targets[ti]).backgroundColor;" +
            "        if (!isTransparent(bg)) {" +
            "          var rgb3 = parseRgb(bg);" +
            "          if (rgb3) {" +
            "            if (statusColor === null) statusColor = bg;" +
            "            if (isDark === null) isDark = brightness(rgb3[0], rgb3[1], rgb3[2]) < 128;" +
            "            break;" +
            "          }" +
            "        }" +
            "      }" +
            "    }" +

            // 5. OS prefers-color-scheme fallback
            "    if (isDark === null) isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;" +

            "    if (typeof AndroidBridge !== 'undefined') {" +
            "      AndroidBridge.onThemeChanged(!!isDark, statusColor || '');" +
            "    }" +
            "  }" +

            // ── observers ────────────────────────────────────────────────────
            // Watch <html> attribute mutations (class, data-theme, color-scheme, etc.)
            "  var htmlObserver = new MutationObserver(function() { detect(); });" +
            "  htmlObserver.observe(document.documentElement, {" +
            "    attributes: true," +
            "    attributeFilter: ['class','data-theme','data-color-scheme','color-scheme','data-mode','style']" +
            "  });" +

            // Watch <head> child-list for dynamic <meta theme-color> injection/removal
            "  if (document.head) {" +
            "    var headObserver = new MutationObserver(function(muts) {" +
            "      for (var i = 0; i < muts.length; i++) {" +
            "        var nodes = muts[i].addedNodes;" +
            "        for (var j = 0; j < nodes.length; j++) {" +
            "          if (nodes[j].nodeName === 'META' && nodes[j].name === 'theme-color') {" +
            "            detect(); return;" +
            "          }" +
            "        }" +
            "      }" +
            "    });" +
            "    headObserver.observe(document.head, { childList: true });" +
            "  }" +

            // Watch existing <meta theme-color> attribute changes
            "  var metaEl = document.querySelector('meta[name=\"theme-color\"]');" +
            "  if (metaEl) {" +
            "    var metaObserver = new MutationObserver(function() { detect(); });" +
            "    metaObserver.observe(metaEl, { attributes: true, attributeFilter: ['content'] });" +
            "  }" +

            // OS dark/light switch
            "  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', detect);" +

            // Fire immediately on inject
            "  detect();" +
            "})();";

        webView.evaluateJavascript(script, null);
    }
}