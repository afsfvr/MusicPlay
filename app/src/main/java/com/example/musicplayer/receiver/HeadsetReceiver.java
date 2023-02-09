package com.example.musicplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;

import java.util.Timer;
import java.util.TimerTask;

public class HeadsetReceiver extends BroadcastReceiver {
    private static final String TAG = HeadsetReceiver.class.getName();
    private static int clickCount;
    private final Timer timer = new Timer();
    private DBManager dbManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "action:" + action);
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.d(TAG, "onReceive:" + event);
            Intent i;
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PAUSE) {
                        i.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    } else {
                        if (dbManager == null) dbManager = DBManager.getInstance(context);
                        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                        if (musicId == - 1 || musicId == 0) {
                            i.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        } else {
                            String path = dbManager.getMusicPath(musicId);
                            i.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                            i.putExtra(Constant.KEY_PATH, path);
                        }
                    }
                    context.sendBroadcast(i);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    i.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                    context.sendBroadcast(i);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    i.putExtra(Constant.COMMAND, Constant.COMMAND_NEXT);
                    context.sendBroadcast(i);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    i.putExtra(Constant.COMMAND, Constant.COMMAND_PREV);
                    context.sendBroadcast(i);
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    clickCount++;
                    if (clickCount == 1) {
                        HeadsetTimerTask task = new HeadsetTimerTask(context);
                        timer.schedule(task, 600);
                    }
                    break;
            }
        } else if (action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PLAY) {
                Intent i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                i.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                context.sendBroadcast(i);
            }
        }
    }

    class HeadsetTimerTask extends TimerTask {
        private final Context context;

        public HeadsetTimerTask(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void run() {
            try {
                if (clickCount == 1) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PLAY) {
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                    } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PAUSE) {
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    } else {
                        if (dbManager == null) dbManager = DBManager.getInstance(context);
                        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                        if (musicId == - 1 || musicId == 0) {
                            intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        } else {
                            String path = dbManager.getMusicPath(musicId);
                            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                            intent.putExtra(Constant.KEY_PATH, path);
                        }

                    }
                    context.sendBroadcast(intent);
                } else if (clickCount == 2) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_NEXT);
                    context.sendBroadcast(intent);
                } else if (clickCount == 3) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PREV);
                    context.sendBroadcast(intent);
                }
                clickCount = 0;
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }
}