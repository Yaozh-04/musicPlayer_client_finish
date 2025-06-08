package com.example.musicplayer_client.player;

import android.media.MediaPlayer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.musicplayer_client.model.Song;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import com.example.musicplayer_client.model.LocalSong;
import com.example.musicplayer_client.utils.Constants;

public class MusicPlayerManager {
    private static MusicPlayerManager instance;
    private MediaPlayer mediaPlayer;
    private List<Song> playList = new ArrayList<>();
    private int currentIndex = -1;
    private static final String BASE_URL = Constants.BASE_URL + ":8080";

    private final MutableLiveData<Song> currentSong = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);

    // 本地音乐播放
    private List<LocalSong> localPlayList = new ArrayList<>();
    private int localCurrentIndex = -1;
    private boolean isPlayingLocal = false;

    private MusicPlayerManager() {
        initMediaPlayer();
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> playNext());
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Log.e("MusicPlayerManager", "MediaPlayer错误: what=" + what + ", extra=" + extra);
            return false;
        });
    }

    public static MusicPlayerManager getInstance() {
        if (instance == null) instance = new MusicPlayerManager();
        return instance;
    }

    public void setPlayList(List<Song> list, int index) {
        Log.d("MusicPlayerManager", "setPlayList: size=" + (list != null ? list.size() : 0) + ", index=" + index);
        playList = list;
        currentIndex = index;
        if (playList != null && !playList.isEmpty() && index >= 0 && index < playList.size()) {
            Song song = playList.get(index);
            Log.d("MusicPlayerManager", "setPlayList: 当前歌曲 isOnline=" + song.isOnline() + ", url=" + song.getUrl());
            if (song.isOnline()) {
                play(song);
                isPlayingLocal = false;
            } else {
                // 构造LocalSong
                com.example.musicplayer_client.model.LocalSong localSong = new com.example.musicplayer_client.model.LocalSong();
                localSong.setTitle(song.getTitle());
                localSong.setArtist(song.getArtist());
                localSong.setAlbum(song.getAlbum());
                localSong.setDuration(song.getDuration());
                localSong.setFilePath(song.getUrl());
                localSong.setCoverBytes(song.getCoverBytes());
                List<com.example.musicplayer_client.model.LocalSong> localList = new ArrayList<>();
                for (Song s : playList) {
                    if (!s.isOnline()) {
                        com.example.musicplayer_client.model.LocalSong l = new com.example.musicplayer_client.model.LocalSong();
                        l.setTitle(s.getTitle());
                        l.setArtist(s.getArtist());
                        l.setAlbum(s.getAlbum());
                        l.setDuration(s.getDuration());
                        l.setFilePath(s.getUrl());
                        l.setCoverBytes(s.getCoverBytes());
                        localList.add(l);
                    }
                }
                int localIndex = localList.indexOf(localSong);
                Log.d("MusicPlayerManager", "setPlayList: 本地音乐 localIndex=" + localIndex + ", filePath=" + localSong.getFilePath());
                playLocal(localList, localIndex >= 0 ? localIndex : 0);
                isPlayingLocal = true;
            }
        } else {
            Log.e("MusicPlayerManager", "setPlayList参数异常: playList为空或index越界");
        }
    }

    public void play(Song song) {
        try {
            if (song == null || song.getUrl() == null) {
                Log.e("MusicPlayerManager", "play: song或fileUrl为空");
                return;
            }
            if (!song.isOnline()) {
                Log.d("MusicPlayerManager", "play: 本地音乐, url=" + song.getUrl());
                com.example.musicplayer_client.model.LocalSong localSong = new com.example.musicplayer_client.model.LocalSong();
                localSong.setTitle(song.getTitle());
                localSong.setArtist(song.getArtist());
                localSong.setAlbum(song.getAlbum());
                localSong.setDuration(song.getDuration());
                localSong.setFilePath(song.getUrl());
                localSong.setCoverBytes(song.getCoverBytes());
                List<com.example.musicplayer_client.model.LocalSong> localList = new ArrayList<>();
                localList.add(localSong);
                playLocal(localList, 0);
                isPlayingLocal = true;
                return;
            }
            // 确保在线音乐不带coverBytes
            song.setCoverBytes(null);
            song.setOnline(true);

            String fileUrl = song.getUrl();
            if (fileUrl.startsWith("/")) {
                fileUrl = fileUrl.substring(1);
            }
            String fullUrl = BASE_URL + "/" + fileUrl;
            Log.d("MusicPlayerManager", "准备播放: " + song.getTitle() + ", URL=" + fullUrl);
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fullUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    mp.start();
                    currentSong.postValue(song);
                    isPlaying.postValue(true);
                    Log.d("MusicPlayerManager", "播放开始: " + song.getTitle());
                } catch (Exception e) {
                    Log.e("MusicPlayerManager", "播放开始异常: " + e.getMessage());
                }
            });
            isPlayingLocal = false;
        } catch (Exception e) {
            Log.e("MusicPlayerManager", "play异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void play() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.start();
                isPlaying.postValue(true);
                Log.d("MusicPlayerManager", "恢复播放");
            } catch (Exception e) {
                Log.e("MusicPlayerManager", "恢复播放异常: " + e.getMessage());
            }
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            try {
                mediaPlayer.pause();
                isPlaying.postValue(false);
                Log.d("MusicPlayerManager", "暂停播放");
            } catch (Exception e) {
                Log.e("MusicPlayerManager", "暂停播放异常: " + e.getMessage());
            }
        }
    }

    public void playNext() {
        Log.d("MusicPlayerManager", "playNext: 当前播放类型=" + (isPlayingLocal ? "本地" : "在线"));
        if (isPlayingLocal) {
            if (localPlayList == null || localPlayList.isEmpty()) {
                Log.e("MusicPlayerManager", "playNext: 本地播放队列为空");
                return;
            }
            localCurrentIndex = (localCurrentIndex + 1) % localPlayList.size();
            Log.d("MusicPlayerManager", "playNext: 切换到本地index=" + localCurrentIndex);
            playLocalSong(localPlayList.get(localCurrentIndex));
        } else {
            if (playList == null || playList.isEmpty()) {
                Log.e("MusicPlayerManager", "playNext: 播放队列为空");
                return;
            }
            currentIndex = (currentIndex + 1) % playList.size();
            Log.d("MusicPlayerManager", "playNext: 切换到在线index=" + currentIndex);
            play(playList.get(currentIndex));
        }
    }

    public void playPrev() {
        Log.d("MusicPlayerManager", "playPrev: 当前播放类型=" + (isPlayingLocal ? "本地" : "在线"));
        if (isPlayingLocal) {
            if (localPlayList == null || localPlayList.isEmpty()) {
                Log.e("MusicPlayerManager", "playPrev: 本地播放队列为空");
                return;
            }
            localCurrentIndex = (localCurrentIndex - 1 + localPlayList.size()) % localPlayList.size();
            Log.d("MusicPlayerManager", "playPrev: 切换到本地index=" + localCurrentIndex);
            playLocalSong(localPlayList.get(localCurrentIndex));
        } else {
            if (playList == null || playList.isEmpty()) {
                Log.e("MusicPlayerManager", "playPrev: 播放队列为空");
                return;
            }
            currentIndex = (currentIndex - 1 + playList.size()) % playList.size();
            Log.d("MusicPlayerManager", "playPrev: 切换到在线index=" + currentIndex);
            play(playList.get(currentIndex));
        }
    }

    public LiveData<Song> getCurrentSong() { return currentSong; }
    public LiveData<Boolean> getIsPlaying() { return isPlaying; }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public List<Song> getPlayList() {
        return playList;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void playLocal(List<LocalSong> list, int index) {
        isPlayingLocal = true;
        localPlayList = list;
        localCurrentIndex = index;
        Log.d("MusicPlayerManager", "playLocal: 播放本地音乐, 列表大小=" + (list != null ? list.size() : 0) + ", index=" + index);
        if (localPlayList != null && !localPlayList.isEmpty() && index >= 0 && index < localPlayList.size()) {
            playLocalSong(localPlayList.get(index));
        } else {
            Log.e("MusicPlayerManager", "playLocal参数异常: playList为空或index越界");
        }
    }

    private void playLocalSong(LocalSong song) {
        try {
            if (song == null || song.getFilePath() == null) {
                Log.e("MusicPlayerManager", "playLocalSong: song或filePath为空");
                return;
            }
            Log.d("MusicPlayerManager", "准备播放本地: " + song.getTitle() + ", path=" + song.getFilePath() + ", coverBytes=" + (song.getCoverBytes() != null ? song.getCoverBytes().length : "null"));
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getFilePath());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                try {
                    mp.start();
                    // 构造Song对象用于UI显示
                    Song uiSong = new Song();
                    uiSong.setTitle(song.getTitle());
                    uiSong.setArtist(song.getArtist());
                    uiSong.setAlbum(song.getAlbum());
                    uiSong.setUrl(song.getFilePath()); // 标记本地
                    uiSong.setCoverBytes(song.getCoverBytes());
                    uiSong.setOnline(false);
                    Log.d("MusicPlayerManager", "currentSong.postValue: 标题=" + uiSong.getTitle() + ", url=" + uiSong.getUrl() + ", coverBytes=" + (uiSong.getCoverBytes() != null ? uiSong.getCoverBytes().length : "null"));
                    currentSong.postValue(uiSong);
                    isPlaying.postValue(true);
                    Log.d("MusicPlayerManager", "本地播放开始: " + song.getTitle());
                } catch (Exception e) {
                    Log.e("MusicPlayerManager", "本地播放开始异常: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e("MusicPlayerManager", "playLocalSong异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 