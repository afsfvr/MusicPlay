package com.example.musicplayer.activity;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.ChooseDirAdapter;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.FolderInfo;
import com.example.musicplayer.entity.MusicInfo;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.ChineseToEnglish;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;
import com.example.musicplayer.view.ScanView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ChooseDirActivity extends BaseActivity {
    private static final String TAG = ChooseDirActivity.class.getName();
    private Toolbar toolbar;
    private ListView listView;
    private ChooseDirAdapter adapter;
    private final ArrayList<FolderInfo> folderInfoList = new ArrayList<>();
    private String rootPath;
    private DBManager dbManager;
    private ScanView scanView;
    private TextView count;
    private int scanNum = 0;
    private boolean chooseDir = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_dir);
        String title = getIntent().getStringExtra("title");
        if (title != null) setTitle(title);

        init();
    }

    private void init() {
        scanView = findViewById(R.id.choose_scan_view);
        count = findViewById(R.id.choose_scan_count);
        toolbar = findViewById(R.id.choose_dir_toolbar);
        setSupportActionBar(toolbar);
        rootPath = Environment.getExternalStorageDirectory().getPath();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        adapter = new ChooseDirAdapter(folderInfoList, this);
        listView = findViewById(R.id.choose_list);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        if ("lrc".equals(getIntent().getStringExtra("choose"))) {
            chooseDir = false;
            listView.setOnItemClickListener((parent, view, position, id) -> {
                FolderInfo folderInfo = folderInfoList.get(position);
                String path = folderInfo.getPath();
                if (folderInfo.getCount() < 0) {
                    updateData(path);
                } else {
                    String name = folderInfo.getName();
                    if (name.lastIndexOf(".lrc") == name.length() - 4) {
                        Intent intent = new Intent();
                        intent.putExtra(Constant.KEY_PATH, folderInfo.getPath());
                        setResult(987, intent);
                        finish();
                    } else {
                        Toast.makeText(this, "请选择后缀为lrc的歌词文件", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            dbManager = DBManager.getInstance(this);
            toolbar.setTitle(rootPath);
            listView.setOnItemClickListener((parent, view, position, id) -> updateData(folderInfoList.get(position).getPath()));
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                FolderInfo folderInfo = folderInfoList.get(position);
                if (position != 0 || ! folderInfo.getName().equals("上一级")) {
                    readDir(folderInfo.getPath());
                }
                return true;
            });
        }
        updateData(rootPath);
    }

    private void updateData(String path) {
        File file = new File(path);
        if (file.exists()) {
            folderInfoList.clear();
            if (file.getPath().length() > rootPath.length()) {
                FolderInfo folderInfo = new FolderInfo();
                folderInfo.setName("上一级");
                folderInfo.setPath(file.getParent());
                folderInfo.setCount(- 1);
                folderInfoList.add(folderInfo);
            }
            File[] files = file.listFiles();
            if (files != null) {
                for (File listFile : files) {
                    if (listFile.isDirectory()) {
                        FolderInfo folderInfo = new FolderInfo();
                        folderInfo.setName(listFile.getName());
                        folderInfo.setPath(listFile.getPath());
                        folderInfo.setCount(- 1);
                        folderInfoList.add(folderInfo);
                    }
                    if (! chooseDir && listFile.isFile()) {
                        String name = listFile.getName();
                        if (name.lastIndexOf(".lrc") == name.length() - 4) {
                            FolderInfo folderInfo = new FolderInfo();
                            folderInfo.setName(name);
                            folderInfo.setPath(listFile.getPath());
                            folderInfo.setCount(1);
                            folderInfoList.add(folderInfo);
                        }
                    }
                }
            }
            toolbar.setTitle(path);
            adapter.updateData();
        } else {
            Toast.makeText(this, "路径错误", Toast.LENGTH_SHORT).show();
        }
    }

    public void readMusic(ArrayList<MusicInfo> list, File file, String path) {
        File[] files = file.listFiles();
        if (files == null) return;
        for (File tmp : files) {
            if (tmp.isFile() && tmp.canRead()) {
                try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                    mmr.setDataSource(tmp.getAbsolutePath());
                    MusicInfo musicInfo = new MusicInfo();
                    String name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    musicInfo.setSinger(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
                    if (name == null) {
                        name = tmp.getName();
                        if (name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
                        String[] split = name.split("-", 2);
                        name = split[0].trim();
                        if (musicInfo.getSinger() == null && split.length == 2) {
                            musicInfo.setSinger(split[1].trim());
                        }
                    }
                    musicInfo.setName(name);
                    musicInfo.setAlbum(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                    musicInfo.setDuration(Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                    musicInfo.setPath(tmp.getPath());
                    musicInfo.setParentPath(path);
                    musicInfo.setLove(0);
                    musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(name).toUpperCase().charAt(0) + "");
                    String p = tmp.getPath();
                    p = p.substring(0, p.lastIndexOf('.') == - 1 ? p.length() : p.lastIndexOf('.')) + ".lrc";
                    if (new File(p).exists()) musicInfo.setLrcPath(p);
                    list.add(musicInfo);
                    mmr.release();
                    Log.d(TAG, "添加'" + tmp.getName() + "'成功");
                    scanNum++;
                    count.post(() -> count.setText("已扫描到" + scanNum + "首歌曲"));
                } catch (Exception e) {
                    Log.d(TAG, "添加'" + tmp.getName() + "'失败");
                }
            } else if (tmp.isDirectory()) {
                readMusic(list, tmp, tmp.getPath());
            }
        }
    }

    public void readDir(final String path) {
        listView.setVisibility(View.GONE);
        count.setVisibility(View.VISIBLE);
        scanView.setVisibility(View.VISIBLE);
        scanView.start();
        toolbar.setTitle(path);
        new Thread("扫描音乐:" + path) {
            @Override
            public void run() {
                super.run();
                try {
                    ArrayList<MusicInfo> musicInfoList = new ArrayList<>();
                    readMusic(musicInfoList, new File(path), path);
                    Collections.sort(musicInfoList);
                    int success = dbManager.insert(musicInfoList);
                    ChooseDirActivity.this.runOnUiThread(() -> {
                        Toast.makeText(ChooseDirActivity.this,
                                "添加成功" + success + "首歌曲，失败" + (scanNum - success) + "首歌曲",
                                Toast.LENGTH_SHORT).show();
                        scanView.stop();
                    });
                    if (MyMusicUtil.getIntShared(Constant.KEY_ID) == - 1) {
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        sendBroadcast(intent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
                ChooseDirActivity.this.finish();
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        if (toolbar.getTitle().length() > rootPath.length()) {
            updateData(new File(toolbar.getTitle() + "").getParent());
        } else {
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }
}