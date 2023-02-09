package com.example.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.musicplayer.R;
import com.example.musicplayer.entity.FolderInfo;

import java.util.List;

public class ChooseDirAdapter extends BaseAdapter {

    private final List<FolderInfo> dataList;
    private final Context context;

    public ChooseDirAdapter(List<FolderInfo> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        if (dataList.size() == 0) return 1;
        else return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.choose_dir_item, null, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (dataList.get(position).getCount() > 0) {
            holder.folderIv.setImageResource(R.drawable.file);
        } else {
            holder.folderIv.setImageResource(R.drawable.folder);
        }
        if (dataList.size() == 0) {
            holder.folderIv.setImageResource(R.drawable.cancel);
            holder.folderName.setText("无文件夹");
        } else {
            FolderInfo folderInfo = dataList.get(position);
            holder.folderName.setText(folderInfo.getName());
        }
        return view;
    }

    public void updateData() {
        notifyDataSetChanged();
    }

    static class ViewHolder {
        LinearLayout contentLl;
        ImageView folderIv;
        TextView folderName;

        public ViewHolder(View itemView) {
            this.contentLl = itemView.findViewById(R.id.model_music_item_ll);
            this.folderIv = itemView.findViewById(R.id.model_head_iv);
            this.folderName = itemView.findViewById(R.id.model_item_name);
            contentLl.setClickable(false);
            folderIv.setClickable(false);
            folderName.setClickable(false);
        }

    }
}
