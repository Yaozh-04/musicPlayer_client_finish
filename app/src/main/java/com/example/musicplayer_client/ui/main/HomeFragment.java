package com.example.musicplayer_client.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.model.Song;
import com.example.musicplayer_client.data.model.SongItem;
import com.example.musicplayer_client.data.model.ApiResponse;
import com.example.musicplayer_client.data.network.ApiService;
import com.example.musicplayer_client.player.MusicPlayerManager;
import com.example.musicplayer_client.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {
    private static final String PREF_NAME = "daily_recommend";
    private static final String KEY_LAST_REFRESH_DATE = "last_refresh_date";
    private static final String KEY_DAILY_SONGS = "daily_songs";
    
    private DailyRecommendAdapter dailyRecommendAdapter;
    private List<Song> dailyRecommendList = new ArrayList<>();
    private List<Song> allOnlineSongs = new ArrayList<>();
    private List<Song> allLocalSongs = new ArrayList<>();
    private List<Song> filteredSongs = new ArrayList<>();
    private boolean isSearching = false;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 初始化Retrofit和ApiService
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl( Constants.BASE_URL + ":8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        TextView tvTitle = view.findViewById(R.id.tv_daily_recommend);
        RecyclerView rvDailyRecommend = view.findViewById(R.id.rv_daily_recommend);
        rvDailyRecommend.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyRecommendAdapter = new DailyRecommendAdapter(this);
        rvDailyRecommend.setAdapter(dailyRecommendAdapter);
        dailyRecommendAdapter.setOnItemClickListener((song, position) -> {
            List<Song> displayList = getCurrentDisplayList();
            Log.d("HomeFragment", "点击歌曲: " + song.getTitle() + ", fileUrl=" + song.getUrl() + ", position=" + position);
            Log.d("HomeFragment", "当前播放队列(displayList)大小: " + displayList.size());
            for (int i = 0; i < displayList.size(); i++) {
                Log.d("HomeFragment", "displayList[" + i + "]: " + displayList.get(i).getTitle() + ", url=" + displayList.get(i).getUrl());
            }
            MusicPlayerManager.getInstance().setPlayList(displayList, position);
            Log.d("HomeFragment", "已调用MusicPlayerManager.setPlayList, position=" + position);
        });

        // 检查是否需要刷新每日推荐
        checkAndLoadDailyRecommend();
        // 加载所有在线音乐
        loadAllOnlineMusic();
        // 初始化本地音乐
        loadLocalMusic();

        // 监听当前播放项高亮
        MusicPlayerManager.getInstance().getCurrentSong().observe(getViewLifecycleOwner(), song -> {
            int index = -1;
            List<Song> displayList = getCurrentDisplayList();
            for (int i = 0; i < displayList.size(); i++) {
                if (displayList.get(i).getId().equals(song.getId())) {
                    index = i;
                    break;
                }
            }
            Log.d("HomeFragment", "当前播放项高亮 index=" + index + ", song=" + (song != null ? song.getTitle() : "null"));
            dailyRecommendAdapter.notifyDataSetChanged();
        });

        // 搜索框监听
        EditText etSearch = requireActivity().findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString();
                isSearching = !keyword.isEmpty();
                filterAllSongs(keyword);
                tvTitle.setText(isSearching ? "搜索结果" : "每日推荐");
                dailyRecommendAdapter.notifyDataSetChanged();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    // 检查并加载每日推荐
    private void checkAndLoadDailyRecommend() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastRefreshDate = prefs.getString(KEY_LAST_REFRESH_DATE, "");

        if (!today.equals(lastRefreshDate)) {
            // 今天还没有刷新过，从API加载新的推荐
            loadDailyRecommendFromApi();
        } else {
            // 今天已经刷新过，从本地加载保存的推荐
            String savedSongs = prefs.getString(KEY_DAILY_SONGS, "");
            if (!savedSongs.isEmpty()) {
                try {
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<Song>>(){}.getType();
                    List<Song> savedList = gson.fromJson(savedSongs, type);
                    dailyRecommendList.clear();
                    dailyRecommendList.addAll(savedList);
                    if (!isSearching) {
                        dailyRecommendAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    Log.e("HomeFragment", "加载保存的每日推荐失败", e);
                    loadDailyRecommendFromApi();
                }
            } else {
                loadDailyRecommendFromApi();
            }
        }
    }

    // 每日推荐API只生成30首
    private void loadDailyRecommendFromApi() {
        Log.d("HomeFragment", "开始加载每日推荐...");
        Log.d("HomeFragment", "API URL: " + Constants.BASE_URL + ":8080/");
        apiService.getSongList().enqueue(new Callback<ApiResponse<List<SongItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SongItem>>> call, Response<ApiResponse<List<SongItem>>> response) {
                Log.d("HomeFragment", "每日推荐API响应: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<SongItem> songItems = response.body().data;
                    dailyRecommendList.clear();
                    if (songItems != null) {
                        Collections.shuffle(songItems);
                        for (int i = 0; i < Math.min(30, songItems.size()); i++) {
                            SongItem item = songItems.get(i);
                            Song song = new Song();
                            song.setId(String.valueOf(item.getId()));
                            song.setTitle(item.getName());
                            song.setArtist(item.getArtist());
                            song.setAlbum(item.getAlbum());
                            song.setDuration(item.getDuration());
                            song.setCoverUrl(item.getCoverUrl());
                            song.setUrl(item.getFileUrl());
                            song.setOnline(true);
                            dailyRecommendList.add(song);
                        }
                    }
                    
                    // 保存到本地
                    SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    Gson gson = new Gson();
                    String savedSongs = gson.toJson(dailyRecommendList);
                    prefs.edit()
                        .putString(KEY_LAST_REFRESH_DATE, today)
                        .putString(KEY_DAILY_SONGS, savedSongs)
                        .apply();

                    if (!isSearching) {
                        dailyRecommendAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.e("HomeFragment", "每日推荐API失败: " + (response.body() != null ? response.body().message : "null"));
                    Log.e("HomeFragment", "错误响应: " + response.errorBody());
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<SongItem>>> call, Throwable t) {
                Log.e("HomeFragment", "每日推荐API请求失败: " + t.getMessage());
                Log.e("HomeFragment", "错误详情: ", t);
            }
        });
    }

    // Adapter数据源切换
    public List<Song> getCurrentDisplayList() {
        return isSearching ? filteredSongs : dailyRecommendList;
    }

    // 获取当前播放索引
    public int getPlayingIndex() {
        Song currentSong = MusicPlayerManager.getInstance().getCurrentSong().getValue();
        if (currentSong == null) return -1;
        
        List<Song> displayList = getCurrentDisplayList();
        for (int i = 0; i < displayList.size(); i++) {
            if (displayList.get(i).getId().equals(currentSong.getId())) {
                return i;
            }
        }
        return -1;
    }

    // 只在allOnlineSongs和allLocalSongs中全局搜索
    private void filterAllSongs(String keyword) {
        filteredSongs.clear();
        if (keyword.isEmpty()) return;
        List<Song> merged = new ArrayList<>();
        merged.addAll(allOnlineSongs);
        merged.addAll(allLocalSongs);
        for (Song song : merged) {
            if (song.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                song.getArtist().toLowerCase().contains(keyword.toLowerCase())) {
                filteredSongs.add(song);
            }
        }
    }

    // 加载本地音乐（假设有LocalMusicLoader工具类）
    private void loadLocalMusic() {
        allLocalSongs.clear();
        List<Song> localSongs = new ArrayList<>();
        // 这里假设LocalMusicLoader.loadLocalMusic返回List<LocalSong>，需转为Song
        List<com.example.musicplayer_client.model.LocalSong> localList = com.example.musicplayer_client.utils.LocalMusicLoader.loadLocalMusic(getContext());
        for (com.example.musicplayer_client.model.LocalSong local : localList) {
            Song song = new Song();
            song.setId(local.getFilePath());
            song.setTitle(local.getTitle());
            song.setArtist(local.getArtist());
            song.setAlbum(local.getAlbum());
            song.setDuration(local.getDuration());
            song.setUrl(local.getFilePath());
            song.setCoverBytes(local.getCoverBytes());
            song.setOnline(false);
            allLocalSongs.add(song);
        }
        // 本地加载后也刷新搜索结果
        filterAllSongs("");
    }

    // 加载所有在线音乐（全局搜索用）
    private void loadAllOnlineMusic() {
        apiService.getSongList().enqueue(new Callback<ApiResponse<List<SongItem>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SongItem>>> call, Response<ApiResponse<List<SongItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    allOnlineSongs.clear();
                    List<SongItem> songItems = response.body().data;
                    if (songItems != null) {
                        for (SongItem item : songItems) {
                            Song song = new Song();
                            song.setId(String.valueOf(item.getId()));
                            song.setTitle(item.getName());
                            song.setArtist(item.getArtist());
                            song.setAlbum(item.getAlbum());
                            song.setDuration(item.getDuration());
                            song.setCoverUrl(item.getCoverUrl());
                            song.setUrl(item.getFileUrl());
                            song.setOnline(true);
                            allOnlineSongs.add(song);
                        }
                    }
                    filterAllSongs(""); // 刷新全局搜索
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<SongItem>>> call, Throwable t) {}
        });
    }
} 