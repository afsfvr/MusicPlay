package com.example.musicplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.R;
import com.example.musicplayer.activity.ModelActivity;
import com.example.musicplayer.adapter.AlbumAdapter;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.AlbumInfo;
import com.example.musicplayer.util.MyMusicUtil;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {

    private static final String TAG = AlbumFragment.class.getName();
    private RecyclerView recyclerView;
    private AlbumAdapter adapter;
    private final ArrayList<AlbumInfo> albumInfoList = new ArrayList<>();
    private DBManager dbManager;
    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    public void onResume() {
        super.onResume();
        albumInfoList.clear();
        albumInfoList.addAll(MyMusicUtil.groupByAlbum(dbManager.getAllMusicFromMusicTable()));
        Log.d(TAG, "onResume: albumInfoList.size() = " + albumInfoList.size());
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_singer, container, false);
        dbManager = DBManager.getInstance(getContext());
        recyclerView = view.findViewById(R.id.singer_recycler_view);
        adapter = new AlbumAdapter(getContext(), albumInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
            @Override
            public void onDeleteMenuClick(View content, int position) {

            }

            @Override
            public void onContentClick(View content, int position) {
                Intent intent = new Intent(mContext, ModelActivity.class);
                intent.putExtra(ModelActivity.KEY_TITLE, albumInfoList.get(position).getName());
                intent.putExtra(ModelActivity.KEY_TYPE, ModelActivity.ALBUM_TYPE);
                mContext.startActivity(intent);
            }
        });
        return view;
    }
}
