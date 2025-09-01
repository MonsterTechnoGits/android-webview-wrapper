package com.monstertechno.webview.managers;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.monstertechno.webview.config.AppConfig;

public class ThemeManager {
    
    private Activity activity;
    private WebView webView;
    private WindowInsetsControllerCompat windowInsetsController;
    
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
     * Setup initial theme based on system settings
     */
    private void setupInitialTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Enable edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
            
            // Set initial status bar color - start with a visible color for testing
            updateStatusBarColor(DEFAULT_ACCENT_COLOR, false);
            
            // Log for debugging
            android.util.Log.d("ThemeManager", "Initial theme setup complete");
        }
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
}