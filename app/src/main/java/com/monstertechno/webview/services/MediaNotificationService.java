package com.monstertechno.webview.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.monstertechno.webview.R;
import com.monstertechno.webview.ui.MainActivity;
import com.monstertechno.webview.utils.NotificationChannels;

public class MediaNotificationService extends Service {
    
    private static final int FOREGROUND_ID = 1002;
    private final IBinder binder = new MediaBinder();
    
    private String currentTitle = "";
    private String currentArtist = "";
    private Bitmap currentAlbumArt;
    private boolean isPlaying = false;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    
    public class MediaBinder extends Binder {
        public MediaNotificationService getService() {
            return MediaNotificationService.this;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getStringExtra("action");
        
        if ("PLAY_PAUSE".equals(action)) {
            togglePlayPause();
        } else if ("UPDATE_MEDIA".equals(action)) {
            currentTitle = intent.getStringExtra("title");
            currentArtist = intent.getStringExtra("artist");
            isPlaying = intent.getBooleanExtra("isPlaying", false);
            
            if (isPlaying) {
                requestAudioFocus();
                startForeground(FOREGROUND_ID, createMediaNotification());
            } else {
                abandonAudioFocus();
                stopForeground(false);
            }
            
            updateNotification();
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
        abandonAudioFocus();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NotificationChannels.MEDIA_CHANNEL_ID,
                "Media Playback",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Media playback controls");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createMediaNotification() {
        Intent playPauseIntent = new Intent(this, MediaNotificationService.class);
        playPauseIntent.putExtra("action", "PLAY_PAUSE");
        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 0, playPauseIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : 
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : 
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationChannels.MEDIA_CHANNEL_ID)
            .setSmallIcon(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play)
            .setContentTitle(currentTitle.isEmpty() ? "Web Media" : currentTitle)
            .setContentText(currentArtist.isEmpty() ? "Playing from WebView" : currentArtist)
            .setContentIntent(pendingIntent)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setOngoing(isPlaying);
        
        if (currentAlbumArt != null) {
            builder.setLargeIcon(currentAlbumArt);
        }
        
        // Add play/pause action
        builder.addAction(
            isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
            isPlaying ? "Pause" : "Play",
            playPausePendingIntent
        );
        
        // Media style for better media controls
        // TODO: Add proper MediaStyle when media library is properly configured
        
        return builder.build();
    }
    
    public void updateMediaInfo(String title, String artist, Bitmap albumArt, boolean playing) {
        this.currentTitle = title;
        this.currentArtist = artist;
        this.currentAlbumArt = albumArt;
        this.isPlaying = playing;
        
        if (playing) {
            requestAudioFocus();
            startForeground(FOREGROUND_ID, createMediaNotification());
        } else {
            abandonAudioFocus();
            updateNotification();
        }
    }
    
    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(FOREGROUND_ID, createMediaNotification());
        }
    }
    
    private void togglePlayPause() {
        isPlaying = !isPlaying;
        
        // Send command to WebView to play/pause
        Intent intent = new Intent("com.monstertechno.webview.MEDIA_CONTROL");
        intent.putExtra("action", isPlaying ? "play" : "pause");
        sendBroadcast(intent);
        
        updateNotification();
    }
    
    private void requestAudioFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
                
                audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this::onAudioFocusChange)
                    .build();
                
                audioManager.requestAudioFocus(audioFocusRequest);
            } else {
                audioManager.requestAudioFocus(this::onAudioFocusChange, 
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }
    
    private void abandonAudioFocus() {
        if (audioManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.abandonAudioFocusRequest(audioFocusRequest);
            } else {
                audioManager.abandonAudioFocus(this::onAudioFocusChange);
            }
        }
    }
    
    private void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // Resume playback or increase volume
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Stop playback
                if (isPlaying) {
                    togglePlayPause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Pause playback
                if (isPlaying) {
                    togglePlayPause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lower volume
                break;
        }
    }
}