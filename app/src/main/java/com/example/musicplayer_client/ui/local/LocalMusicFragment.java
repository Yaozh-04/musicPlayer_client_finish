package com.example.musicplayer_client.ui.local;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.musicplayer_client.R;
import com.example.musicplayer_client.utils.LocalMusicLoader;
import com.example.musicplayer_client.model.LocalSong;
import java.util.ArrayList;
import java.util.List;

public class LocalMusicFragment extends Fragment {
    private List<LocalSong> allLocalSongs = new ArrayList<>();
    private List<LocalSong> filteredSongs = new ArrayList<>();
    private LocalMusicAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rv_local_music);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        allLocalSongs = LocalMusicLoader.loadLocalMusic(getContext());
        filteredSongs = new ArrayList<>(allLocalSongs);
        adapter = new LocalMusicAdapter(filteredSongs);
        recyclerView.setAdapter(adapter);

        // 搜索框监听
        EditText etSearch = requireActivity().findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLocalSongs(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        adapter.setOnItemClickListener((song, position) -> {
            com.example.musicplayer_client.player.MusicPlayerManager.getInstance().playLocal(filteredSongs, position);
        });
        return view;
    }

    private void filterLocalSongs(String keyword) {
        filteredSongs.clear();
        if (keyword.isEmpty()) {
            filteredSongs.addAll(allLocalSongs);
        } else {
            for (LocalSong song : allLocalSongs) {
                if (song.getTitle().toLowerCase().contains(keyword.toLowerCase()) || song.getArtist().toLowerCase().contains(keyword.toLowerCase())) {
                    filteredSongs.add(song);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
} 