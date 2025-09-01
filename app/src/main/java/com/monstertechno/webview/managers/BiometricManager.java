package com.monstertechno.webview.managers;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

public class BiometricManager {
    
    private Context context;
    
    public BiometricManager(Context context) {
        this.context = context;
    }
    
    public boolean isBiometricAvailable() {
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(context);
        
        switch (biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL)) {
            case androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
            case androidx.biometric.BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
            default:
                return false;
        }
    }
    
    public void authenticate(FragmentActivity activity, 
                           AuthenticationCallback onSuccess, 
                           AuthenticationErrorCallback onError) {
        
        if (!isBiometricAvailable()) {
            onError.onError("Biometric authentication is not available on this device");
            return;
        }
        
        Executor executor = ContextCompat.getMainExecutor(context);
        
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, 
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    onError.onError(errString.toString());
                }
                
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    onSuccess.onSuccess();
                }
                
                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    onError.onError("Authentication failed. Please try again.");
                }
            });
        
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Use your fingerprint or face to authenticate")
            .setDescription("Place your finger on the sensor or look at the camera")
            .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL)
            .build();
        
        // For devices running Android 10 and below, we need to provide a negative button
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Use your fingerprint to authenticate")
                .setDescription("Place your finger on the sensor")
                .setNegativeButtonText("Cancel")
                .build();
        }
        
        biometricPrompt.authenticate(promptInfo);
    }
    
    public String getBiometricStatus() {
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(context);
        
        switch (biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG | Authenticators.DEVICE_CREDENTIAL)) {
            case androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS:
                return "Biometric authentication is available";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "No biometric features available on this device";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric features are currently unavailable";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "The user hasn't enrolled any biometric credentials";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "A security vulnerability has been discovered";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return "The specified options are incompatible with the current Android version";
            case androidx.biometric.BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                return "Unable to determine whether the user can authenticate";
            default:
                return "Unknown status";
        }
    }
    
    public interface AuthenticationCallback {
        void onSuccess();
    }
    
    public interface AuthenticationErrorCallback {
        void onError(String error);
    }
}