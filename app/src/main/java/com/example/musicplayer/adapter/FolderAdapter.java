package com.example.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.R;
import com.example.musicplayer.entity.FolderInfo;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private static final String TAG = FolderAdapter.class.getName();
    private final List<FolderInfo> folderInfoList;
    private final Context context;
    private FolderAdapter.OnItemClickListener onItemClickListener;

    public FolderAdapter(Context context, List<FolderInfo> folderInfoList) {
        this.context = context;
        this.folderInfoList = folderInfoList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View swipeContent;
        LinearLayout contentLl;
        ImageView folderIv;
        TextView folderName;
        TextView count;
        Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = itemView.findViewById(R.id.model_swipemenu_layout);
            this.contentLl = itemView.findViewById(R.id.model_music_item_ll);
            this.folderIv = itemView.findViewById(R.id.model_head_iv);
            this.folderName = itemView.findViewById(R.id.model_item_name);
            this.count = itemView.findViewById(R.id.model_music_count);
            this.deleteBtn = itemView.findViewById(R.id.model_swip_delete_menu_btn);
        }

    }

    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item, parent, false);
        return new FolderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FolderAdapter.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: position = " + position);
        FolderInfo folder = folderInfoList.get(position);
        holder.folderIv.setImageResource(R.drawable.folder);
        holder.folderName.setText(folder.getName());
        holder.count.setText("" + folder.getCount() + "首" + folder.getPath());
        holder.contentLl.setOnClickListener(v -> onItemClickListener.onContentClick(holder.contentLl, position));

        holder.deleteBtn.setOnClickListener(v -> onItemClickListener.onDeleteMenuClick(holder.swipeContent, position));
    }

    @Override
    public int getItemCount() {
        return folderInfoList.size();
    }

    public interface OnItemClickListener {
        void onDeleteMenuClick(View content, int position);

        void onContentClick(View content, int position);
    }

    public void setOnItemClickListener(FolderAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
