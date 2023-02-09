package com.example.musicplayer.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.example.musicplayer.R;
import com.example.musicplayer.activity.HomeActivity;
import com.example.musicplayer.activity.PlayActivity;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.fragment.PlayBarFragment;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;
import com.example.musicplayer.util.UpdateUIThread;

import java.io.File;
import java.util.ArrayList;

import static com.example.musicplayer.service.MusicPlayerService.PLAYER_MANAGER_ACTION;

public class PlayerManagerReceiver extends BroadcastReceiver {

    private static final String TAG = PlayerManagerReceiver.class.getName();
    public static final String ACTION_UPDATE_UI_ADAPTER = "com.example.musicplayer.receiver.PlayerManagerReceiver:action_update_ui_adapter_broad_cast";
    private MediaPlayer mediaPlayer;
    private DBManager dbManager;
    private static int status = Constant.STATUS_STOP;
    private int threadNumber;
    private Context context;
    private NotificationManager notificationManager;
    private String packageName;
    private AudioManager audioManager;
    private static boolean audioFocus = false;
    private static int pause = 0;

    public PlayerManagerReceiver() {
    }

    public PlayerManagerReceiver(Context context, NotificationManager notificationManager, String packageName, AudioManager audioManager) {
        super();
        Log.d(TAG, "create");
        this.context = context;
        dbManager = DBManager.getInstance(context);
        int musicID = MyMusicUtil.getIntShared(Constant.KEY_ID);
        if (musicID != - 1) {
            playMusic(dbManager.getMusicPath(musicID));
            status = Constant.STATUS_PAUSE;
        } else {
            mediaPlayer = new MediaPlayer();
        }
        this.notificationManager = notificationManager;
        this.packageName = packageName;
        this.audioManager = audioManager;
        UpdateUI(true);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int cmd = intent.getIntExtra(Constant.COMMAND, Constant.COMMAND_INIT);
        Log.d(TAG, "cmd = " + cmd);
        boolean updateMusic = false;
        switch (cmd) {
            case Constant.COMMAND_INIT:
                Log.d(TAG, "COMMAND_INIT");
                break;
            case Constant.COMMAND_PLAY:
                Log.d(TAG, "COMMAND_PLAY");
                status = Constant.STATUS_PLAY;
                String musicPath = intent.getStringExtra(Constant.KEY_PATH);
                if (musicPath != null) {
                    playMusic(musicPath);
                    updateMusic = true;
                }
                if (! audioFocus && audioManager != null) requestAudioFocus();
                mediaPlayer.start();
                break;
            case Constant.COMMAND_PAUSE:
                status = Constant.STATUS_PAUSE;
                mediaPlayer.pause();
                if (pause == 1) {
                    pause = 2;
                } else {
                    pause = 0;
                }
                break;
            case Constant.COMMAND_STOP: //本程序停止状态都是删除当前播放音乐触发
                if (mediaPlayer != null && status != Constant.STATUS_STOP) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                status = Constant.STATUS_STOP;
                initStopOperate();
                updateMusic = true;
                break;
            case Constant.COMMAND_PROGRESS://拖动进度
                int curProgress = intent.getIntExtra(Constant.KEY_CURRENT, 0);
                //异步的，可以设置完成监听来获取真正定位完成的时候
                mediaPlayer.seekTo(curProgress);
                break;
            case Constant.COMMAND_RELEASE:
                NumberRandom();
                if (mediaPlayer != null && status != Constant.STATUS_STOP) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                }
                if (PlayerManagerReceiver.audioFocus) {
                    audioManager.abandonAudioFocus(onAudioFocusChangeListener);
                    audioFocus = false;
                }
                status = Constant.STATUS_STOP;
                break;
            case Constant.COMMAND_PREV:
                MyMusicUtil.playPreMusic(this.context);
                status = Constant.STATUS_PLAY;
                break;
            case Constant.COMMAND_NEXT:
                MyMusicUtil.playNextMusic(this.context);
                status = Constant.STATUS_PLAY;
                break;
            case Constant.COMMAND_UPDATE_NOTIFICATION:
                updateNotification();
                break;
        }

