package com.monstertechno.webview.config;

public class AppConfig {
    
    // ========================================
    // CONFIGURATION - Update these values for your app
    // ========================================
    
    /**
     * The main website URL that this app will wrap
     * ONLY UPDATE THIS URL - Everything else will auto-generate!
     * 
     * Examples:
     * - "https://www.google.com" -> generates google.com, www.google.com, m.google.com, etc.
     * - "https://github.com" -> generates github.com, www.github.com, m.github.com, etc.
     * - "https://example.com" -> generates example.com, www.example.com, m.example.com, etc.
     * 
     * For LOCAL HTML files in assets folder:
     * - "file:///android_asset/index.html" -> loads app/src/main/assets/index.html
     * - "file:///android_asset/theme-demo.html" -> loads app/src/main/assets/theme-demo.html
     * - "file:///android_asset/app/index.html" -> loads app/src/main/assets/app/index.html
     */
    public static final String TARGET_WEBSITE_URL = "file:///android_asset/index.html";
    
    // ========================================
    // AUTO-GENERATED - DO NOT MODIFY BELOW
    // These values are automatically generated from TARGET_WEBSITE_URL
    // ========================================
    
    /**
     * Auto-generated from TARGET_WEBSITE_URL
     */
    public static final String TARGET_WEBSITE_HOST = extractHost(TARGET_WEBSITE_URL);
    
    /**
     * Auto-generated from TARGET_WEBSITE_URL
     * Includes common subdomains that should stay in WebView
     * 
     * For "https://www.example.com", this generates:
     * - example.com, www.example.com, m.example.com
     * - mobile.example.com, api.example.com, cdn.example.com
     * - static.example.com, assets.example.com, app.example.com, secure.example.com
     */
    public static final String[] ALLOWED_HOSTS = generateAllowedHosts(TARGET_WEBSITE_URL);
    
    /**
     * Enable or disable JavaScript bridge
     * Set to false if you don't need native-web communication
     */
    public static final boolean ENABLE_JAVASCRIPT_BRIDGE = true;
    
    /**
     * Enable or disable file downloads
     */
    public static final boolean ENABLE_FILE_DOWNLOADS = true;
    
    /**
     * Enable or disable biometric authentication features
     */
    public static final boolean ENABLE_BIOMETRIC_AUTH = true;
    
    /**
     * Enable or disable media notifications
     */
    public static final boolean ENABLE_MEDIA_NOTIFICATIONS = true;
    
    /**
     * Enable or disable browser notifications from the website
     */
    public static final boolean ENABLE_BROWSER_NOTIFICATIONS = true;
    
    /**
     * Enable automatic status bar theme adaptation from website
     * When enabled, the app will automatically detect website's theme colors
     * and adapt the status bar color accordingly (dark/light mode + theme colors)
     */
    public static final boolean ENABLE_AUTO_THEME_ADAPTATION = true;
    
    /**
     * Show splash screen on app start
     */
    public static final boolean SHOW_SPLASH_SCREEN = true;
    
    /**
     * Splash screen duration in milliseconds
     */
    public static final int SPLASH_DURATION_MS = 2000;
    
    /**
     * App name for notifications and dialogs
     */
    public static final String APP_NAME = "WebView App";
    
    /**
     * User agent suffix to identify the app
     */
    public static final String USER_AGENT_SUFFIX = " WebViewApp/1.0";
    
    // ========================================
    // AUTO-GENERATION HELPER METHODS
    // ========================================
    
    /**
     * Extract host from URL for auto-generation
     */
    private static String extractHost(String url) {
        try {
            // Handle asset URLs (local files)
            if (url.startsWith("file:///android_asset/")) {
                return "localhost"; // Treat assets as localhost
            }
            
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getHost();
        } catch (Exception e) {
            // Handle asset URLs in fallback
            if (url.startsWith("file:///android_asset/")) {
                return "localhost";
            }
            
            // Fallback: try to extract manually
            String cleanUrl = url.replace("https://", "").replace("http://", "");
            int slashIndex = cleanUrl.indexOf('/');
            if (slashIndex > 0) {
                cleanUrl = cleanUrl.substring(0, slashIndex);
            }
            return cleanUrl;
        }
    }
    
