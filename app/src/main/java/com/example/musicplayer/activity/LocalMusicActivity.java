package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.example.musicplayer.R;
import com.example.musicplayer.fragment.AlbumFragment;
import com.example.musicplayer.fragment.FolderFragment;
import com.example.musicplayer.fragment.SingerFragment;
import com.example.musicplayer.fragment.SingleFragment;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.view.MyViewPager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicActivity extends PlayBarBaseActivity {

    private static final String TAG = LocalMusicActivity.class.getName();
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private MyViewPager viewPager;
    private MyAdapter fragmentAdapter;
    private List<String> titleList = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    private SingleFragment singleFragment;
    private SingerFragment singerFragment;
    private AlbumFragment albumFragment;
    private FolderFragment folderFragment;
    private TextView nothingTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        toolbar = findViewById(R.id.local_music_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Constant.LABEL_LOCAL);
        }
        init();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }


    private void init() {
        addTapData();
        viewPager = findViewById(R.id.local_viewPager);
        tabLayout = findViewById(R.id.local_tab);
        fragmentAdapter = new MyAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2); //预加载页面数
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        nothingTv = findViewById(R.id.local_nothing_tv);
        nothingTv.setOnClickListener(v -> {
            Intent intent = new Intent(LocalMusicActivity.this, ScanActivity.class);
            startActivity(intent);
        });
    }


    //滑动布局
    private void addTapData() {
        titleList.add("单曲");
        titleList.add("歌手");
        titleList.add("专辑");
        titleList.add("文件夹");

        if (singleFragment == null) {
            singleFragment = new SingleFragment();
            fragments.add(singleFragment);
        }
        if (singerFragment == null) {
            singerFragment = new SingerFragment();
            fragments.add(singerFragment);
        }
        if (albumFragment == null) {
            albumFragment = new AlbumFragment();
            fragments.add(albumFragment);
        }
        if (folderFragment == null) {
            folderFragment = new FolderFragment();
            fragments.add(folderFragment);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.local_music_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            try {
                fragments.forEach(Fragment::onResume);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.scan_local_menu) {
            Intent intent = new Intent(LocalMusicActivity.this, ScanActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == android.R.id.home) {
            this.finish();
        } else if (item.getItemId() == R.id.add_dir) {
            Intent intent = new Intent(LocalMusicActivity.this, ChooseDirActivity.class);
            intent.putExtra(Constant.TITLE, "长按选择文件夹");
            startActivityForResult(intent, 100);
        } else if (item.getItemId() == R.id.delete_all) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.dialog_delete_file, null);
            ((TextView) view.findViewById(R.id.delete_text)).setText("确认删除所有歌曲吗？");
            final CheckBox deleteFile = view.findViewById(R.id.dialog_delete_cb);
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view);
            builder.setPositiveButton("删除", (dialog, which) -> {
                singleFragment.deleteAll(deleteFile.isChecked());
                try {
                    fragments.forEach(Fragment::onResume);
                } catch (Exception ignored) {
                }
                dialog.dismiss();
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.show();
        }
        return true;
    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * 用来显示tab上的名字
         * @param position
         * @return
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }

}
