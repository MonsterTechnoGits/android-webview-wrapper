package com.monstertechno.webview.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.monstertechno.webview.R;
import com.monstertechno.webview.ui.MainActivity;
import com.monstertechno.webview.utils.NotificationChannels;

public class DownloadService extends Service {
    
    private static final int FOREGROUND_ID = 1001;
    private final IBinder binder = new DownloadBinder();
    private boolean isRunning = false;
    
    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            startForeground(FOREGROUND_ID, createNotification());
            isRunning = true;
        }
        
        return START_NOT_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NotificationChannels.DOWNLOAD_CHANNEL_ID,
                "Download Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background download service");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : 
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        return new NotificationCompat.Builder(this, NotificationChannels.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("WebView Downloads")
            .setContentText("Managing downloads in background")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }
    
    public void updateProgress(String filename, int progress) {
        Notification notification = new NotificationCompat.Builder(this, NotificationChannels.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading " + filename)
            .setContentText(progress + "% completed")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
        
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(FOREGROUND_ID, notification);
        }
    }
    
    public void downloadCompleted(String filename) {
        Notification notification = new NotificationCompat.Builder(this, NotificationChannels.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText(filename + " downloaded successfully")
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build();
        
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification);
        }
        
        stopSelf();
    }
}