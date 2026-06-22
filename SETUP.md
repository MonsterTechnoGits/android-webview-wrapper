# Setup Guide

Step-by-step walkthrough for turning this template into your own branded Android app.

## 1. Set your website URL

Open [app/build.gradle.kts](app/build.gradle.kts) and edit the `productFlavors` block:

```kotlin
productFlavors {
    create("dev") {
        buildConfigField("String", "TARGET_WEBSITE_URL", "\"https://dev.yoursite.com\"")
    }
    create("staging") {
        buildConfigField("String", "TARGET_WEBSITE_URL", "\"https://staging.yoursite.com\"")
    }
    create("prod") {
        buildConfigField("String", "TARGET_WEBSITE_URL", "\"https://yoursite.com\"")
    }
}
```

If you don't need multiple environments, just edit the `prod` URL and always build with `*ProdRelease` / `*ProdDebug` variants. [AppConfig.java](app/src/main/java/com/monstertechno/webview/config/AppConfig.java) reads `BuildConfig.TARGET_WEBSITE_URL` automatically — no other file needs to change.

```bash
./gradlew assembleProdDebug   # debug build
./gradlew assembleProdRelease # release build (see signing below)
```

## 2. Rename the package (applicationId)

The default package is `com.monstertechno.webview`. Rename it to your own reverse-domain name with the provided script:

```bash
./scripts/rename-package.sh com.yourcompany.yourapp
```

This moves the Java source directories, rewrites all `package`/`import` statements, and updates `namespace`/`applicationId` in `app/build.gradle.kts`. Review with `git diff`, then rebuild to confirm:

```bash
./gradlew assembleProdDebug
```

## 3. Set the app name

Edit `app_name` in [app/src/main/res/values/strings.xml](app/src/main/res/values/strings.xml). Also update `APP_NAME` in `AppConfig.java` (used for notifications/dialogs).

## 4. Replace the app icon

Replace the `ic_launcher` / `ic_launcher_round` assets in each `app/src/main/res/mipmap-*` directory, or regenerate them via Android Studio's **Image Asset Studio** (right-click `res` → New → Image Asset) and point it at your logo.

## 5. Customize the splash screen

Edit [app/src/main/res/layout/layout_splash.xml](app/src/main/res/layout/layout_splash.xml) and [app/src/main/res/drawable/bg_splash_circle.xml](app/src/main/res/drawable/bg_splash_circle.xml) for your branding/colors. Splash duration and enable/disable toggle live in `AppConfig.java` (`SHOW_SPLASH_SCREEN`, `SPLASH_DURATION_MS`).

## 6. Set version info

Bump `versionCode` and `versionName` in `app/build.gradle.kts` `defaultConfig` block before every release build.

## 7. Configure release signing

Generate a keystore (one-time):

```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

**Do not commit the keystore or its passwords.** Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "release-keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...existing isMinifyEnabled/proguardFiles
        }
    }
}
```

Set the four env vars (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) locally and in CI secrets — never hardcode them.

## 8. Build the release artifact

```bash
./gradlew bundleProdRelease   # AAB for Play Store
./gradlew assembleProdRelease # APK for direct distribution
```

## 9. Publish to Google Play

1. Create an app entry in [Play Console](https://play.google.com/console)
2. Upload the `.aab` from `app/build/outputs/bundle/prodRelease/`
3. Fill in store listing, screenshots, and privacy policy URL
4. Submit for review

---

For feature toggles (JS bridge, file downloads, biometric auth, notifications), see the comments in [AppConfig.java](app/src/main/java/com/monstertechno/webview/config/AppConfig.java).
