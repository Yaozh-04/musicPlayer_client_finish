package com.example.musicplayer_client.ui.local;

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
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.model.LocalSong;
import java.util.List;

public class LocalMusicAdapter extends RecyclerView.Adapter<LocalMusicAdapter.ViewHolder> {
    private List<LocalSong> songList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(LocalSong song, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public LocalMusicAdapter(List<LocalSong> songList) {
        this.songList = songList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_local_music, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalSong song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        if (song.getCoverBytes() != null) {
            Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(song.getCoverBytes())
                .placeholder(R.drawable.ic_music_placeholder)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .into(holder.ivCover);
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_music_placeholder);
        }
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(song, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvArtist;
        ViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.iv_cover);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvArtist = itemView.findViewById(R.id.tv_artist);
        }
    }
} 