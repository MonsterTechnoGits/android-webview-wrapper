package com.monstertechno.webview.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.monstertechno.webview.services.MediaNotificationService;

public class NotificationReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if ("com.monstertechno.webview.MEDIA_PLAY_PAUSE".equals(action)) {
            Intent serviceIntent = new Intent(context, MediaNotificationService.class);
            serviceIntent.putExtra("action", "PLAY_PAUSE");
            context.startService(serviceIntent);
        }
    }
}