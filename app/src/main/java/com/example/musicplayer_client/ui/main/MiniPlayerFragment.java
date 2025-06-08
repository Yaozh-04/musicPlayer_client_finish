package com.example.musicplayer_client.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.engine.GlideException;
import android.graphics.drawable.Drawable;
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.model.Song;
import com.example.musicplayer_client.player.MusicPlayerManager;
import com.example.musicplayer_client.utils.Constants;

import android.graphics.Bitmap;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

public class MiniPlayerFragment extends Fragment {

    private ImageView ivCover;
    private ImageButton btnPlayPause, btnNext, btnPrev;
    private TextView tvTitle, tvArtist;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        ivCover = view.findViewById(R.id.mini_player_cover);
        btnPlayPause = view.findViewById(R.id.mini_player_play_pause);
        btnNext = view.findViewById(R.id.mini_player_next);
        btnPrev = view.findViewById(R.id.mini_player_prev);
        tvTitle = view.findViewById(R.id.mini_player_title);
        tvArtist = view.findViewById(R.id.mini_player_artist);

        MusicPlayerManager player = MusicPlayerManager.getInstance();
        player.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
            if (song != null) {
                tvTitle.setText(song.getTitle());
                tvArtist.setText(song.getArtist());
                Log.d("MiniPlayerFragment", "当前歌曲: " + song.getTitle() + ", isOnline=" + song.isOnline() + ", coverBytes=" + (song.getCoverBytes() != null ? song.getCoverBytes().length : "null") + ", coverUrl=" + song.getCoverUrl());

                if (song.isOnline()) {
                    // 只用coverUrl
                    String coverUrl = song.getCoverUrl();
                    if (coverUrl != null && coverUrl.startsWith("/")) {
                        coverUrl = coverUrl.substring(1);
                    }
                    String fullCoverUrl = Constants.BASE_URL + ":8080/" + coverUrl;
                    Log.d("MiniPlayerFragment", "加载在线音乐封面: " + fullCoverUrl);
                    Glide.with(requireContext())
                        .load(fullCoverUrl)
                        .placeholder(R.drawable.ic_music_placeholder)
                        .error(R.drawable.ic_music_placeholder)
                        .transform(new CenterCrop(), new RoundedCorners(12))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Log.e("MiniPlayerFragment", "Glide加载失败: " + (e != null ? e.getMessage() : "null") + ", url=" + model);
                                if (e != null) e.logRootCauses("MiniPlayerFragment");
                                return false;
                            }
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                                Log.d("MiniPlayerFragment", "Glide加载成功: " + model);
                                return false;
                            }
                        })
                        .into(ivCover);
                } else if (song.getCoverBytes() != null && song.getCoverBytes().length > 0) {
                    // 只用coverBytes
                    Log.d("MiniPlayerFragment", "使用本地coverBytes显示封面，长度=" + song.getCoverBytes().length);
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(song.getCoverBytes())
                        .placeholder(R.drawable.ic_music_placeholder)
                        .error(R.drawable.ic_music_placeholder)
                        .transform(new CenterCrop(), new RoundedCorners(12))
                        .into(ivCover);
                    Log.d("MiniPlayerFragment", "本地封面Bitmap Glide加载");
                } else {
                    ivCover.setImageResource(R.drawable.ic_music_placeholder);
                    Log.e("MiniPlayerFragment", "本地封面Bitmap decode失败");
                }
            } else {
                ivCover.setImageResource(R.drawable.ic_music_placeholder);
                tvTitle.setText("");
                tvArtist.setText("");
                Log.d("MiniPlayerFragment", "song为null，重置UI");
            }
        });
        player.getIsPlaying().observe(getViewLifecycleOwner(), isPlaying -> {
            btnPlayPause.setImageResource(isPlaying != null && isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        });

        btnPlayPause.setOnClickListener(v -> {
            Boolean isPlaying = player.getIsPlaying().getValue();
            if (isPlaying != null && isPlaying) {
                player.pause();
            } else {
                player.play();
            }
        });
        btnNext.setOnClickListener(v -> player.playNext());
        btnPrev.setOnClickListener(v -> player.playPrev());

        return view;
    }
} 