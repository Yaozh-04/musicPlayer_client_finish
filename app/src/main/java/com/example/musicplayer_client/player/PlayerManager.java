package com.example.musicplayer_client.player;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.musicplayer_client.model.Song;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerManager {
    private static PlayerManager instance;
    private MediaPlayer mediaPlayer;
    private List<Song> playQueue;
    private int currentIndex;
    private Context context;
    private Handler handler;
    private boolean isPrepared = false;

    // LiveData用于通知UI更新
    private MutableLiveData<Song> currentSongLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> isPlayingLiveData = new MutableLiveData<>();
    private MutableLiveData<Integer> progressLiveData = new MutableLiveData<>();

    private PlayerManager(Context context) {
        this.context = context.getApplicationContext();
        this.playQueue = new ArrayList<>();
        this.currentIndex = -1;
        this.handler = new Handler(Looper.getMainLooper());
        initMediaPlayer();
    }

    public static synchronized PlayerManager getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerManager(context);
        }
        return instance;
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mp -> {
            isPrepared = true;
            mp.start();
            isPlayingLiveData.postValue(true);
            startProgressUpdate();
        });

        mediaPlayer.setOnCompletionListener(mp -> {
            playNext();
        });

        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            isPrepared = false;
            isPlayingLiveData.postValue(false);
            return false;
        });
    }

    public void play(List<Song> queue, int index) {
        if (queue == null || queue.isEmpty() || index < 0 || index >= queue.size()) {
            return;
        }

        this.playQueue = queue;
        this.currentIndex = index;
        Song song = playQueue.get(index);
        
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getUrl());
            mediaPlayer.prepareAsync();
            currentSongLiveData.postValue(song);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playNext() {
        if (playQueue.isEmpty()) return;
        int nextIndex = (currentIndex + 1) % playQueue.size();
        play(playQueue, nextIndex);
    }

    public void playPrev() {
        if (playQueue.isEmpty()) return;
        int prevIndex = (currentIndex - 1 + playQueue.size()) % playQueue.size();
        play(playQueue, prevIndex);
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlayingLiveData.postValue(false);
            stopProgressUpdate();
        }
    }

    public void resume() {
        if (mediaPlayer != null && isPrepared && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlayingLiveData.postValue(true);
            startProgressUpdate();
        }
    }

    private void startProgressUpdate() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int progress = (int) ((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration() * 100);
                    progressLiveData.postValue(progress);
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void stopProgressUpdate() {
        handler.removeCallbacksAndMessages(null);
    }

    // Getters for LiveData
    public LiveData<Song> getCurrentSongLiveData() {
        return currentSongLiveData;
    }

    public LiveData<Boolean> getIsPlayingLiveData() {
        return isPlayingLiveData;
    }

    public LiveData<Integer> getProgressLiveData() {
        return progressLiveData;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopProgressUpdate();
    }
} 