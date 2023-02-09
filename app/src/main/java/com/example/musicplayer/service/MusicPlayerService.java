package com.example.musicplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import com.example.musicplayer.R;
import com.example.musicplayer.receiver.HeadsetReceiver;
import com.example.musicplayer.receiver.PlayerManagerReceiver;
import com.example.musicplayer.util.Constant;

public class MusicPlayerService extends Service {
    private static final String TAG = MusicPlayerService.class.getName();

    public static final String PLAYER_MANAGER_ACTION = "com.example.musicplayer.service.MusicPlayerService.player.action";
    private NotificationManager notificationManager;

    private AudioManager audioManager;

    private PlayerManagerReceiver mReceiver;

    private HeadsetReceiver headsetReceiver;
    private ComponentName name;

    public MusicPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, "onCreate: ");
        register();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy: ");
        unRegister();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }


    private void register() {
        headsetReceiver = new HeadsetReceiver();
        registerReceiver(headsetReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        name = new ComponentName(getPackageName(), HeadsetReceiver.class.getName());
        audioManager.registerMediaButtonEventReceiver(name);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(Constant.CHANNEL_ID, Constant.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        startForeground(Constant.NOTIFICATION_ID, getNotification());

        mReceiver = new PlayerManagerReceiver(MusicPlayerService.this, notificationManager, getPackageName(), audioManager);
        registerReceiver(mReceiver, new IntentFilter(PLAYER_MANAGER_ACTION));
    }

    private void unRegister() {
        if (notificationManager != null) {
            notificationManager.cancel(1);
        }

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }

        if (headsetReceiver != null) {
            unregisterReceiver(headsetReceiver);
        }

        if (audioManager != null) {
            audioManager.unregisterMediaButtonEventReceiver(name);
        }
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this, Constant.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("音乐");
        return builder.build();
    }
}