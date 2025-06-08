package com.example.musicplayer_client.model;

public class Song {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String coverUrl;
    private String url;  // 本地文件路径或在线音乐URL
    private boolean isOnline;  // 标识是否为在线音乐
    private long duration;  // 音乐时长（毫秒）
    private byte[] coverBytes; // 本地音乐封面二进制数据

    public Song(String id, String title, String artist, String album, String coverUrl, String url, boolean isOnline, long duration) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUrl = coverUrl;
        this.url = url;
        this.isOnline = isOnline;
        this.duration = duration;
    }

    public Song() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public byte[] getCoverBytes() { return coverBytes; }
    public void setCoverBytes(byte[] coverBytes) { this.coverBytes = coverBytes; }
} 