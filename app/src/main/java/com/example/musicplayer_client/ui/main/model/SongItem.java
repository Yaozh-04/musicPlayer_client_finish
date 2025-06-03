package com.example.musicplayer_client.ui.main.model;

public class SongItem {
    public int coverResId;
    public String title;
    public String artist;
    public SongItem(int coverResId, String title, String artist) {
        this.coverResId = coverResId;
        this.title = title;
        this.artist = artist;
    }
} 