package com.example.musicplayer_client.ui.online;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer_client.model.Song;
import com.example.musicplayer_client.data.model.SongItem;
import com.example.musicplayer_client.data.model.ApiResponse;
import com.example.musicplayer_client.data.network.ApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.utils.Constants;

public class OnlineMusicFragment extends Fragment {
    private RecyclerView recyclerView;
    private OnlineMusicAdapter adapter;
    private List<Song> allOnlineSongs = new ArrayList<>();
    private List<Song> filteredOnlineSongs = new ArrayList<>();
    private ApiService apiService;
    private static final String TAG = "OnlineMusicFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.example.musicplayer_client.R.layout.fragment_online_music, container, false);
        recyclerView = view.findViewById(com.example.musicplayer_client.R.id.recycler_online_music);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OnlineMusicAdapter(filteredOnlineSongs);
        recyclerView.setAdapter(adapter);

        // 搜索框监听
        EditText etSearch = requireActivity().findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOnlineSongs(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // 集成播放功能：点击播放并高亮
        adapter.setOnItemClickListener((song, position) -> {
            // 确保所有Song的isOnline为true
            for (Song s : filteredOnlineSongs) {
                s.setOnline(true);
            }
            Log.d(TAG, "点击播放: " + song.getTitle() + ", position=" + position + ", url=" + song.getUrl());
            com.example.musicplayer_client.player.MusicPlayerManager.getInstance().setPlayList(filteredOnlineSongs, position);
            adapter.setPlayingIndex(position);
        });

        // 联动高亮：监听当前播放歌曲变化，自动高亮对应项
        com.example.musicplayer_client.player.MusicPlayerManager.getInstance()
            .getCurrentSong()
            .observe(getViewLifecycleOwner(), song -> {
                if (song == null) return;
                int index = -1;
                for (int i = 0; i < allOnlineSongs.size(); i++) {
                    // 用 fileUrl 判断是否同一首
                    if (allOnlineSongs.get(i).getUrl() != null && allOnlineSongs.get(i).getUrl().equals(song.getUrl())) {
                        index = i;
                        break;
                    }
                }
                adapter.setPlayingIndex(index);
            });

        // 初始化Retrofit和ApiService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( Constants.BASE_URL + ":8080/") // 请根据实际服务端地址修改
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // 先显示假数据，防止网络慢时页面空白
        if (allOnlineSongs.isEmpty()) {
            for (int i = 1; i <= 1; i++) {
                Song song = new Song();
                song.setTitle("测试歌曲 " + i);
                song.setArtist("测试歌手 " + i);
                song.setAlbum("测试专辑 " + i);
                song.setDuration(180000);
                song.setCoverUrl("");
                song.setUrl("");
                allOnlineSongs.add(song);
            }
            adapter.notifyDataSetChanged();
        }

        // 发起网络请求
        loadOnlineMusic();
        return view;
    }

    private void loadOnlineMusic() {
        Log.d(TAG, "开始请求在线音乐API");
        apiService.getSongList().enqueue(new Callback<ApiResponse<List<SongItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SongItem>>> call, Response<ApiResponse<List<SongItem>>> response) {
                Log.d(TAG, "API响应: code=" + response.code() + ", body=" + response.body());
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<SongItem> songItems = response.body().data;
                    allOnlineSongs.clear();
                    if (songItems != null) {
                        for (SongItem item : songItems) {
                            Song song = new Song();
                            song.setTitle(item.getName());
                            song.setArtist(item.getArtist());
                            song.setAlbum(item.getAlbum());
                            song.setDuration(item.getDuration());
                            song.setCoverUrl(item.getCoverUrl());
                            song.setUrl(item.getFileUrl());
                            allOnlineSongs.add(song);
                        }
                    }
                    // 修正：同步filteredOnlineSongs，刷新Adapter
                    filteredOnlineSongs.clear();
                    filteredOnlineSongs.addAll(allOnlineSongs);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "获取在线音乐失败: " + (response.body() != null ? response.body().message : "null"));
                    Toast.makeText(getContext(), "获取在线音乐失败", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<SongItem>>> call, Throwable t) {
                Log.e(TAG, "API请求失败: " + t.getMessage());
                Toast.makeText(getContext(), "网络请求失败: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOnlineSongs(String keyword) {
        filteredOnlineSongs.clear();
        if (keyword.isEmpty()) {
            filteredOnlineSongs.addAll(allOnlineSongs);
        } else {
            for (Song song : allOnlineSongs) {
                if (song.getTitle().toLowerCase().contains(keyword.toLowerCase()) || song.getArtist().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredOnlineSongs.add(song);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
} 