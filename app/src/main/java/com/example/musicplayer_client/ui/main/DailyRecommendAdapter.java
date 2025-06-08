package com.example.musicplayer_client.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.model.Song;
import com.example.musicplayer_client.utils.Constants;

import java.util.List;
import android.util.Log;

public class DailyRecommendAdapter extends RecyclerView.Adapter<DailyRecommendAdapter.ViewHolder> {
    private final HomeFragment parentFragment;
    private OnItemClickListener onItemClickListener;

    public DailyRecommendAdapter(HomeFragment fragment) {
        this.parentFragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_recommend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = parentFragment.getCurrentDisplayList().get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        Log.d("Adapter", "onBindViewHolder: position=" + position + ", title=" + song.getTitle() + ", url=" + song.getUrl() + ", isOnline=" + song.isOnline());
        if (song.isOnline()) {
            String coverUrl = song.getCoverUrl();
            if (coverUrl != null && coverUrl.startsWith("/")) {
                coverUrl = coverUrl.substring(1);
            }
            String fullCoverUrl = com.example.musicplayer_client.utils.Constants.BASE_URL + ":8080/" + coverUrl;
            Glide.with(holder.itemView.getContext())
                .load(fullCoverUrl)
                .placeholder(R.drawable.ic_music_placeholder)
                .error(R.drawable.ic_music_placeholder)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .into(holder.ivCover);
            Log.d("Adapter", "onBindViewHolder 在线: " + song.getTitle() + ", fullCoverUrl=" + fullCoverUrl);
        } else if (song.getCoverBytes() != null) {
            Glide.with(holder.itemView.getContext())
                .load(song.getCoverBytes())
                .placeholder(R.drawable.ic_music_placeholder)
                .error(R.drawable.ic_music_placeholder)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .into(holder.ivCover);
            Log.d("Adapter", "onBindViewHolder 本地有封面: " + song.getTitle());
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_music_placeholder);
            Log.d("Adapter", "onBindViewHolder 本地无封面: " + song.getTitle());
        }
        holder.itemView.setBackgroundResource(position == parentFragment.getPlayingIndex() ?
                R.color.apple_music_selected : android.R.color.transparent);
        holder.itemView.setOnClickListener(v -> {
            Log.d("DailyRecommendAdapter", "点击item: position=" + position + ", title=" + song.getTitle() + ", url=" + song.getUrl() + ", isOnline=" + song.isOnline());
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(song, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return parentFragment.getCurrentDisplayList().size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Song song, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvArtist;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
        }
    }
} 