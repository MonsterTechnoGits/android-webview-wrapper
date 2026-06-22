plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.monstertechno.webview"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.monstertechno.webview"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // WebView and modern web features
    implementation(libs.webkit)

    // Custom tabs for external links
    implementation("androidx.browser:browser:1.8.0")

    // Biometric authentication
    implementation(libs.biometric)

    // Background work and notifications
    implementation(libs.work.runtime)
    implementation("androidx.startup:startup-runtime:1.1.1")

    // Network and JSON
    implementation(libs.gson)

    // Image loading
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}