    /**
     * Generate allowed hosts array from main URL
     */
    private static String[] generateAllowedHosts(String url) {
        String host = extractHost(url);
        if (host == null || host.isEmpty()) {
            return new String[]{"localhost"};
        }
        
        // Handle asset URLs (local files)
        if (url.startsWith("file:///android_asset/") || host.equals("localhost")) {
            return new String[]{
                "localhost",               // For asset files
                "127.0.0.1",              // Local IP
                "file",                   // File protocol
                "android_asset"           // Asset protocol
            };
        }
        
        // Extract base domain (remove www if present)
        String baseDomain = host.startsWith("www.") ? host.substring(4) : host;
        
        // Generate common subdomain variations
        return new String[]{
            baseDomain,                    // example.com
            "www." + baseDomain,          // www.example.com  
            "m." + baseDomain,            // m.example.com
            "mobile." + baseDomain,       // mobile.example.com
            "api." + baseDomain,          // api.example.com
            "cdn." + baseDomain,          // cdn.example.com
            "static." + baseDomain,       // static.example.com
            "assets." + baseDomain,       // assets.example.com
            "app." + baseDomain,          // app.example.com
            "secure." + baseDomain        // secure.example.com
        };
    }
    
    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Check if a URL belongs to the target website
     */
    public static boolean isAllowedHost(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        
        // Handle asset URLs (local files)
        if (url.startsWith("file:///android_asset/")) {
            return TARGET_WEBSITE_URL.startsWith("file:///android_asset/");
        }
        
        try {
            java.net.URL urlObj = new java.net.URL(url);
            String host = urlObj.getHost();
            
            if (host == null) {
                // For file URLs without host
                if (url.startsWith("file://") && TARGET_WEBSITE_URL.startsWith("file://")) {
                    return true;
                }
                return false;
            }
            
            // Check exact match first
            if (host.equals(TARGET_WEBSITE_HOST)) {
                return true;
            }
            
            // Check against allowed hosts
            for (String allowedHost : ALLOWED_HOSTS) {
                if (host.equals(allowedHost) || host.endsWith("." + allowedHost)) {
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            // Fallback for asset URLs
            if (url.startsWith("file:///android_asset/")) {
                return TARGET_WEBSITE_URL.startsWith("file:///android_asset/");
            }
            return false;
        }
    }
    
    /**
     * Check if a URL is external (should open in Chrome Custom Tabs)
     */
    public static boolean isExternalUrl(String url) {
        return !isAllowedHost(url);
    }
    
    /**
     * Get the main website URL
     */
    public static String getMainUrl() {
        return TARGET_WEBSITE_URL;
    }
    
    /**
     * Check if JavaScript bridge should be enabled
     */
    public static boolean isJavaScriptBridgeEnabled() {
        return ENABLE_JAVASCRIPT_BRIDGE;
    }
    
    /**
     * Check if file downloads should be enabled
     */
    public static boolean isFileDownloadsEnabled() {
        return ENABLE_FILE_DOWNLOADS;
    }
    
    /**
     * Check if biometric authentication should be enabled
     */
    public static boolean isBiometricAuthEnabled() {
        return ENABLE_BIOMETRIC_AUTH;
    }
    
    /**
     * Check if media notifications should be enabled
     */
    public static boolean isMediaNotificationsEnabled() {
        return ENABLE_MEDIA_NOTIFICATIONS;
    }
    
    /**
     * Check if browser notifications should be enabled
     */
    public static boolean isBrowserNotificationsEnabled() {
        return ENABLE_BROWSER_NOTIFICATIONS;
    }
    
    /**
     * Check if auto theme adaptation should be enabled
     */
    public static boolean isAutoThemeAdaptationEnabled() {
        return ENABLE_AUTO_THEME_ADAPTATION;
    }
}