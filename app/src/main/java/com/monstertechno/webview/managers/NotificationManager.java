package com.monstertechno.webview.managers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.monstertechno.webview.R;
import com.monstertechno.webview.ui.MainActivity;
import com.monstertechno.webview.utils.NotificationChannels;

public class NotificationManager {
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    public NotificationManager(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
    }
    
    public void showWebNotification(String title, String message, String iconUrl) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : 
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannels.BROWSER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(new long[]{0, 250, 250, 250});
        
        if (iconUrl != null && !iconUrl.isEmpty()) {
            loadNotificationIcon(iconUrl, builder, NotificationChannels.BROWSER_NOTIFICATION_ID);
        } else {
            showNotification(builder.build(), NotificationChannels.BROWSER_NOTIFICATION_ID);
        }
    }
    
    public void showDownloadNotification(String filename, int progress, boolean completed) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : 
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannels.DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(completed ? "Download Complete" : "Downloading")
            .setContentText(filename)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true);
        
        if (!completed) {
            builder.setProgress(100, progress, false)
                   .setOngoing(true);
        } else {
            builder.setProgress(0, 0, false)
                   .setAutoCancel(true)
                   .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }
        
        showNotification(builder.build(), NotificationChannels.DOWNLOAD_NOTIFICATION_ID);
    }
    
    public void showMediaNotification(String title, String artist, Bitmap albumArt, 
                                    boolean isPlaying, PendingIntent playPauseIntent) {
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationChannels.MEDIA_CHANNEL_ID)
            .setSmallIcon(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play)
            .setContentTitle(title)
            .setContentText(artist)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(isPlaying)
            .setShowWhen(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT);
        
        if (albumArt != null) {
            builder.setLargeIcon(albumArt);
        }
        
        // Media controls
        if (playPauseIntent != null) {
            builder.addAction(
                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                isPlaying ? "Pause" : "Play",
                playPauseIntent
            );
        }
        
        // Media style - simplified for now
        // TODO: Add proper MediaStyle when media library is properly configured
        
        showNotification(builder.build(), NotificationChannels.MEDIA_NOTIFICATION_ID);
    }
    
    private void loadNotificationIcon(String iconUrl, NotificationCompat.Builder builder, int notificationId) {
        Glide.with(context)
            .asBitmap()
            .load(iconUrl)
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    builder.setLargeIcon(resource);
                    showNotification(builder.build(), notificationId);
                }
                
                @Override
                public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                    showNotification(builder.build(), notificationId);
                }
                
                @Override
                public void onLoadFailed(android.graphics.drawable.Drawable errorDrawable) {
                    showNotification(builder.build(), notificationId);
                }
            });
    }
    
    private void showNotification(Notification notification, int notificationId) {
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(notificationId, notification);
        }
    }
    
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }
    
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
    
    public boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }
}