package com.example.musicplayer.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.example.musicplayer.fragment.PlayBarFragment;
import com.example.musicplayer.receiver.PlayerManagerReceiver;

//此线程只是用于循环发送广播，通知更改歌曲播放进度。
public class UpdateUIThread extends Thread {

    private static final String TAG = UpdateUIThread.class.getName();
    private final int threadNumber;
    private final Context context;
    private final PlayerManagerReceiver playerManagerReceiver;

    public UpdateUIThread(PlayerManagerReceiver playerManagerReceiver, Context context, int threadNumber, String name) {
        super(name);
        Log.i(TAG, "UpdateUIThread: ");
        this.playerManagerReceiver = playerManagerReceiver;
        this.context = context;
        this.threadNumber = threadNumber;
    }

    @Override
    public void run() {
        try {
            while (playerManagerReceiver.getThreadNumber() == this.threadNumber) {
                if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_STOP) {
                    Log.e(TAG, "run: Constant.STATUS_STOP");
                    break;
                } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PLAY) {
                    int duration = playerManagerReceiver.getMediaPlayer().getDuration();
                    int curPosition = playerManagerReceiver.getMediaPlayer().getCurrentPosition();
                    Intent intent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
                    intent.putExtra(Constant.STATUS, Constant.STATUS_RUN);
                    intent.putExtra(Constant.KEY_DURATION, duration);
                    intent.putExtra(Constant.KEY_CURRENT, curPosition);
                    intent.putExtra("updateMusic", false);
                    context.sendBroadcast(intent);
                }
                // Log.d(TAG, "STATUS is " + PlayerManagerReceiver.status);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }
}

