package com.example.musicplayer_client.ui.main;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.data.model.Song;
import com.example.musicplayer_client.ui.main.adapter.LocalMusicAdapter;
import com.example.musicplayer_client.util.MusicPlayerManager;
import java.util.ArrayList;
import java.util.List;

public class LocalMusicActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1001;
    private RecyclerView rvLocalMusic;
    private LocalMusicAdapter adapter;
    private List<Song> songList = new ArrayList<>();
    // 迷你播放器控件
    private ImageView miniCover;
    private TextView miniTitle, miniArtist;
    private ImageButton miniPlayPause;
    private Song currentSong;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        rvLocalMusic = findViewById(R.id.rv_local_music);
        rvLocalMusic.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LocalMusicAdapter(songList, song -> {
            playSong(song);
        });
        rvLocalMusic.setAdapter(adapter);
        // 迷你播放器
        miniCover = findViewById(R.id.mini_player_cover);
        miniTitle = findViewById(R.id.mini_player_title);
        miniArtist = findViewById(R.id.mini_player_artist);
        miniPlayPause = findViewById(R.id.mini_player_play_pause);
        miniPlayPause.setOnClickListener(v -> {
            if (MusicPlayerManager.isPlaying()) {
                MusicPlayerManager.pause();
                miniPlayPause.setImageResource(R.drawable.ic_play_arrow);
            } else {
                MusicPlayerManager.resume();
                miniPlayPause.setImageResource(R.drawable.ic_pause);
            }
        });
        checkPermissionAndLoad();
    }

    private void checkPermissionAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
            } else {
                loadLocalMusic();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                loadLocalMusic();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadLocalMusic();
        }
    }

    private void loadLocalMusic() {
        songList.clear();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Song song = new Song();
                song.setId(cursor.getLong(0));
                song.setTitle(cursor.getString(1));
                song.setArtist(cursor.getString(2));
                song.setAlbum(cursor.getString(3));
                song.setDuration(cursor.getLong(4));
                song.setData(cursor.getString(5));
                song.setAlbumId(cursor.getLong(6));
                songList.add(song);
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void playSong(Song song) {
        currentSong = song;
        MusicPlayerManager.play(this, song.getData());
        updateMiniPlayer();
    }

    private void updateMiniPlayer() {
        if (currentSong == null) return;
        miniTitle.setText(currentSong.getTitle());
        miniArtist.setText(currentSong.getArtist());
        Uri coverUri = getAlbumArtUri(currentSong.getAlbumId());
        Glide.with(this)
                .load(coverUri)
                .placeholder(R.drawable.ic_music_note)
                .into(miniCover);
        miniPlayPause.setImageResource(MusicPlayerManager.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
    }

    private Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPlayerManager.stop();
    }
} 