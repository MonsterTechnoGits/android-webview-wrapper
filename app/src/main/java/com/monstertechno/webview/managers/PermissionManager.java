package com.monstertechno.webview.managers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_AUDIO_PERMISSION = 1002;
    private static final int REQUEST_LOCATION_PERMISSION = 1003;
    private static final int REQUEST_STORAGE_PERMISSION = 1004;
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1005;
    
    private Context context;
    private PermissionCallback cameraCallback;
    private PermissionCallback audioCallback;
    private PermissionCallback locationCallback;
    private PermissionCallback storageCallback;
    private PermissionCallback notificationCallback;
    
    public PermissionManager(Context context) {
        this.context = context;
    }
    
    public void requestCameraPermission(PermissionCallback onGranted, PermissionCallback onDenied) {
        this.cameraCallback = onGranted;
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            onGranted.onResult();
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, 
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                onDenied.onResult();
            }
        }
    }
    
    public void requestAudioPermission(PermissionCallback onGranted, PermissionCallback onDenied) {
        this.audioCallback = onGranted;
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
            == PackageManager.PERMISSION_GRANTED) {
            onGranted.onResult();
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, 
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_PERMISSION);
            } else {
                onDenied.onResult();
            }
        }
    }
    
    public void requestLocationPermission(PermissionCallback onGranted, PermissionCallback onDenied) {
        this.locationCallback = onGranted;
        
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        };
        
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED) {
            onGranted.onResult();
        } else {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context, 
                    permissions, REQUEST_LOCATION_PERMISSION);
            } else {
                onDenied.onResult();
            }
        }
    }
    
    public void requestStoragePermission(PermissionCallback onGranted, PermissionCallback onDenied) {
        this.storageCallback = onGranted;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            String[] permissions = {
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            };
            
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) 
                == PackageManager.PERMISSION_GRANTED) {
                onGranted.onResult();
            } else {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, 
                        permissions, REQUEST_STORAGE_PERMISSION);
                } else {
                    onDenied.onResult();
                }
            }
        } else {
            String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
                onGranted.onResult();
            } else {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, 
                        permissions, REQUEST_STORAGE_PERMISSION);
                } else {
                    onDenied.onResult();
                }
            }
        }
    }
    
    public void requestNotificationPermission(PermissionCallback onGranted, PermissionCallback onDenied) {
        this.notificationCallback = onGranted;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                == PackageManager.PERMISSION_GRANTED) {
                onGranted.onResult();
            } else {
                if (context instanceof Activity) {
                    ActivityCompat.requestPermissions((Activity) context, 
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
                } else {
                    onDenied.onResult();
                }
            }
        } else {
            onGranted.onResult(); // No permission needed for older versions
        }
    }
    
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (cameraCallback != null) {
                    if (granted) {
                        cameraCallback.onResult();
                    }
                    cameraCallback = null;
                }
                break;
                
            case REQUEST_AUDIO_PERMISSION:
                if (audioCallback != null) {
                    if (granted) {
                        audioCallback.onResult();
                    }
                    audioCallback = null;
                }
                break;
                
            case REQUEST_LOCATION_PERMISSION:
                if (locationCallback != null) {
                    if (granted) {
                        locationCallback.onResult();
                    }
                    locationCallback = null;
                }
                break;
                
            case REQUEST_STORAGE_PERMISSION:
                if (storageCallback != null) {
                    if (granted) {
                        storageCallback.onResult();
                    }
                    storageCallback = null;
                }
                break;
                
            case REQUEST_NOTIFICATION_PERMISSION:
                if (notificationCallback != null) {
                    if (granted) {
                        notificationCallback.onResult();
                    }
                    notificationCallback = null;
                }
                break;
        }
    }
    
    public boolean hasAllPermissions() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
                == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    public interface PermissionCallback {
        void onResult();
    }
}