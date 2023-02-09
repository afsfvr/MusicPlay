package com.example.musicplayer.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.musicplayer.R;
import com.example.musicplayer.activity.PlayActivity;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.receiver.PlayerManagerReceiver;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;
import com.example.musicplayer.view.PlayingPopWindow;

import java.util.ArrayList;

public class PlayBarFragment extends Fragment {

    private static final String TAG = PlayBarFragment.class.getName();
    public static final String ACTION_UPDATE_UI_PlayBar = "com.example.musicplayer.fragment.PlayBarFragment:action_update_ui_broad_cast";
    private LinearLayout playBarLl;
    private ImageView playIv;
    private SeekBar seekBar;
    private ImageView nextIv;
    private ImageView menuIv;
    private ImageView rotateIv;
    private TextView musicNameTv;
    private TextView singerNameTv;
    private HomeReceiver mReceiver;
    private DBManager dbManager;
    private View view;
    private Context context;
    private ColorStateList tint1;
    private ColorStateList tint2;


    public static synchronized PlayBarFragment newInstance() {
        return new PlayBarFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = DBManager.getInstance(getActivity());
        register();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.fragment_playbar, container, false);
        playBarLl = view.findViewById(R.id.home_activity_playbar_ll);
        seekBar = view.findViewById(R.id.home_seekbar);
        playIv = view.findViewById(R.id.play_iv);
        menuIv = view.findViewById(R.id.play_menu_iv);
        nextIv = view.findViewById(R.id.next_iv);
        rotateIv = view.findViewById(R.id.album_picture_iv);
        musicNameTv = view.findViewById(R.id.home_music_name_tv);
        singerNameTv = view.findViewById(R.id.home_singer_name_tv);

        tint1 = rotateIv.getImageTintList();
        tint2 = playIv.getImageTintList();

        setMusicName();
        initPlayIv();
        setFragmentBb();
        playBarLl.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PlayActivity.class);
            intent.putExtra(Constant.KEY_CURRENT, seekBar.getProgress());
            startActivity(intent);
        });

        playIv.setOnClickListener(v -> {
            int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
            if (musicId == - 1 || musicId == 0) {
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                getActivity().sendBroadcast(intent);
                Toast.makeText(getActivity(), "歌曲不存在", Toast.LENGTH_SHORT).show();
            } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PAUSE) {
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                getActivity().sendBroadcast(intent);
            } else if (PlayerManagerReceiver.getMediaPlayerStatus() == Constant.STATUS_PLAY) {
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                getActivity().sendBroadcast(intent);
            } else {
                //为停止状态时发送播放命令，并发送将要播放歌曲的路径
                String path = dbManager.getMusicPath(musicId);
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                intent.putExtra(Constant.KEY_PATH, path);
                Log.i(TAG, "onClick: path = " + path);
                getActivity().sendBroadcast(intent);
            }
        });

        nextIv.setOnClickListener(v -> MyMusicUtil.playNextMusic(getActivity()));

        menuIv.setOnClickListener(v -> showPopFormBottom());
        return view;
    }

    public void showPopFormBottom() {
        PlayingPopWindow playingPopWindow = new PlayingPopWindow(getActivity());
        //      设置Popupwindow显示位置（从底部弹出）
        playingPopWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        getActivity().getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        playingPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha = 1f;
                getActivity().getWindow().setAttributes(params);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    public void setFragmentBb() {
        //获取播放控制栏颜色
        int defaultColor = 0xFFFFFF;
        int[] attrsArray = {R.attr.play_bar_color};
        TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
        int color = typedArray.getColor(0, defaultColor);
        typedArray.recycle();
        playBarLl.setBackgroundColor(color);
    }

    private void register() {
        mReceiver = new HomeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_UPDATE_UI_PlayBar);
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    private void unRegister() {
        if (mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }
    }

    private void setMusicName() {
        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
        Log.i(TAG, "setMusicName musicID = " + musicId);
        if (musicId == - 1) {
            musicNameTv.setText("听听音乐");
            singerNameTv.setText("好音质");
            rotateIv.setImageTintList(tint2);
            rotateIv.setImageResource(R.drawable.album);
        } else {
            ArrayList<String> musicInfo = dbManager.getMusicInfo(musicId);
            musicNameTv.setText(musicInfo.get(1));
            singerNameTv.setText(musicInfo.get(2));
            try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                mmr.setDataSource(musicInfo.get(5));
                byte[] byte_pic = mmr.getEmbeddedPicture();
                Bitmap bitmap = BitmapFactory.decodeByteArray(byte_pic, 0, byte_pic.length);
                rotateIv.setImageBitmap(bitmap);
                rotateIv.setImageTintList(tint1);
                mmr.release();
            } catch (Exception e) {
                rotateIv.setImageTintList(tint2);
                rotateIv.setImageResource(R.drawable.album);
            }
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

    class HomeReceiver extends BroadcastReceiver {

        int status;
        int duration;
        int current;

        @Override
        public void onReceive(Context context, Intent intent) {
            status = intent.getIntExtra(Constant.STATUS, 0);
            Log.d(TAG, "onReceive: " + status);
            current = intent.getIntExtra(Constant.KEY_CURRENT, 0);
            duration = intent.getIntExtra(Constant.KEY_DURATION, 100);
            if (intent.getBooleanExtra("updateMusic", false)) {
                setMusicName();
            }
            switch (status) {
                case Constant.STATUS_STOP:
                    playIv.setSelected(false);
                    seekBar.setProgress(0);
                    break;
                case Constant.STATUS_PLAY:
                    playIv.setSelected(true);
                    break;
                case Constant.STATUS_PAUSE:
                    playIv.setSelected(false);
                    break;
                case Constant.STATUS_RUN:
                    playIv.setSelected(true);
                    seekBar.setMax(duration);
                    seekBar.setProgress(current);
                    break;
                default:
                    break;
            }

        }
    }
}
