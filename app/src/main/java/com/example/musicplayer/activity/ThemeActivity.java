package com.example.musicplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.R;
import com.example.musicplayer.entity.ThemeInfo;
import com.example.musicplayer.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;

public class ThemeActivity extends BaseActivity {
    public static int THEME_SIZE = 11;
    private String[] themeType = {"哔哩粉", "知乎蓝", "酷安绿", "网易红", "藤萝紫", "碧海蓝", "樱草绿", "咖啡棕", "柠檬橙", "星空灰", "夜间模式"};
    private int[] colors = {R.color.biliPink, R.color.zhihuBlue, R.color.kuanGreen, R.color.cloudRed,
            R.color.tengluoPurple, R.color.seaBlue, R.color.grassGreen, R.color.coffeeBrown,
            R.color.lemonOrange, R.color.startSkyGray, R.color.nightActionbar};

    private static final String TAG = ThemeActivity.class.getName();
    private RecyclerView recyclerView;
    private ThemeAdapter adapter;
    private Toolbar toolbar;
    private int selectTheme = 0;
    private final List<ThemeInfo> themeInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        selectTheme = MyMusicUtil.getTheme(ThemeActivity.this);
        toolbar = findViewById(R.id.theme_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        init();
    }

    private void init() {
        for (int i = 0; i < themeType.length; i++) {
            ThemeInfo info = new ThemeInfo();
            info.setName(themeType[i]);
            info.setColor(colors[i]);
            info.setSelect(selectTheme == i);
            if (i == themeType.length - 1) {
                info.setBackground(R.color.nightBg);
            } else {
                info.setBackground(R.color.colorWhite);
            }
            themeInfoList.add(info);
        }
        recyclerView = findViewById(R.id.theme_rv);
        adapter = new ThemeAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ThemeActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(ThemeActivity.this, HomeActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return true;
    }

    private class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {

        public ThemeAdapter() {
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout relativeLayout;
            ImageView circleIv;
            TextView nameTv;
            Button selectBtn;

            public ViewHolder(View itemView) {
                super(itemView);
                this.relativeLayout = itemView.findViewById(R.id.theme_item_rl);
                this.circleIv = itemView.findViewById(R.id.theme_iv);
                this.nameTv = itemView.findViewById(R.id.theme_name_tv);
                this.selectBtn = itemView.findViewById(R.id.theme_select_tv);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ThemeActivity.this).inflate(R.layout.change_theme_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final ThemeInfo themeInfo = themeInfoList.get(position);
            if (selectTheme == THEME_SIZE - 1) {
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_night);
                holder.selectBtn.setBackgroundResource(R.drawable.shape_theme_btn_night);
            } else {
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_day);
                holder.selectBtn.setBackgroundResource(R.drawable.shape_theme_btn_day);
            }
            holder.selectBtn.setPadding(0, 0, 0, 0);
            if (themeInfo.isSelect()) {
                holder.circleIv.setImageResource(R.drawable.tick);
                holder.selectBtn.setText("使用中");
                holder.selectBtn.setTextColor(getResources().getColor(themeInfo.getColor()));
            } else {
                holder.circleIv.setImageBitmap(null);
                holder.selectBtn.setText("使用");
                holder.selectBtn.setTextColor(getResources().getColor(R.color.grey500));
            }
            holder.circleIv.setBackgroundResource(themeInfo.getColor());
            holder.nameTv.setTextColor(getResources().getColor(themeInfo.getColor()));
            holder.nameTv.setText(themeInfo.getName());
            holder.selectBtn.setOnClickListener(v -> refreshTheme(themeInfo, position));
        }

        @Override
        public int getItemCount() {
            return themeInfoList.size();
        }
    }

    private void refreshTheme(ThemeInfo themeInfo, int position) {
        try {
            if (position == (THEME_SIZE - 1)) {
                MyMusicUtil.setNightMode(ThemeActivity.this, true);
            } else if (MyMusicUtil.getNightMode(ThemeActivity.this)) {
                MyMusicUtil.setNightMode(ThemeActivity.this, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectTheme = position;
        MyMusicUtil.setTheme(ThemeActivity.this, position);
        toolbar.setBackgroundColor(getResources().getColor(themeInfo.getColor()));
        recyclerView.setBackgroundColor(getResources().getColor(themeInfo.getBackground()));
        getWindow().setStatusBarColor(getResources().getColor(themeInfo.getColor()));
        for (ThemeInfo info : themeInfoList) {
            info.setSelect(info.getName().equals(themeInfo.getName()));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
