package com.example.musicplayer_client.util;

import android.content.Context;
import android.media.MediaPlayer;
import java.io.IOException;

public class MusicPlayerManager {
    private static MediaPlayer mediaPlayer;
    private static String currentPath;

    public static void play(Context context, String path) {
        stop();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
            currentPath = path;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public static void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            currentPath = null;
        }
    }

    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public static String getCurrentPath() {
        return currentPath;
    }
} 