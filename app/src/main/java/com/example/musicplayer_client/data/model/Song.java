package com.example.musicplayer_client.data.model;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String data; // 文件路径
    private long albumId;
    private String coverUrl; // 封面图片URL

    public Song() {}

    public Song(long id, String title, String artist, String album, long duration, String data, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.data = data;
        this.albumId = albumId;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public long getAlbumId() { return albumId; }
    public void setAlbumId(long albumId) { this.albumId = albumId; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
} 