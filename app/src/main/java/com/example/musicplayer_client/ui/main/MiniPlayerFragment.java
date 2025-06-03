package com.example.musicplayer_client.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.musicplayer_client.R;

public class MiniPlayerFragment extends Fragment {

    private ImageView cover;
    private TextView title, artist;
    private ImageButton btnPrev, btnPlayPause, btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);
        cover = view.findViewById(R.id.mini_player_cover);
        title = view.findViewById(R.id.mini_player_title);
        artist = view.findViewById(R.id.mini_player_artist);
        btnPrev = view.findViewById(R.id.mini_player_prev);
        btnPlayPause = view.findViewById(R.id.mini_player_play_pause);
        btnNext = view.findViewById(R.id.mini_player_next);

        // TODO: 后续与ViewModel和Service联动，当前先做UI演示
        btnPlayPause.setOnClickListener(v -> {
            // 切换播放/暂停图标
            if (btnPlayPause.getTag() == null || btnPlayPause.getTag().equals("play")) {
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                btnPlayPause.setTag("pause");
            } else {
                btnPlayPause.setImageResource(R.drawable.ic_play_arrow);
                btnPlayPause.setTag("play");
            }
        });

        // 其他按钮可先留空
        btnPrev.setOnClickListener(v -> {});
        btnNext.setOnClickListener(v -> {});

        return view;
    }
} 