        UpdateUI(updateMusic);
    }

    private void requestAudioFocus() {
        audioFocus = (1 == audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN));
        Log.d(TAG, "requestAudioFocus is " + audioFocus);
    }

    private void initStopOperate() {
        NumberRandom();
        MyMusicUtil.setShared(Constant.KEY_ID, dbManager.getFirstId());
        MyMusicUtil.setShared(Constant.KEY_LIST, Constant.LIST_ALLMUSIC);
    }

    private void playMusic(String musicPath) {
        NumberRandom();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            Log.d(TAG, "playMusic onCompletion: ");
            NumberRandom();                //切换线程
            onComplete();     //调用音乐切换模块，进行相应操作
            UpdateUI(true);                //更新界面
        });
        try {
            File file = new File(musicPath);
            if (! file.exists()) {
                Toast.makeText(context, "歌曲文件不存在，请重新扫描", Toast.LENGTH_SHORT).show();
                MyMusicUtil.playNextMusic(context);
                return;
            }
            mediaPlayer.setDataSource(musicPath);
            mediaPlayer.prepare();

            new UpdateUIThread(this, context, threadNumber, file.getName()).start();
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    //取一个（0，100）之间的不一样的随机数
    private void NumberRandom() {
        int count;
        do {
            count = (int) (Math.random() * 100);
        } while (count == threadNumber);
        threadNumber = count;
    }

    private void onComplete() {
        MyMusicUtil.playNextMusic(context);
    }

    private void UpdateUI(boolean updateMusic) {
        Log.d(TAG, "updateUI");
        Intent playBarintent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);    //接收广播为MusicUpdateMain
        playBarintent.putExtra(Constant.STATUS, status);
        playBarintent.putExtra("updateMusic", updateMusic);
        context.sendBroadcast(playBarintent);

        Intent intent = new Intent(ACTION_UPDATE_UI_ADAPTER);    //接收广播为所有歌曲列表的adapter
        context.sendBroadcast(intent);

        updateNotification();
    }

    private void updateNotification() {
        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);

        RemoteViews view = new RemoteViews(packageName, R.layout.notification);

        Intent intent = new Intent(context, PlayActivity.class);
        try {
            intent.putExtra(Constant.KEY_CURRENT, mediaPlayer.getCurrentPosition());
        } catch (Exception ignored) {
        }
        PendingIntent home = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_layout, home);

        intent = new Intent(PLAYER_MANAGER_ACTION);
        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PREV);
        PendingIntent prev = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_prev, prev);

        intent = new Intent(PLAYER_MANAGER_ACTION);
        intent.putExtra(Constant.COMMAND, Constant.COMMAND_NEXT);
        PendingIntent next = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_next, next);

        intent = new Intent(PLAYER_MANAGER_ACTION);
        if (status == Constant.STATUS_PAUSE) {
            view.setImageViewResource(R.id.notification_play, R.drawable.play_btn_play);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
        } else if (status == Constant.STATUS_PLAY) {
            view.setImageViewResource(R.id.notification_play, R.drawable.play_pause);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
        } else {
            view.setImageViewResource(R.id.notification_play, R.drawable.play_btn_play);
            if (musicId == - 1) {
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            } else {
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                intent.putExtra(Constant.KEY_PATH, dbManager.getMusicPath(musicId));
            }
        }
        PendingIntent play = PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_play, play);

        intent = new Intent(Constant.FLOAT_RECEIVER);
        if (HomeActivity.getShowFloat()) {
            view.setTextColor(R.id.notification_lrc, HomeActivity.THEME_COLOR);
        } else {
            view.setTextColor(R.id.notification_lrc, Color.parseColor("#515151"));
        }
        intent.putExtra(Constant.FLOAT_CHANGE, true);
        PendingIntent lyric = PendingIntent.getBroadcast(context, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_lrc, lyric);

        ArrayList<String> musicInfo = dbManager.getMusicInfo(musicId);
        view.setTextViewText(R.id.notification_music_name, musicInfo.get(1));
        try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
            mmr.setDataSource(musicInfo.get(5));
            byte[] byte_pic = mmr.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(byte_pic, 0, byte_pic.length);
            view.setImageViewBitmap(R.id.notification_disc, bitmap);
            mmr.release();
        } catch (Exception e) {
            view.setImageViewResource(R.id.notification_disc, R.drawable.bg_disc);
        }

        Notification notification = new Notification.Builder(context, Constant.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCustomContentView(view).build();

        notificationManager.notify(Constant.NOTIFICATION_ID, notification);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public static int getMediaPlayerStatus() {
        return status;
    }

    private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        Intent intent = new Intent(PLAYER_MANAGER_ACTION);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d(TAG, "获得音频焦点");
                if (pause == 2) {
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "失去音频焦点");
                audioFocus = false;
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "临时失去音频焦点");
                if (PlayerManagerReceiver.status == Constant.STATUS_PLAY) {
                    pause = 1;
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Log.d(TAG, "临时失去音频焦点，降低音量");
                break;
        }
        onReceive(context, intent);
        //        sendBroadcast(intent);
    };
}
