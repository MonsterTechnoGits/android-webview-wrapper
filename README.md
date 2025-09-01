# 📱 WebView Wrapper App

A clean, modern Android WebView wrapper that transforms any website into a native-feeling app. Perfect for wrapping web applications with advanced native features while keeping external links in Chrome Custom Tabs.

## ✨ Key Features

### 🎯 Single App Wrapper
- **Clean Interface** - No browser UI, just your website
- **Host Validation** - Only your domain loads in WebView
- **Custom Tabs** - External links open in Chrome Custom Tabs
- **Splash Screen** - Professional app launch experience
- **Error Handling** - Clean error states with retry functionality

### 🌐 Modern WebView Features
- **2025 Browser Standards** support
- **JavaScript Enabled** with modern APIs
- **Hardware Acceleration** for smooth performance
- **Progress Indicators** during loading
- **Full-Screen Support** for videos and media

### 🔗 JavaScript Bridge APIs
- **Device Information** access
- **Toast Notifications** 
- **Device Vibration** control
- **Clipboard Operations** (copy/paste)
- **App & URL Launching**
- **Feature Detection** (camera, microphone, etc.)

### 🔐 Advanced Security Features
- **Biometric Authentication** (fingerprint support)
- **Permission Management** system
- **Network Security Config** for HTTPS
- **Safe Browsing** enabled

### 📁 File Management
- **File Upload/Download** with progress tracking
- **Background Downloads** using WorkManager
- **Download Notifications** with progress
- **File Sharing** capabilities
- **Scoped Storage** compliance (Android 10+)

### 📱 Device Integration
- **Camera & Microphone** access for WebRTC
- **Geolocation API** support
- **Media Playback Controls** with system notifications
- **Browser Push Notifications**
- **Audio Focus Management**

### 🎨 Modern UI/UX
- **Material Design** interface
- **Bottom Navigation** with floating action button
- **Dark/Light Theme** support (auto)
- **Edge-to-Edge** display
- **Responsive Design**

## 🛠️ Technical Architecture

### Modular Components
- **WebViewManager** - Core WebView configuration
- **JavaScriptBridge** - Native-web communication
- **PermissionManager** - Runtime permission handling
- **FileManager** - File operations and downloads
- **BiometricManager** - Fingerprint authentication
- **NotificationManager** - System notifications
- **Background Services** - Download and media services

### Modern Android Features
- **WorkManager** for background tasks
- **Notification Channels** for categorized notifications
- **Biometric Prompt** for secure authentication
- **FileProvider** for secure file sharing
- **Service Workers** support
- **WebView Debugging** enabled in development

## 📋 Requirements
- **Android 10.0** (API Level 29) or higher
- **Internet Permission** for web browsing
- **Storage Permission** for file downloads
- **Camera/Microphone** permissions for WebRTC
- **Biometric Hardware** for fingerprint authentication (optional)

## ⚙️ Easy Configuration

### 1. Set Your Website URL
Edit `AppConfig.java` to customize for your website:

```java
// Change these values in AppConfig.java
public static final String TARGET_WEBSITE_URL = "https://your-website.com";
public static final String TARGET_WEBSITE_HOST = "your-website.com";
public static final String[] ALLOWED_HOSTS = {
    "your-website.com",
    "www.your-website.com",
    "api.your-website.com"
};
```

### 2. Customize Features
Enable/disable features as needed:

```java
public static final boolean ENABLE_JAVASCRIPT_BRIDGE = true;
public static final boolean ENABLE_FILE_DOWNLOADS = true;
public static final boolean ENABLE_BIOMETRIC_AUTH = true;
public static final boolean SHOW_SPLASH_SCREEN = true;
```

### 3. Build and Deploy
1. Open in Android Studio
2. Update configuration in `AppConfig.java`
3. Build: `./gradlew assembleDebug`
4. Install the APK on your device

## 🧪 Demo Features

The included demo page (`demo.html`) showcases:

- ✅ Device information retrieval
- ✅ App information access
- ✅ Feature detection
- ✅ Toast notifications
- ✅ System notifications
- ✅ Device vibration
- ✅ Biometric authentication
- ✅ File downloads
- ✅ Text sharing
- ✅ Clipboard operations
- ✅ External app launching
- ✅ Media playback controls

## 🔧 Configuration

### WebView Settings
The app configures WebView with:
- JavaScript enabled
- DOM storage enabled
- File access permissions
- Mixed content compatibility
- Safe browsing enabled
- Modern user agent string
- Hardware acceleration

### Security Features
- Network Security Config for HTTPS enforcement
- Biometric authentication for sensitive operations
- Permission-based access to device features
- Secure file sharing with FileProvider

## 📚 JavaScript Bridge API

### Available Methods
```javascript
// Device Information
AndroidBridge.getDeviceInfo()
AndroidBridge.getAppInfo()
AndroidBridge.isFeatureSupported(feature)

// UI Interactions
AndroidBridge.showToast(message)
AndroidBridge.showNotification(title, message, iconUrl)
AndroidBridge.vibrate(milliseconds)

// Authentication
AndroidBridge.authenticateFingerprint(callback)

// File Operations
AndroidBridge.downloadFile(url, filename)
AndroidBridge.shareText(text)
AndroidBridge.shareFile(filePath)

// Clipboard
AndroidBridge.setClipboard(text)
AndroidBridge.getClipboard()

// External Apps
AndroidBridge.openUrl(url)
AndroidBridge.openExternalApp(packageName)
```

## 🏗️ Architecture Benefits

### Modular Design
- **Easy Maintenance**: Each feature is in its own manager class
- **Easy Testing**: Components can be tested independently  
- **Easy Upgrades**: New features can be added without affecting existing code
- **Clean Code**: Separation of concerns with clear interfaces

### Performance Optimizations
- Background downloads don't block UI
- WebView hardware acceleration enabled
- Efficient memory management
- Proper lifecycle handling

### Security Best Practices
- Runtime permission requests
- Secure file storage and sharing
- Network security enforcement
- Biometric authentication integration

## 🤝 Contributing

This project follows modern Android development practices:
- **Java** for maximum compatibility
- **Material Design** components
- **AndroidX** libraries
- **Modular Architecture** for maintainability
- **Clean Code** principles

## 📄 License

This project is built as a demonstration of modern WebView capabilities with advanced browser functionality.

---

**Built with ❤️ using modern Android development practices**