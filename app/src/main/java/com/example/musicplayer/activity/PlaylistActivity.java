package com.example.musicplayer.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.PlaylistAdapter;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.MusicInfo;
import com.example.musicplayer.entity.PlayListInfo;
import com.example.musicplayer.receiver.PlayerManagerReceiver;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;
import com.example.musicplayer.view.MusicPopMenuWindow;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends PlayBarBaseActivity {

    private static final String TAG = PlaylistActivity.class.getName();
    private RecyclerView recyclerView;
    private PlaylistAdapter playlistAdapter;
    private final List<MusicInfo> musicInfoList = new ArrayList<>();
    private PlayListInfo playListInfo;
    private Toolbar toolbar;
    private TextView noneTv;//没有歌单时现实的TextView
    private ImageView bgIv;
    private DBManager dbManager;
    private UpdateReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        playListInfo = getIntent().getParcelableExtra("playlistInfo");
        toolbar = findViewById(R.id.activity_playlist_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        CollapsingToolbarLayout mCollapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        mCollapsingToolbarLayout.setTitle(playListInfo.getName());
        dbManager = DBManager.getInstance(this);
        musicInfoList.addAll(dbManager.getMusicListByPlaylist(playListInfo.getId()));
        initView();
        register();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        recyclerView = (RecyclerView) findViewById(R.id.activity_playlist_rv);
        playlistAdapter = new PlaylistAdapter(this, playListInfo, musicInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(playlistAdapter);
        noneTv = (TextView) findViewById(R.id.activity_playlist_none_tv);
        if (playListInfo.getCount() == 0) {
            recyclerView.setVisibility(View.GONE);
            noneTv.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            noneTv.setVisibility(View.GONE);
        }


        playlistAdapter.setOnItemClickListener(new PlaylistAdapter.OnItemClickListener() {
            @Override
            public void onOpenMenuClick(int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                showPopFormBottom(musicInfo);
            }

            @Override
            public void onDeleteMenuClick(View swipeView, int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                final int curId = musicInfo.getId();
                final int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                //从列表移除
                int ret = dbManager.removeMusicFromPlaylist(musicInfo.getId(), playListInfo.getId());
                if (ret > 0) {
                    Toast.makeText(PlaylistActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PlaylistActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                }
                if (curId == musicId) {
                    //移除的是当前播放的音乐
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    sendBroadcast(intent);
                }
                musicInfoList.clear();
                musicInfoList.addAll(dbManager.getMusicListByPlaylist(playListInfo.getId()));
                playlistAdapter.updateMusicInfoList();
                //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                ((SwipeMenuLayout) swipeView).quickClose();
            }

        });

        // 当点击外部空白处时，关闭正在展开的侧滑菜单
        findViewById(R.id.activity_playlist).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SwipeMenuLayout viewCache = SwipeMenuLayout.getViewCache();
                    if (null != viewCache) {
                        viewCache.smoothClose();
                    }
                }
                return false;
            }
        });

    }

    public void showPopFormBottom(MusicInfo musicInfo) {
        MusicPopMenuWindow menuPopupWindow = new MusicPopMenuWindow(PlaylistActivity.this, musicInfo, findViewById(R.id.activity_playlist), Constant.ACTIVITY_MYLIST);
        //      设置Popupwindow显示位置（从底部弹出）
        menuPopupWindow.showAtLocation(findViewById(R.id.activity_playlist), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = PlaylistActivity.this.getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        menuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });

        menuPopupWindow.setOnDeleteUpdateListener(() -> {
            musicInfoList.clear();
            musicInfoList.addAll(dbManager.getMusicListByPlaylist(playListInfo.getId()));
            playlistAdapter.updateMusicInfoList();
        });

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
        unRegister();
    }


    private void register() {
        try {
            if (mReceiver != null) {
                this.unRegister();
            }
            mReceiver = new UpdateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PlayerManagerReceiver.ACTION_UPDATE_UI_ADAPTER);
            this.registerReceiver(mReceiver, intentFilter);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void unRegister() {
        try {
            if (mReceiver != null) {
                this.unregisterReceiver(mReceiver);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            playlistAdapter.notifyDataSetChanged();
        }
    }
}
