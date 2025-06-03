package com.example.musicplayer_client.ui.main.model;

public class PlaylistItem {
    public int coverResId;
    public String name;
    public PlaylistItem(int coverResId, String name) {
        this.coverResId = coverResId;
        this.name = name;
    }
} 