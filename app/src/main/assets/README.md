# Assets Folder

This folder contains HTML files that can be loaded locally in your WebView wrapper app.

## How to Use

1. Place your HTML files in this `app/src/main/assets/` folder
2. Update `TARGET_WEBSITE_URL` in `AppConfig.java`:

```java
// Load local HTML file
public static final String TARGET_WEBSITE_URL = "file:///android_asset/index.html";
```

## Available Files

- **`index.html`** - Simple welcome page with app features
- **`theme-demo.html`** - Interactive theme adaptation demo
- **`README.md`** - This documentation file

## Examples

```java
// Load welcome page
public static final String TARGET_WEBSITE_URL = "file:///android_asset/index.html";

// Load theme demo
public static final String TARGET_WEBSITE_URL = "file:///android_asset/theme-demo.html";

// Load from subfolder
public static final String TARGET_WEBSITE_URL = "file:///android_asset/app/dashboard.html";
```

## Features Supported

When using asset HTML files, you get full WebView features:
- ✅ JavaScript Bridge (`AndroidBridge`)
- ✅ Auto theme adaptation
- ✅ File upload/download
- ✅ Biometric authentication
- ✅ Notifications
- ✅ External link handling (opens in Custom Tabs)

## File Structure

You can organize files in subfolders:
```
assets/
├── index.html
├── theme-demo.html
├── app/
│   ├── dashboard.html
│   └── settings.html
├── css/
│   └── styles.css
└── js/
    └── app.js
```

## External Links

Links to external websites will automatically open in Chrome Custom Tabs:
```html
<!-- Opens in Custom Tabs -->
<a href="https://google.com">Google</a>
<a href="https://github.com">GitHub</a>

<!-- Stays in WebView (if using asset URL as target) -->
<a href="theme-demo.html">Theme Demo</a>
<a href="file:///android_asset/app/dashboard.html">Dashboard</a>
```

## Development Tips

1. **Live Reload**: Rebuild the app after editing HTML files
2. **Debugging**: Enable WebView debugging in developer options
3. **Console**: Use `console.log()` for debugging JavaScript
4. **Theme Colors**: Use `<meta name="theme-color">` for status bar adaptation