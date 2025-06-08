package com.example.musicplayer_client.model;

public class LocalSong {
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String filePath;
    private byte[] coverBytes;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public byte[] getCoverBytes() { return coverBytes; }
    public void setCoverBytes(byte[] coverBytes) { this.coverBytes = coverBytes; }
} 