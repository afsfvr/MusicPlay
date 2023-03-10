package com.example.musicplayer.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.example.musicplayer.R;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.MusicInfo;
import com.example.musicplayer.entity.PlayListInfo;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;

import java.io.File;

public class MusicPopMenuWindow extends PopupWindow {

    private static final String TAG = MusicPopMenuWindow.class.getName();
    private View view;
    private Activity activity;
    private TextView nameTv;
    //    private LinearLayout playLl;
    private LinearLayout addLl;
    private LinearLayout loveLl;
    //    private LinearLayout ringLl;
    private LinearLayout deleteLl;
    private LinearLayout cancelLl;
    private ImageView loveIv;
    private MusicInfo musicInfo;
    private PlayListInfo playListInfo;
    private int witchActivity = Constant.ACTIVITY_LOCAL;
    private View parentView;

    public MusicPopMenuWindow(Activity activity, MusicInfo musicInfo, View parentView, int witchActivity) {
        super(activity);
        this.activity = activity;
        this.musicInfo = musicInfo;
        this.parentView = parentView;
        this.witchActivity = witchActivity;
        initView();
    }

    public MusicPopMenuWindow(Activity activity, MusicInfo musicInfo, View parentView, int witchActivity, PlayListInfo playListInfo) {
        super(activity);
        this.activity = activity;
        this.musicInfo = musicInfo;
        this.parentView = parentView;
        this.witchActivity = witchActivity;
        this.playListInfo = playListInfo;
        initView();
    }

    private void initView() {
        this.view = LayoutInflater.from(activity).inflate(R.layout.pop_window_menu, null);
        // ????????????
        this.setContentView(this.view);
        // ??????????????????????????????,????????????????????????
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);

        // ???????????????????????????
        this.setFocusable(true);
        // ?????????????????????
        this.setOutsideTouchable(true);

        // ???????????????????????????
        this.setBackgroundDrawable(activity.getResources().getDrawable(R.color.colorWhite));

        // ????????????????????????????????????????????????????????????
        this.setAnimationStyle(R.style.pop_window_animation);


        // ??????OnTouchListener???????????????????????????????????????????????????????????????????????????
        this.view.setOnTouchListener((v, event) -> {
            int height = view.getTop();
            int y = (int) event.getY();
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (y < height) {
                    dismiss();
                }
            }
            return true;
        });


        nameTv = view.findViewById(R.id.popwin_name_tv);
        //        playLl = ) view.findViewById(R.id.popwin_play_ll);
        addLl = view.findViewById(R.id.popwin_add_rl);
        loveLl = view.findViewById(R.id.popwin_love_ll);
        //        ringLl =  view.findViewById(R.id.popwin_ring_ll);
        deleteLl = view.findViewById(R.id.popwin_delete_ll);
        cancelLl = view.findViewById(R.id.popwin_cancel_ll);
        loveIv = view.findViewById(R.id.popwin_love_iv);

        nameTv.setText("????????? " + musicInfo.getName());
        if (musicInfo.getLove() == 1) {
            loveIv.setImageResource(R.drawable.love_hover);
        }

        //        playLl.setOnClickListener(new View.OnClickListener() {
        //
        //            public void onClick(View v) {
        //                MyMusicUtil.playNextMusic(activity);
        //
        //                dismiss();
        //            }
        //        });

        addLl.setOnClickListener(v -> {
            dismiss();
            AddPlaylistWindow addPlaylistWindow = new AddPlaylistWindow(activity, musicInfo);
            addPlaylistWindow.showAtLocation(parentView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
            WindowManager.LayoutParams params = activity.getWindow().getAttributes();
            //?????????Popupwindow????????????????????????
            params.alpha = 0.7f;
            activity.getWindow().setAttributes(params);
            //??????Popupwindow??????????????????Popupwindow?????????????????????1f
            addPlaylistWindow.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                    params.alpha = 1f;
                    activity.getWindow().setAttributes(params);
                }
            });
        });

        loveLl.setOnClickListener(v -> {
            dismiss();
            View view = LayoutInflater.from(activity).inflate(R.layout.my_love_toast, null);
            if (musicInfo.getLove() == 1) {
                DBManager.getInstance(activity).removeMusic(musicInfo.getId(), Constant.ACTIVITY_MYLOVE);
                ((TextView) view.findViewById(R.id.love_text)).setText("??????????????????????????????");
                musicInfo.setLove(0);
            } else {
                MyMusicUtil.setMusicMylove(activity, musicInfo.getId());
                musicInfo.setLove(1);
            }
            Toast toast = new Toast(activity);
            toast.setView(view);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        });

        //        ringLl.setOnClickListener(new View.OnClickListener() {
        //
        //            public void onClick(View v) {
        //                MyMusicUtil.setMyRingtone(activity);
        //                dismiss();
        //            }
        //        });

        deleteLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                deleteOperate(musicInfo, activity);
                dismiss();
            }
        });

        cancelLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dismiss();
            }
        });


    }

    public void deleteOperate(MusicInfo musicInfo, final Context context) {
        final int curId = musicInfo.getId();
        final int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
        final DBManager dbManager = DBManager.getInstance(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_delete_file, null);
        final CheckBox deleteFile = view.findViewById(R.id.dialog_delete_cb);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.create();

        builder.setView(view);

        builder.setPositiveButton("??????", (dialog1, which) -> {
            if (deleteFile.isChecked()) {
                //??????????????????
                //?????????????????????????????????
                File file = new File(musicInfo.getPath());
                if (file.exists()) {
                    boolean ret = file.delete();
                    deleteMediaDB(file, context);
                    Log.w(TAG, "???????????? = " + ret);
                    dbManager.deleteMusic(curId);
                    String lrcPath = musicInfo.getLrcPath();
                    if (lrcPath != null && lrcPath.length() > 0) {
                        Log.w(TAG, "???????????? = " + new File(lrcPath).delete());
                    }
                }
                if (curId == musicId) {
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    context.sendBroadcast(intent);
                    MyMusicUtil.setShared(Constant.KEY_ID, dbManager.getFirstId());
                }
            } else {
                //???????????????
                if (witchActivity == Constant.ACTIVITY_MYLIST) {
                    dbManager.removeMusicFromPlaylist(curId, playListInfo.getId());
                } else {
                    dbManager.removeMusic(curId, witchActivity);
                }

                if (curId == musicId) {
                    //?????????????????????????????????
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    context.sendBroadcast(intent);
                }
            }
            if (onDeleteUpdateListener != null) {
                onDeleteUpdateListener.onDeleteUpdate();
            }
            dialog1.dismiss();

        });
        builder.setNegativeButton("??????", (dialog12, which) -> dialog12.dismiss());

        builder.show();
    }

    public static void deleteMediaDB(File file, Context context) {
        String filePath = file.getPath();
        int res = context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.DATA + "= \"" + filePath + "\"",
                null);
        Log.i(TAG, "deleteMediaDB: res = " + res);
    }

    private OnDeleteUpdateListener onDeleteUpdateListener;

    public void setOnDeleteUpdateListener(OnDeleteUpdateListener onDeleteUpdateListener) {
        this.onDeleteUpdateListener = onDeleteUpdateListener;
    }

    public interface OnDeleteUpdateListener {
        void onDeleteUpdate();
    }
}
