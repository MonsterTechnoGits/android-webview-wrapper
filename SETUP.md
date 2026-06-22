# Setup Guide

Step-by-step walkthrough for turning this template into your own branded Android app, written for **Android Studio** (the terminal/`gradlew` equivalent is included for each step too).

> **Prerequisite:** Open the project in Android Studio first — `File → Open...` and select the `android-webview-wrapper` folder. Wait for Gradle sync to finish (progress bar at the bottom) before doing anything else.

## 1. Set your website URL

In the **Project** panel (left sidebar), switch the view dropdown from "Android" to "Project" if you want to see real file paths, then navigate to:

```
app/src/main/java/com/monstertechno/webview/config/AppConfig.java
```

Or just press **`Shift` twice** (Search Everywhere) and type `AppConfig.java`. Open it and change one line:

```java
public static final String TARGET_WEBSITE_URL = "https://yoursite.com";
```

Save the file (`Cmd+S` / `Ctrl+S`), then run the app:
- Click the green **Run ▶** button in the toolbar, or press `Shift+F10` (Windows/Linux) / `Ctrl+R` (Mac)
- Pick a connected device or emulator when prompted

Terminal equivalent (use the **Terminal** tab at the bottom of Android Studio, or any shell):
```bash
./gradlew assembleDebug
```

## 2. Rename the package (applicationId)

The default package is `com.monstertechno.webview`. Use the bundled script — open the **Terminal** tab at the bottom of Android Studio and run:

```bash
./scripts/rename-package.sh com.yourcompany.yourapp
```

This moves the Java source directories, rewrites all `package`/`import` statements, and updates `namespace`/`applicationId` in `app/build.gradle.kts`.

After it finishes, Android Studio will show a banner saying **"Gradle files have changed... Sync Now"** — click **Sync Now**. Then verify the rename worked:
- Click **Build → Rebuild Project** from the menu bar
- Or run `./gradlew assembleDebug` in the Terminal tab

Review what changed with `git diff` before committing.

## 3. Set the app name

In the **Project** panel, open:
```
app/src/main/res/values/strings.xml
```
and change the `app_name` value. Also update `APP_NAME` in `AppConfig.java` (used for notifications/dialogs).

## 4. Replace the app icon

Right-click the `app/src/main/res` folder in the **Project** panel → **New → Image Asset**. This opens Android Studio's built-in icon generator:
1. Set **Icon Type** to "Launcher Icons (Adaptive and Legacy)"
2. Under **Foreground Layer**, click the image icon and select your logo file
3. Adjust padding/scaling in the preview
4. Click **Next**, then **Finish** — Android Studio generates all the `mipmap-*` density folders automatically

## 5. Customize the splash screen

Open these two files from the **Project** panel and edit colors/branding:
```
app/src/main/res/layout/layout_splash.xml
app/src/main/res/drawable/bg_splash_circle.xml
```
Use the **Split** or **Design** view (tabs in the top-right of the editor) to preview layout changes visually. Splash duration and the enable/disable toggle live in `AppConfig.java` (`SHOW_SPLASH_SCREEN`, `SPLASH_DURATION_MS`).

## 6. Set version info

Open `app/build.gradle.kts` and bump `versionCode` and `versionName` in the `defaultConfig` block before every release build.

## 7. Configure release signing

Android Studio has a built-in wizard for this — no need to use `keytool` manually:

1. Menu bar → **Build → Generate Signed Bundle / APK...**
2. Choose **Android App Bundle** (recommended for Play Store) or **APK**
3. Click **Next**, then **Create new...** under Key store path
4. Fill in the keystore path, passwords, and certificate info — Android Studio creates the `.jks` file for you
5. **Do not commit this keystore file or its passwords to git.** Store it somewhere safe (password manager, secrets vault) — if you lose it, you can never update your published app again.
6. Click **Next**, select **release** as the build variant, and **Finish**

To repeat this build without the wizard later (e.g. in CI), see the terminal equivalent: add a `signingConfigs` block to `app/build.gradle.kts` reading from environment variables —

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

then set the four env vars (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) locally and in your CI secrets — never hardcode them.

## 8. Build the release artifact

If you used the **Generate Signed Bundle/APK** wizard in Step 7, it already produced your `.aab`/`.apk` — Android Studio shows a notification with a **locate** link when the build finishes.

To build again later:
- Menu bar → **Build → Generate Signed Bundle / APK...** (re-run the wizard, it remembers your keystore)
- Or in the Terminal tab:
```bash
./gradlew bundleRelease   # AAB for Play Store
./gradlew assembleRelease # APK for direct distribution
```

## 9. Publish to Google Play

1. Create an app entry in [Play Console](https://play.google.com/console)
2. Upload the `.aab` from `app/build/outputs/bundle/release/`
3. Fill in store listing, screenshots, and privacy policy URL
4. Submit for review

---

For feature toggles (JS bridge, file downloads, biometric auth, notifications), see the comments in [AppConfig.java](app/src/main/java/com/monstertechno/webview/config/AppConfig.java).

## Common Android Studio gotchas

- **"Gradle sync failed"** after pulling changes or editing `build.gradle.kts` → click the **Sync Now** banner, or **File → Sync Project with Gradle Files**.
- **Stale build after renaming the package** → **Build → Clean Project**, then **Build → Rebuild Project**.
- **Emulator not showing up in the device dropdown** → open **Device Manager** (phone icon in the toolbar) and start/create a virtual device first.
- **Can't find a file mentioned here** → press `Shift` twice (Search Everywhere) and type the filename.
