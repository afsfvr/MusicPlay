package com.example.musicplayer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer.R;
import com.example.musicplayer.entity.SingerInfo;

import java.util.List;

public class SingerAdapter extends RecyclerView.Adapter<SingerAdapter.ViewHolder> {

    private static final String TAG = SingerAdapter.class.getName();
    private final List<SingerInfo> singerInfoList;
    private final Context context;
    private OnItemClickListener onItemClickListener;

    public SingerAdapter(Context context, List<SingerInfo> singerInfoList) {
        this.context = context;
        this.singerInfoList = singerInfoList;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View swipeContent;
        LinearLayout contentLl;
        ImageView singerIv;
        TextView singelName;
        TextView count;
        //        Button deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = (View) itemView.findViewById(R.id.model_swipemenu_layout);
            this.contentLl = (LinearLayout) itemView.findViewById(R.id.model_music_item_ll);
            this.singerIv = (ImageView) itemView.findViewById(R.id.model_head_iv);
            this.singelName = (TextView) itemView.findViewById(R.id.model_item_name);
            this.count = (TextView) itemView.findViewById(R.id.model_music_count);
            //            this.deleteBtn = (Button) itemView.findViewById(R.id.model_swip_delete_menu_btn);
        }

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: position = " + position);
        SingerInfo singer = singerInfoList.get(position);
        holder.singelName.setText(singer.getName());
        holder.singerIv.setImageResource(R.drawable.singer);
        holder.count.setText(singer.getCount() + "首");
        holder.contentLl.setOnClickListener(v -> onItemClickListener.onContentClick(holder.contentLl, position));
    }

    @Override
    public int getItemCount() {
        return singerInfoList.size();
    }

    public interface OnItemClickListener {
        void onDeleteMenuClick(View content, int position);

        void onContentClick(View content, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
