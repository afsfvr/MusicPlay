package com.example.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.R;
import com.example.musicplayer.activity.ModelActivity;
import com.example.musicplayer.adapter.FolderAdapter;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.FolderInfo;
import com.example.musicplayer.entity.MusicInfo;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.MyMusicUtil;
import com.example.musicplayer.view.MusicPopMenuWindow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FolderFragment extends Fragment {

    private static final String TAG = FolderFragment.class.getName();
    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private final List<FolderInfo> folderInfoList = new ArrayList<>();
    private DBManager dbManager;
    private Context mContext;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_singer, container, false);
        dbManager = DBManager.getInstance(getContext());
        recyclerView = view.findViewById(R.id.singer_recycler_view);
        adapter = new FolderAdapter(getContext(), folderInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new FolderAdapter.OnItemClickListener() {
            @Override
            public void onDeleteMenuClick(View swipeView, int position) {
                FolderInfo folderInfo = folderInfoList.get(position);
                LayoutInflater inflater = LayoutInflater.from(mContext);
                View view = inflater.inflate(R.layout.dialog_delete_file, null);
                ((TextView) view.findViewById(R.id.delete_text)).setText("确定将所选文件夹从列表中移除吗？");
                final int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
                final CheckBox deleteFile = view.findViewById(R.id.dialog_delete_cb);
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setView(view);
                builder.setPositiveButton("删除", (dialog, which) -> {
                    List<MusicInfo> musicListByFolder = dbManager.getMusicListByFolder(folderInfo.getPath());
                    musicListByFolder.forEach(musicInfo -> deleteMusic(musicInfo, mContext, deleteFile.isChecked(), musicId));
                    folderInfoList.remove(folderInfo);
                    //                    folderInfoList.addAll(MyMusicUtil.groupByFolder(dbManager.getAllMusicFromMusicTable()));
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                });
                builder.setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                });
                builder.show();
            }

            @Override
            public void onContentClick(View content, int position) {
                Intent intent = new Intent(mContext, ModelActivity.class);
                intent.putExtra(ModelActivity.KEY_TITLE, folderInfoList.get(position).getName());
                intent.putExtra(ModelActivity.KEY_TYPE, ModelActivity.FOLDER_TYPE);
                intent.putExtra(ModelActivity.KEY_PATH, folderInfoList.get(position).getPath());
                mContext.startActivity(intent);
            }
        });
        return view;
    }

    public void deleteMusic(final MusicInfo musicInfo, final Context context, boolean deleteFile, final int musicId) {
        final int curId = musicInfo.getId();
        dbManager.removeMusic(curId, Constant.ACTIVITY_LOCAL);
        if (deleteFile) {       //同时删除文件
            File file = new File(musicInfo.getPath());
            if (file.exists()) {
                boolean ret = file.delete();
                MusicPopMenuWindow.deleteMediaDB(file, context);
                Log.w(TAG, "删除歌曲 = " + ret);
                dbManager.deleteMusic(curId);
                String lrcPath = musicInfo.getLrcPath();
                if (lrcPath != null && lrcPath.length() > 0) {
                    Log.w(TAG, "删除歌词 = " + new File(lrcPath).delete());
                }
            } else {
                Toast.makeText(context, "找不到文件", Toast.LENGTH_SHORT).show();
            }
        }
        if (curId == musicId) {
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            context.sendBroadcast(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        folderInfoList.clear();
        folderInfoList.addAll(MyMusicUtil.groupByFolder(dbManager.getAllMusicFromMusicTable()));
        Log.d(TAG, "onResume: folderInfoList.size() = " + folderInfoList.size());
        adapter.notifyDataSetChanged();
    }
}
