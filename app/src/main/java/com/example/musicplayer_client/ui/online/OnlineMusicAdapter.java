package com.example.musicplayer_client.ui.online;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.model.Song;
import com.example.musicplayer_client.utils.Constants;

import java.util.List;

public class OnlineMusicAdapter extends RecyclerView.Adapter<OnlineMusicAdapter.MusicViewHolder> {
    private List<Song> data;
    private int playingIndex = -1;
    private OnItemClickListener onItemClickListener;
    private static final String BASE_URL = Constants.BASE_URL + ":8080/";

    public OnlineMusicAdapter(List<Song> data) {
        this.data = data;
    }

    public void setPlayingIndex(int index) {
        this.playingIndex = index;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        Song song = data.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        String coverUrl = song.getCoverUrl();
        if (coverUrl != null && coverUrl.startsWith("/")) {
            coverUrl = coverUrl.substring(1);
        }
        String fullCoverUrl = BASE_URL + coverUrl;
        Log.d("OnlineMusicAdapter", "加载封面: " + fullCoverUrl + ", 歌曲: " + song.getTitle());
        Glide.with(holder.itemView.getContext())
            .load(fullCoverUrl)
            .placeholder(R.drawable.ic_music_placeholder)
            .error(R.drawable.ic_music_placeholder)
            .transform(new com.bumptech.glide.load.resource.bitmap.CenterCrop(), new com.bumptech.glide.load.resource.bitmap.RoundedCorners(12))
            .into(holder.ivCover);
        // 高亮当前播放项
        if (position == playingIndex) {
            holder.itemView.setBackgroundResource(R.color.apple_music_selected);
        } else {
            holder.itemView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
        holder.itemView.setOnClickListener(v -> {
            Log.d("OnlineMusicAdapter", "点击item: " + song.getTitle() + ", position=" + position);
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(song, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvArtist;
        MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Song song, int position);
    }
} 