package com.example.musicplayer.activity;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.musicplayer.R;
import com.example.musicplayer.adapter.HomeListViewAdapter;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.LrcRow;
import com.example.musicplayer.entity.PlayListInfo;
import com.example.musicplayer.fragment.PlayBarFragment;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyApplication;
import com.example.musicplayer.util.MyMusicUtil;
import com.example.musicplayer.util.ShutdownThread;
import com.example.musicplayer.view.LyricView;
import com.google.android.material.navigation.NavigationView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends PlayBarBaseActivity {

    private static final String TAG = HomeActivity.class.getName();
    private DBManager dbManager;
    private DrawerLayout mDrawerLayout;
    private NavigationView navView;
    private ImageView navHeadIv;
    private LinearLayout localMusicLl;
    private LinearLayout lastPlayLl;
    private LinearLayout myLoveLl;
    private LinearLayout myListTitleLl;
    private Toolbar toolbar;
    private TextView localMusicCountTv;
    private TextView lastPlayCountTv;
    private TextView myLoveCountTv;
    private TextView myPLCountTv;
    private ImageView myPLArrowIv;
    private ImageView myPLAddIv;
    private ListView listView;
    private HomeListViewAdapter adapter;
    private final List<PlayListInfo> playListInfos = new ArrayList<>();
    private List<LrcRow> list;
    private int count;
    private boolean isOpenMyPL = false; //标识我的歌单列表打开状态
    private boolean isStartTheme = false;
    private WindowManager windowManager;
    private LyricView lyricView;
    private WindowManager.LayoutParams layoutParams;
    private FloatingReceiver fReceiver;
    private static boolean showFloat = false;
    private ActivityResultLauncher<Intent> floatResult;
    private int musicID;
    private ShutdownThread exitThread;
    public static int THEME_COLOR = Color.RED;
    private MenuItem nightItem;
    private MenuItem exitItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_home);
        dbManager = DBManager.getInstance(HomeActivity.this);
        toolbar = findViewById(R.id.home_activity_toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        nightItem = navView.getMenu().findItem(R.id.nav_night_mode);
        exitItem = navView.getMenu().findItem(R.id.nav_exit);
        View headerView = navView.getHeaderView(0);
        navHeadIv = headerView.findViewById(R.id.nav_head_bg_iv);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.drawer_menu);
        }
        dbManager.getMusicCount(Constant.LIST_ALLMUSIC);
        refreshNightModeTitle();
        navView.setNavigationItemSelectedListener(item -> {
            // mDrawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.nav_theme:
                    isStartTheme = true;
                    Intent intentTheme = new Intent(HomeActivity.this, ThemeActivity.class);
                    startActivity(intentTheme);
                    break;
                case R.id.nav_night_mode:
                    int preTheme = 0;
                    if (MyMusicUtil.getNightMode(HomeActivity.this)) {
                        //当前为夜间模式，则恢复之前的主题
                        MyMusicUtil.setNightMode(HomeActivity.this, false);
                        preTheme = MyMusicUtil.getPreTheme(HomeActivity.this);
                        MyMusicUtil.setTheme(HomeActivity.this, preTheme);
                    } else {
                        //当前为白天模式，则切换到夜间模式
                        MyMusicUtil.setNightMode(HomeActivity.this, true);
                        MyMusicUtil.setTheme(HomeActivity.this, ThemeActivity.THEME_SIZE - 1);
                    }
                    //                        Intent intentNight = new Intent(HomeActivity.this,HomeActivity.class);
                    //                        startActivity(intentNight);
                    recreate();
                    refreshNightModeTitle();
                    //                        overridePendingTransition(R.anim.start_anim,R.anim.out_anim);
                    break;
                case R.id.nav_exit:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    String[] items = new String[]{"不开启", "15分钟后", "30分钟后", "45分钟后", "60分钟后", "自定义"};
                    builder.setItems(items, (dialog, which) -> {
                        LocalDateTime dateTime = null;
                        switch (which) {
                            case 0:
                                if (exitThread != null) {
                                    exitThread.setExitTime(null);
                                    exitThread = null;
                                    exitItem.setTitle("定时关闭");
                                }
                                Toast.makeText(HomeActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                dateTime = LocalDateTime.now().plusMinutes(15);
                                break;
                            case 2:
                                dateTime = LocalDateTime.now().plusMinutes(30);
                                break;
                            case 3:
                                dateTime = LocalDateTime.now().plusMinutes(45);
                                break;
                            case 4:
                                dateTime = LocalDateTime.now().plusMinutes(60);
                                break;
                            case 5:
                                int h = 0, m = 0;
                                if (exitThread != null && exitThread.getExitTime() != null) {
                                    LocalDateTime time = exitThread.getExitTime();
                                    LocalDateTime localTime = LocalDateTime.now();
                                    h = time.getHour() - localTime.getHour();
                                    m = time.getMinute() - localTime.getMinute();
                                }
                                TimePickerDialog timePickerDialog = new TimePickerDialog(this, android.app.AlertDialog.THEME_HOLO_LIGHT, (view, hour, minute) -> {
                                    LocalDateTime time = LocalDateTime.now().plusHours(hour).plusMinutes(minute);
                                    if (exitThread != null) {
                                        exitThread.setExitTime(time);
                                    } else {
                                        exitThread = new ShutdownThread(time, HomeActivity.this);
                                        exitThread.start();
                                    }
                                    Toast.makeText(HomeActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                                }, h, m, true);
                                timePickerDialog.setTitle("自定义停止播放时间");
                                timePickerDialog.show();
                                break;
                        }
                        if (dateTime != null) {
                            if (exitThread != null) {
                                exitThread.setExitTime(dateTime);
                            } else {
                                exitThread = new ShutdownThread(dateTime, HomeActivity.this);
                                exitThread.start();
                            }
                            Toast.makeText(HomeActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    });
                    builder.show();
                    break;
                case R.id.nav_logout:
                    finish();
                    MyApplication.exitApp();
                    break;
            }
            return false;
        });
        floatResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Settings.canDrawOverlays(HomeActivity.this)) {
                        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                        if (showFloat) showFloatingWindow();
                    } else {
                        showFloat = false;
                        Intent i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        i.putExtra(Constant.COMMAND, Constant.COMMAND_UPDATE_NOTIFICATION);
                        sendBroadcast(i);
                        Toast.makeText(HomeActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                    }
                });
        init();
        register();

        Intent startIntent = new Intent(HomeActivity.this, MusicPlayerService.class);
        startForegroundService(startIntent);
    }

    private void refreshNightModeTitle() {
        if (MyMusicUtil.getNightMode(HomeActivity.this)) {
            nightItem.setTitle("日间模式");
        } else {
            nightItem.setTitle("夜间模式");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        count = dbManager.getMusicCount(Constant.LIST_ALLMUSIC);
        localMusicCountTv.setText(count + "");
        count = dbManager.getMusicCount(Constant.LIST_LASTPLAY);
        lastPlayCountTv.setText(count + "");
        count = dbManager.getMusicCount(Constant.LIST_MYLOVE);
        myLoveCountTv.setText(count + "");
        count = dbManager.getMusicCount(Constant.LIST_MYPLAY);
        myPLCountTv.setText("(" + count + ")");
        updateDataList();
    }

    private void init() {
        localMusicLl = findViewById(R.id.home_local_music_ll);
        lastPlayLl = findViewById(R.id.home_recently_music_ll);
        myLoveLl = findViewById(R.id.home_my_love_music_ll);
        myListTitleLl = findViewById(R.id.home_my_list_title_ll);
        listView = findViewById(R.id.home_my_list_lv);
        localMusicCountTv = findViewById(R.id.home_local_music_count_tv);
        lastPlayCountTv = findViewById(R.id.home_recently_music_count_tv);
        myLoveCountTv = findViewById(R.id.home_my_love_music_count_tv);
        myPLCountTv = findViewById(R.id.home_my_list_count_tv);
        myPLArrowIv = findViewById(R.id.home_my_pl_arror_iv);
        myPLAddIv = findViewById(R.id.home_my_pl_add_iv);

        localMusicLl.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LocalMusicActivity.class);
            startActivity(intent);
        });

        lastPlayLl.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LastMyloveActivity.class);
            intent.putExtra(Constant.LABEL, Constant.LABEL_LAST);
            startActivity(intent);
        });

        myLoveLl.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LastMyloveActivity.class);
            intent.putExtra(Constant.LABEL, Constant.LABEL_MYLOVE);
            startActivity(intent);
        });

        playListInfos.addAll(dbManager.getMyPlayList());
        adapter = new HomeListViewAdapter(playListInfos, this, dbManager);
        listView.setAdapter(adapter);
        myPLAddIv.setOnClickListener(v -> {
            //添加歌单
            final AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
            View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.dialog_create_playlist, null);
            final EditText playlistEt = view.findViewById(R.id.dialog_playlist_name_et);
            builder.setView(view);
            builder.setPositiveButton("确定", (dialog, which) -> {
                String name = playlistEt.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(HomeActivity.this, "请输入歌单名", Toast.LENGTH_SHORT).show();
                    return;
                }
                dbManager.createPlaylist(name);
                dialog.dismiss();
                updateDataList();
            });

            builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

            builder.show();//配置好后再builder show
        });
        myListTitleLl.setOnClickListener(v -> {
            //展现我的歌单
            if (isOpenMyPL) {
                isOpenMyPL = false;
                myPLArrowIv.setImageResource(R.drawable.arrow_right);
                listView.setVisibility(View.GONE);
            } else {
                isOpenMyPL = true;
                myPLArrowIv.setImageResource(R.drawable.arrow_down);
                listView.setVisibility(View.VISIBLE);
                updateDataList();
                // playListInfos = dbManager.getMyPlayList();
                // adapter = new HomeListViewAdapter(playListInfos, HomeActivity.this, dbManager);
                // listView.setAdapter(adapter);
            }
        });
        initFloatView();
    }

    public void updateDataList() {
        playListInfos.clear();
        playListInfos.addAll(dbManager.getMyPlayList());
        adapter.notifyDataSetChanged();
        count = dbManager.getMusicCount(Constant.LIST_MYPLAY);
        myPLCountTv.setText("(" + count + ")");
    }

    private void initFloatView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // 新建悬浮窗控件
        lyricView = new LyricView(this);
        lyricView.setLoadingTipText("无歌词");
        lyricView.setDesktopLrc(true);
        lyricView.setOnTouchListener(new View.OnTouchListener() {
            private int x;
            private int y;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        layoutParams.x = layoutParams.x + movedX;
                        layoutParams.y = layoutParams.y + movedY;

                        // 更新悬浮窗控件布局
                        windowManager.updateViewLayout(view, layoutParams);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        // 设置LayoutParam
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = 150;
        layoutParams.gravity = Gravity.TOP;
        if (showFloat) {
            showFloatingWindow();
        }
    }

    private void updateLyric(int id) {
        if (musicID != id) {
            musicID = id;
            if (musicID == - 1) {
                list = null;
                Toast.makeText(HomeActivity.this, "歌曲不存在", Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<String> info = dbManager.getMusicInfo(musicID);
                String path = info.get(9);
                if (path != null && path.length() > 0) {
                    list = LrcRow.createLrcRows(path);
                }
            }
            lyricView.setLrc(list);
        }

    }

    public void showFloatingWindow() {
        try {
            if (Settings.canDrawOverlays(HomeActivity.this)) {
                updateLyric(MyMusicUtil.getIntShared(Constant.KEY_ID));
                TypedValue value = new TypedValue();
                if (this.getTheme().resolveAttribute(R.attr.image_tint, value, true)) {
                    lyricView.setmHighLightRowColor(value.data);
                    if (value.data != Color.WHITE) THEME_COLOR = value.data;
                }
                windowManager.addView(lyricView, layoutParams);
            } else {
                Toast.makeText(HomeActivity.this, "无悬浮窗权限", Toast.LENGTH_SHORT).show();
                floatResult.launch(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            }
        } catch (Exception ignored) {
        }
        Intent i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        i.putExtra(Constant.COMMAND, Constant.COMMAND_UPDATE_NOTIFICATION);
        sendBroadcast(i);
    }

    private void hideFloatingWindow() {
        try {
            if (Settings.canDrawOverlays(HomeActivity.this)) {
                windowManager.removeView(lyricView);
            }
        } catch (Exception ignored) {
        }
        Intent i = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
        i.putExtra(Constant.COMMAND, Constant.COMMAND_UPDATE_NOTIFICATION);
        sendBroadcast(i);
    }

    private void register() {
        fReceiver = new FloatingReceiver();
        IntentFilter filter = new IntentFilter(Constant.FLOAT_RECEIVER);
        filter.addAction(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
        registerReceiver(fReceiver, filter);
    }

    private void unregister() {
        if (fReceiver != null) {
            unregisterReceiver(fReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        if (showFloat) {
            hideFloatingWindow();
        }
        unregister();
        if (exitThread != null) {
            exitThread.setExitTime(null);
            exitThread = null;
        }
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isStartTheme) {
            finish();
        }
        isStartTheme = false;
    }

    @Override
    public void onBackPressed() {
        mDrawerLayout.closeDrawers();
        if (! moveTaskToBack(true)) finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public MenuItem getExitItem() {
        return exitItem;
    }

    public static boolean getShowFloat() {
        return showFloat;
    }

    class FloatingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            if (intent.getBooleanExtra(Constant.FLOAT_CHANGE, false)) {
                if (showFloat) {
                    hideFloatingWindow();
                } else {
                    showFloatingWindow();
                }
                showFloat = ! showFloat;
            } else if (showFloat) {
                if (intent.getBooleanExtra("updateMusic", false)) {
                    updateLyric(MyMusicUtil.getIntShared(Constant.KEY_ID));
                }
                if (intent.getIntExtra(Constant.STATUS, 0) == Constant.STATUS_RUN) {
                    lyricView.seekLrcToTime(intent.getIntExtra(Constant.KEY_CURRENT, 0));
                }
            }
        }
    }
}
