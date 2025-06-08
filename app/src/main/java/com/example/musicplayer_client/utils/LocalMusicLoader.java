package com.example.musicplayer_client.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.media.MediaMetadataRetriever;
import com.example.musicplayer_client.model.LocalSong;
import java.util.ArrayList;
import java.util.List;

public class LocalMusicLoader {
    public static List<LocalSong> loadLocalMusic(Context context) {
        List<LocalSong> songList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            MediaStore.Audio.Media.IS_MUSIC + "!= 0",
            null,
            MediaStore.Audio.Media.DEFAULT_SORT_ORDER
        );
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                // 提取封面
                byte[] coverBytes = null;
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(path);
                    coverBytes = mmr.getEmbeddedPicture();
                    mmr.release();
                } catch (Exception e) {
                    // 忽略异常
                }

                LocalSong song = new LocalSong();
                song.setTitle(title);
                song.setArtist(artist);
                song.setAlbum(album);
                song.setDuration(duration);
                song.setFilePath(path);
                song.setCoverBytes(coverBytes);
                songList.add(song);
            }
            cursor.close();
        }
        return songList;
    }
} 