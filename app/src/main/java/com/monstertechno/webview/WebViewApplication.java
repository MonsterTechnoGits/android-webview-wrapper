package com.monstertechno.webview;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.monstertechno.webview.core.WebViewManager;
import com.monstertechno.webview.utils.NotificationChannels;

public class WebViewApplication extends Application implements Configuration.Provider {

    private static WebViewApplication instance;

    public static WebViewApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        initializeWebView();
        createNotificationChannels();
        initializeWorkManager();
    }

    private void initializeWebView() {
        WebViewManager.initialize(this);
    }

    private void createNotificationChannels() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Download notification channel
        NotificationChannel downloadChannel = new NotificationChannel(
            NotificationChannels.DOWNLOAD_CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        downloadChannel.setDescription("Notifications for file downloads");
        downloadChannel.enableVibration(true);
        downloadChannel.setShowBadge(true);

        // Media playback channel
        NotificationChannel mediaChannel = new NotificationChannel(
            NotificationChannels.MEDIA_CHANNEL_ID,
            "Media Playback",
            NotificationManager.IMPORTANCE_LOW
        );
        mediaChannel.setDescription("Media playback controls");
        mediaChannel.setShowBadge(false);

        // Browser notifications channel
        NotificationChannel browserChannel = new NotificationChannel(
            NotificationChannels.BROWSER_CHANNEL_ID,
            "Browser Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        );
        browserChannel.setDescription("Web notifications from websites");
        browserChannel.enableVibration(true);
        browserChannel.setShowBadge(true);

        notificationManager.createNotificationChannel(downloadChannel);
        notificationManager.createNotificationChannel(mediaChannel);
        notificationManager.createNotificationChannel(browserChannel);
    }

    private void initializeWorkManager() {
        // WorkManager is automatically initialized by the system
        // Custom configuration is handled by implementing Configuration.Provider
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build();
    }
}