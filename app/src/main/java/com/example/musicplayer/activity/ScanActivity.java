package com.example.musicplayer.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.example.musicplayer.R;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.MusicInfo;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.*;
import com.example.musicplayer.view.ScanView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanActivity extends BaseActivity {

    private static final String TAG = ScanActivity.class.getName();
    private DBManager dbManager;
    private Toolbar toolbar;
    private Button scanBtn;
    private TextView scanPathTv;
    private TextView scanCountTv;
    private CheckBox filterCb;
    private ScanView scanView;
    private Handler handler;
    private Message msg;
    private String scanPath;
    private int progress = 0;
    private int musicCount = 0;
    private boolean scanning = false;
    private int curMusicId;
    private String curMusicPath;
    private final List<MusicInfo> musicInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        dbManager = DBManager.getInstance(ScanActivity.this);
        scanBtn = findViewById(R.id.start_scan_btn);
        setScanBtnBg();
        toolbar = findViewById(R.id.scan_music_toolbar);
        scanCountTv = findViewById(R.id.scan_count);
        scanPathTv = findViewById(R.id.scan_path);
        filterCb = findViewById(R.id.scan_filter_cb);
        scanView = findViewById(R.id.scan_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        scanBtn.setOnClickListener(v -> {
            if (! scanning) {
                scanPathTv.setVisibility(View.VISIBLE);
                scanning = true;
                startScanLocalMusic();
                scanView.start();
                scanBtn.setText("停止扫描");
            } else {
                scanPathTv.setVisibility(View.GONE);
                scanning = false;
                scanView.stop();
                scanCountTv.setText("");
                scanBtn.setText("开始扫描");
            }
        });


        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Constant.SCAN_NO_MUSIC:
                        Toast.makeText(ScanActivity.this, "本地没有歌曲，快去下载吧", Toast.LENGTH_SHORT).show();
                        scanComplete();
                        break;
                    case Constant.SCAN_ERROR:
                        Toast.makeText(ScanActivity.this, "数据库错误", Toast.LENGTH_LONG).show();
                        scanComplete();
                        break;
                    case Constant.SCAN_COMPLETE:
                        initCurPlaying();
                        scanComplete();
                        break;
                    case Constant.SCAN_UPDATE:
                        //                        int updateProgress = msg.getData().getInt("progress");
                        String path = msg.getData().getString("scanPath");
                        scanCountTv.setText("已扫描到" + progress + "首歌曲");
                        scanPathTv.setText(path);
                        break;
                }
            }
        };

    }

    private void scanComplete() {
        scanBtn.setText("完成");
        scanning = false;
        scanBtn.setOnClickListener(v -> {
            if (! scanning) {
                ScanActivity.this.finish();
            }
        });
        scanView.stop();
    }

    public void startScanLocalMusic() {
        new Thread("扫描歌曲") {

            @Override
            public void run() {
                super.run();
                try {
                    String[] muiscInfoArray = new String[]{
                            MediaStore.Audio.Media.TITLE,               //歌曲名称
                            MediaStore.Audio.Media.ARTIST,              //歌曲歌手
                            MediaStore.Audio.Media.ALBUM,               //歌曲的专辑名
                            MediaStore.Audio.Media.DURATION,            //歌曲时长
                            MediaStore.Audio.Media.DATA};               //歌曲文件的全路径
                    Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            muiscInfoArray, null, null, null);
                    if (cursor != null && cursor.getCount() != 0) {
                        Log.i(TAG, "run: cursor.getCount() = " + cursor.getCount());
                        while (cursor.moveToNext()) {
                            if (! scanning) {
                                return;
                            }
                            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                            int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));

                            if (filterCb.isChecked() && duration < 1000 * 60) {
                                Log.i(TAG, "run: name = " + name + " duration < 1000 * 60");
                                continue;
                            }

                            File file = new File(path);
                            String parentPath = file.getParentFile().getPath();

                            name = replaseUnKnowe(name);
                            singer = replaseUnKnowe(singer);
                            album = replaseUnKnowe(album);
                            path = replaseUnKnowe(path);

                            MusicInfo musicInfo = new MusicInfo();

                            musicInfo.setName(name);
                            musicInfo.setSinger(singer);
                            musicInfo.setAlbum(album);
                            musicInfo.setDuration(duration);
                            musicInfo.setPath(path);
                            Log.i(TAG, "run: parentPath = " + parentPath);
                            musicInfo.setParentPath(parentPath);
                            musicInfo.setLove(0);
                            musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(name).toUpperCase().charAt(0) + "");

                            path = path.substring(0, path.lastIndexOf('.') == - 1 ? path.length() : path.lastIndexOf('.')) + ".lrc";
                            if (new File(path).exists()) {
                                musicInfo.setLrcPath(path);
                            }

                            musicInfoList.add(musicInfo);
                            progress++;
                            scanPath = path;
                            musicCount = cursor.getCount();
                            msg = new Message();    //每次都必须new，必须发送新对象，不然会报错
                            msg.what = Constant.SCAN_UPDATE;
                            msg.arg1 = musicCount;
                            //                                Bundle data = new Bundle();
                            //                                data.putInt("progress", progress);
                            //                                data.putString("scanPath", scanPath);
                            //                                msg.setData(data);
                            handler.sendMessage(msg);  //更新UI界面
                            try {
                                sleep(50);
                            } catch (InterruptedException e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                        }

                        //扫描完成获取一下当前播放音乐及路径
                        curMusicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                        curMusicPath = dbManager.getMusicPath(curMusicId);

                        // 根据a-z进行排序源数据
                        Collections.sort(musicInfoList);
                        dbManager.updateAllMusic(musicInfoList);

                        //扫描完成
                        msg = new Message();
                        msg.what = Constant.SCAN_COMPLETE;
                        handler.sendMessage(msg);  //更新UI界面

                    } else {
                        msg = new Message();
                        msg.what = Constant.SCAN_NO_MUSIC;
                        handler.sendMessage(msg);  //更新UI界面
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "run: error = ", e);
                    //扫描出错
                    msg = new Message();
                    msg.what = Constant.SCAN_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    public static String replaseUnKnowe(String oldStr) {
        try {
            if (oldStr != null) {
                if (oldStr.equals("<unknown>")) {
                    oldStr = oldStr.replaceAll("<unknown>", "未知");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "replaseUnKnowe: error = ", e);
        }
        return oldStr;
    }

    //初始化当前播放音乐，有可能当前正在播放音乐已经被过滤掉了
    private void initCurPlaying() {
        try {
            boolean contain = false;
            int id = 1;
            for (MusicInfo info : musicInfoList) {
                Log.d(TAG, "initCurPlaying: info.getPath() = " + info.getPath());
                Log.d(TAG, "initCurPlaying: curMusicPath = " + curMusicPath);
                if (info.getPath().equals(curMusicPath)) {
                    contain = true;
                    Log.d(TAG, "initCurPlaying: musicInfoList.indexOf(info) = " + musicInfoList.indexOf(info));
                    id = musicInfoList.indexOf(info) + 1;
                }
            }
            if (contain) {
                Log.d(TAG, "initCurPlaying: contains");
                Log.d(TAG, "initCurPlaying: id = " + id);
                MyMusicUtil.setShared(Constant.KEY_ID, id);
            } else {
                Log.d(TAG, "initCurPlaying: !!!contains");
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                sendBroadcast(intent);
            }

        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    private void setScanBtnBg() {
        int defColor = CustomAttrValueUtil.getAttrColorValue(R.attr.colorAccent, R.color.colorAccent, this);
        int pressColor = CustomAttrValueUtil.getAttrColorValue(R.attr.press_color, R.color.colorAccent, this);
        Drawable backgroundDrawable = scanBtn.getBackground();
        StateListDrawable sld = (StateListDrawable) backgroundDrawable;// 通过向下转型，转回原型，selector对应的Java类为：StateListDrawable
        SelectorUtil.changeViewColor(sld, new int[]{pressColor, defColor});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
