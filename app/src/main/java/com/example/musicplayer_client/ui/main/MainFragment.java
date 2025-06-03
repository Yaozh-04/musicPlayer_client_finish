package com.example.musicplayer_client.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer_client.R;
import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {
    private CardView cardLocalMusic, cardOnlineMusic;
    private RecyclerView rvRecommend;
    private RecommendAdapter recommendAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        cardLocalMusic = view.findViewById(R.id.card_local_music);
        cardOnlineMusic = view.findViewById(R.id.card_online_music);
        rvRecommend = view.findViewById(R.id.rv_recommend);

        // 卡片点击事件
        cardLocalMusic.setOnClickListener(v -> {
            // TODO: 跳转到本地音乐二级歌单页面
            Toast.makeText(getContext(), "进入本地音乐", Toast.LENGTH_SHORT).show();
        });
        cardOnlineMusic.setOnClickListener(v -> {
            // TODO: 跳转到在线音乐二级歌单页面
            Toast.makeText(getContext(), "进入在线音乐", Toast.LENGTH_SHORT).show();
        });

        // 推荐列表
        rvRecommend.setLayoutManager(new LinearLayoutManager(getContext()));
        recommendAdapter = new RecommendAdapter(getMockSongs());
        rvRecommend.setAdapter(recommendAdapter);

        return view;
    }

    // TODO: 后续替换为网络请求或本地数据
    private List<SongItem> getMockSongs() {
        List<SongItem> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            list.add(new SongItem("推荐歌曲" + i, "歌手" + i, ""));
        }
        return list;
    }

    // 推荐歌曲item数据结构
    public static class SongItem {
        public String title;
        public String artist;
        public String coverUrl;
        public SongItem(String title, String artist, String coverUrl) {
            this.title = title;
            this.artist = artist;
            this.coverUrl = coverUrl;
        }
    }
} 