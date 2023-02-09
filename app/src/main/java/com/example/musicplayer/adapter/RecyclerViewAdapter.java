package com.example.musicplayer.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.R;
import com.example.musicplayer.database.DBManager;
import com.example.musicplayer.entity.MusicInfo;
import com.example.musicplayer.service.MusicPlayerService;
import com.example.musicplayer.util.Constant;
import com.example.musicplayer.util.CustomAttrValueUtil;
import com.example.musicplayer.util.MyMusicUtil;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
        implements SectionIndexer {

    private static final String TAG = RecyclerViewAdapter.class.getName();
    private final List<MusicInfo> musicInfoList;
    private final Context context;
    private DBManager dbManager;
    private OnItemClickListener onItemClickListener;
    private boolean showLetter;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View swipeContent;
        LinearLayout contentLl;
        TextView musicIndex;
        TextView musicName;
        TextView musicSinger;
        TextView letterIndex;
        ImageView menuIv;
        Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = itemView.findViewById(R.id.swipemenu_layout);
            this.contentLl = itemView.findViewById(R.id.local_music_item_ll);
            this.musicName = itemView.findViewById(R.id.local_music_name);
            this.musicIndex = itemView.findViewById(R.id.local_index);
            this.musicSinger = itemView.findViewById(R.id.local_music_singer);
            this.letterIndex = itemView.findViewById(R.id.indext_head_tv);
            this.menuIv = itemView.findViewById(R.id.local_music_item_never_menu);
            this.deleteBtn = itemView.findViewById(R.id.swip_delete_menu_btn);
        }

    }

    public RecyclerViewAdapter(Context context, List<MusicInfo> musicInfoList) {
        this(context, musicInfoList, true);
    }

    public RecyclerViewAdapter(Context context, List<MusicInfo> musicInfoList, boolean showLetter) {
        this.context = context;
        this.musicInfoList = musicInfoList;
        this.dbManager = DBManager.getInstance(context);
        this.showLetter = showLetter;
    }


    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的item的位置
     */
    public int getPositionForSection(int section) {
        Log.i(TAG, "getPositionForSection: section = " + section);
        for (int i = 0; i < getItemCount(); i++) {
            char firstChar = musicInfoList.get(i).getFirstLetter().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return - 1;
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    @Override
    public int getSectionForPosition(int position) {
        return musicInfoList.get(position).getFirstLetter().charAt(0);
    }

    @Override
    public int getItemCount() {
        return musicInfoList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_music_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MusicInfo musicInfo = musicInfoList.get(position);
        holder.musicName.setText(musicInfo.getName());
        holder.musicIndex.setText("" + (position + 1));
        holder.musicSinger.setText(musicInfo.getSinger());

        int appbg = CustomAttrValueUtil.getAttrColorValue(R.attr.colorAccent, 0xFFFA7298, context);
        int defaultTvColor = CustomAttrValueUtil.getAttrColorValue(R.attr.text_color, R.color.grey700, context);

        if (musicInfo.getId() == MyMusicUtil.getIntShared(Constant.KEY_ID)) {
            holder.musicName.setTextColor(appbg);
            holder.musicIndex.setTextColor(appbg);
            holder.musicSinger.setTextColor(appbg);
        } else {
            holder.musicName.setTextColor(defaultTvColor);
            holder.musicIndex.setTextColor(context.getResources().getColor(R.color.grey700));
            holder.musicSinger.setTextColor(context.getResources().getColor(R.color.grey700));
        }
        int section = getSectionForPosition(position);
        int firstPosition = getPositionForSection(section);
        if (showLetter) {
            if (firstPosition == position) {
                holder.letterIndex.setVisibility(View.VISIBLE);
                holder.letterIndex.setText("" + musicInfo.getFirstLetter());
            } else {
                holder.letterIndex.setVisibility(View.GONE);
            }
        }

        holder.contentLl.setOnClickListener(v -> {
            Log.i(TAG, "onClick: 播放 " + musicInfo.getName());
            String path = dbManager.getMusicPath(musicInfo.getId());
            Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            intent.putExtra(Constant.KEY_PATH, path);
            context.sendBroadcast(intent);
            MyMusicUtil.setShared(Constant.KEY_ID, musicInfo.getId());
            notifyDataSetChanged();
            if (onItemClickListener != null)
                onItemClickListener.onContentClick(position);
        });

        holder.menuIv.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onOpenMenuClick(position);
        });

        holder.deleteBtn.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onDeleteMenuClick(holder.swipeContent, holder.getAdapterPosition());
        });
    }

    public void updateMusicInfoList() {
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onOpenMenuClick(int position);

        void onDeleteMenuClick(View content, int position);

        void onContentClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


}
