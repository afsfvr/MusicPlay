package com.example.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.musicplayer.R;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.LrcRow;
import com.example.musicplayer.fragment.PlayBarFragment;
import com.example.musicplayer.receiver.PlayerManagerReceiver;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.CustomAttrValueUtil;
import com.example.musicplayer.util.MyMusicUtil;
import com.example.musicplayer.view.LyricView;
import com.example.musicplayer.view.PlayingPopWindow;

import java.util.ArrayList;
import java.util.Locale;

public class PlayActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = PlayActivity.class.getName();

    private DBManager dbManager;

    private ImageView backIv;
    private ImageView playIv;
    private ImageView menuIv;
    private ImageView preIv;
    private ImageView nextIv;
    private ImageView modeIv;
    private ImageView rotateIv;
    private ImageView loveIv;

    private TextView curTimeTv;
    private TextView totalTimeTv;

    private TextView musicNameTv;
    private TextView singerNameTv;

    private SeekBar seekBar;

    private PlayReceiver mReceiver;

    private int current;
    private int duration;
    /**
     * 自定义LrcView，用来展示歌词
     */
    private LyricView lrcView;
    private ActivityResultLauncher<Intent> chooseLrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        setStyle();
        current = getIntent().getIntExtra(Constant.KEY_CURRENT, 0);
        dbManager = DBManager.getInstance(PlayActivity.this);
        chooseLrc = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (result.getResultCode() != 987 || data == null) return;
                    String path = data.getStringExtra(Constant.KEY_PATH);
                    if (path != null && path.length() > 0) {
                        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                        if (musicId == - 1 || musicId == 0) {
                            Toast.makeText(this, "歌曲不存在", Toast.LENGTH_SHORT).show();
                        } else {
                            dbManager.updateLrcPath(path, musicId);
                            initLyric(path);
                        }
                    }
                });
        initView();
        register();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unRegister();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    private void initView() {
        lrcView = findViewById(R.id.lyric_view);
        TypedValue value = new TypedValue();
        if (this.getTheme().resolveAttribute(R.attr.image_tint, value, true)) {
            lrcView.setmHighLightRowColor(value.data);
        }
        backIv = findViewById(R.id.iv_back);
        playIv = findViewById(R.id.iv_play);
        menuIv = findViewById(R.id.iv_menu);
        preIv = findViewById(R.id.iv_prev);
        nextIv = findViewById(R.id.iv_next);
        modeIv = findViewById(R.id.iv_mode);
        rotateIv = findViewById(R.id.iv_disc_rotate);
        loveIv = findViewById(R.id.iv_love);
        curTimeTv = findViewById(R.id.tv_current_time);
        totalTimeTv = findViewById(R.id.tv_total_time);
        musicNameTv = findViewById(R.id.tv_title);
        singerNameTv = findViewById(R.id.tv_artist);
        seekBar = findViewById(R.id.activity_play_seekbar);
        backIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
        menuIv.setOnClickListener(this);
        preIv.setOnClickListener(this);
        nextIv.setOnClickListener(this);
        modeIv.setOnClickListener(this);
        lrcView.setOnClickListener(new LyricView.OnClickListener() {
            @Override
            public void onClick() {
                Intent intent = new Intent(PlayActivity.this, ChooseDirActivity.class);
                intent.putExtra(Constant.TITLE, "选择歌词");
                intent.putExtra("choose", "lrc");
                chooseLrc.launch(intent);
            }

            @Override
            public void onSlideClick(int time) {
                int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                if (musicId == - 1) {
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_LONG).show();
                } else {
                    seekBar.setProgress(time);
                    if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_STOP) {
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                        intent.putExtra(Constant.KEY_PATH, dbManager.getMusicPath(musicId));
                        sendBroadcast(intent);
                    } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PAUSE) {
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                        sendBroadcast(intent);
                    }
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PROGRESS);
                    intent.putExtra(Constant.KEY_CURRENT, current);
                }
                sendBroadcast(intent);
            }
        });

        setSeekBarBg();
        initPlayMode();
        initTitle();
        initPlayIv();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                if (musicId == - 1) {
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_LONG).show();
                } else {
                    if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_STOP) {
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                        intent.putExtra(Constant.KEY_PATH, dbManager.getMusicPath(musicId));
                        sendBroadcast(intent);
                    } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PAUSE) {
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                        sendBroadcast(intent);
                    }
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PROGRESS);
                    intent.putExtra(Constant.KEY_CURRENT, current);
                }
                sendBroadcast(intent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                current = progress;
                initTime();
                lrcView.seekLrcToTime(progress);
            }
        });

        seekBar.setProgress(current);
    }

    private void initLyric(String lrcPath) {
        if (lrcPath != null && lrcPath.length() > 0) {
            lrcView.setLrc(LrcRow.createLrcRows(lrcPath));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_mode:
                switchPlayMode();
                break;
            case R.id.iv_play:
                play();
                break;
            case R.id.iv_next:
                MyMusicUtil.playNextMusic(this);
                break;
            case R.id.iv_prev:
                MyMusicUtil.playPreMusic(this);
                break;
            case R.id.iv_menu:
                showPopFormBottom();
                break;
        }
    }


    private void initPlayIv() {
        switch (PlayerManagerReceiver.getMediaPlayerStatus()) {
            case Constant.STATUS_STOP:
                playIv.setSelected(false);
                break;
            case Constant.STATUS_PLAY:
                playIv.setSelected(true);
                break;
            case Constant.STATUS_PAUSE:
                playIv.setSelected(false);
                break;
            case Constant.STATUS_RUN:
                playIv.setSelected(true);
                break;
        }
    }

    private void initPlayMode() {
        int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
        if (playMode == - 1) {
            playMode = 0;
        }
        modeIv.setImageLevel(playMode);
    }

    private void initTitle() {
        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
        if (musicId == - 1) {
            musicNameTv.setText("听听音乐");
            singerNameTv.setText("好音质");
            rotateIv.setImageResource(R.drawable.bg_disc);
        } else {
            ArrayList<String> musicInfo = dbManager.getMusicInfo(musicId);
            musicNameTv.setText(musicInfo.get(1));
            singerNameTv.setText(musicInfo.get(2));
            duration = Integer.parseInt(musicInfo.get(4));
            seekBar.setMax(duration);
            totalTimeTv.setText(formatTime(duration));
            try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                mmr.setDataSource(musicInfo.get(5));
                byte[] byte_pic = mmr.getEmbeddedPicture();
                Bitmap bitmap = BitmapFactory.decodeByteArray(byte_pic, 0, byte_pic.length);
                rotateIv.setImageBitmap(bitmap);
                mmr.release();
            } catch (Exception e) {
                rotateIv.setImageResource(R.drawable.bg_disc);
            }
            if (Integer.parseInt(musicInfo.get(7)) == 1) {
                loveIv.setImageResource(R.drawable.love_hover);
            }
            loveIv.setOnClickListener(new View.OnClickListener() {
                boolean isLove = Integer.parseInt(musicInfo.get(7)) == 1;

                @Override
                public void onClick(View v) {
                    if (isLove) {
                        dbManager.removeMusic(musicId, Constant.ACTIVITY_MYLOVE);
                        loveIv.setImageResource(R.drawable.love);
                        Toast.makeText(PlayActivity.this, "删除我喜欢的音乐成功", Toast.LENGTH_SHORT).show();
                        isLove = false;
                    } else {
                        dbManager.setMyLove(musicId);
                        loveIv.setImageResource(R.drawable.love_hover);
                        Toast.makeText(PlayActivity.this, "设置我喜欢的音乐成功", Toast.LENGTH_SHORT).show();
                        isLove = true;
                    }
                }
            });
            initLyric(musicInfo.get(9));
        }
    }

    private void initTime() {
        curTimeTv.setText(formatTime(current));
        totalTimeTv.setText(formatTime(duration));
        //        if (progress - mLastProgress >= 1000) {
        //            tvCurrentTime.setText(formatTime(progress));
        //            mLastProgress = progress;
        //        }
    }

    private String formatTime(long time) {
        return formatTime("mm:ss", time);
    }

    public static String formatTime(String pattern, long milli) {
        int m = (int) (milli / DateUtils.MINUTE_IN_MILLIS);
        int s = (int) ((milli / DateUtils.SECOND_IN_MILLIS) % 60);
        String mm = String.format(Locale.getDefault(), "%02d", m);
        String ss = String.format(Locale.getDefault(), "%02d", s);
        return pattern.replace("mm", mm).replace("ss", ss);
    }

    private void switchPlayMode() {
        int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                MyMusicUtil.setShared(Constant.KEY_MODE, Constant.PLAYMODE_RANDOM);
                break;
            case Constant.PLAYMODE_RANDOM:
                MyMusicUtil.setShared(Constant.KEY_MODE, Constant.PLAYMODE_SINGLE_REPEAT);
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                MyMusicUtil.setShared(Constant.KEY_MODE, Constant.PLAYMODE_SEQUENCE);
                break;
        }
        initPlayMode();
    }

    private void setSeekBarBg() {
        try {
            int progressColor = CustomAttrValueUtil.getAttrColorValue(R.attr.colorPrimary, R.color.colorAccent, this);
            LayerDrawable layerDrawable = (LayerDrawable) seekBar.getProgressDrawable();
            ScaleDrawable scaleDrawable = (ScaleDrawable) layerDrawable.findDrawableByLayerId(android.R.id.progress);
            GradientDrawable drawable = (GradientDrawable) scaleDrawable.getDrawable();
            drawable.setColor(progressColor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void play() {
        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
        if (musicId == - 1 || musicId == 0) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            sendBroadcast(intent);
            Toast.makeText(PlayActivity.this, "歌曲不存在", Toast.LENGTH_SHORT).show();
        } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PAUSE) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            sendBroadcast(intent);
        } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PLAY) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
            sendBroadcast(intent);
        } else {
            //为停止状态时发送播放命令，并发送将要播放歌曲的路径
            String path = dbManager.getMusicPath(musicId);
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            intent.putExtra(Constant.KEY_PATH, path);
            Log.i(TAG, "onClick: path = " + path);
            sendBroadcast(intent);
        }
    }

    public void showPopFormBottom() {
        PlayingPopWindow playingPopWindow = new PlayingPopWindow(PlayActivity.this);
        playingPopWindow.showAtLocation(findViewById(R.id.activity_play), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.7f;
        getWindow().setAttributes(params);

        playingPopWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams params1 = getWindow().getAttributes();
            params1.alpha = 1f;
            getWindow().setAttributes(params1);
        });

    }

    private void register() {
        mReceiver = new PlayReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
        registerReceiver(mReceiver, intentFilter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }


    class PlayReceiver extends BroadcastReceiver {

        int status;

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            status = intent.getIntExtra(Constant.STATUS, 0);
            if (intent.getBooleanExtra("updateMusic", false)) {
                initTitle();
            }
            switch (status) {
                case Constant.STATUS_STOP:
                    playIv.setSelected(false);
                    break;
                case Constant.STATUS_PLAY:
                    playIv.setSelected(true);
                    break;
                case Constant.STATUS_PAUSE:
                    playIv.setSelected(false);
                    break;
                case Constant.STATUS_RUN:
                    current = intent.getIntExtra(Constant.KEY_CURRENT, 0);
                    duration = intent.getIntExtra(Constant.KEY_DURATION, 100);
                    playIv.setSelected(true);
                    seekBar.setMax(duration);
                    seekBar.setProgress(current);
                    break;
            }

        }
    }

    private void setStyle() {
        View decorView = getWindow().getDecorView();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(option);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

}
