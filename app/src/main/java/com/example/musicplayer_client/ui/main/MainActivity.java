package com.example.musicplayer_client.ui.main;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.musicplayer_client.R;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentContainerView;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private FragmentContainerView fragmentContainer;
    private FrameLayout miniPlayerContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = findViewById(R.id.tab_layout);
        fragmentContainer = findViewById(R.id.fragment_container);
        miniPlayerContainer = findViewById(R.id.mini_player_container);

        // 初始化Tab
        tabLayout.addTab(tabLayout.newTab().setText("在线音乐"));
        tabLayout.addTab(tabLayout.newTab().setText("本地音乐"));

        // 默认显示主页Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainFragment())
                .commit();
        }

        // Tab切换监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment fragment;
                if (tab.getPosition() == 0) {
                    fragment = new OnlineMusicFragment();
                } else {
                    fragment = new LocalMusicFragment();
                }
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // 加载全局迷你播放器Fragment
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.mini_player_container, new MiniPlayerFragment())
            .commit();
    }
} 