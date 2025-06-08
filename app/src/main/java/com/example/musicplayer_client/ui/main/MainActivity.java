package com.example.musicplayer_client.ui.main;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.musicplayer_client.R;
import android.util.Log;
import com.example.musicplayer_client.ui.online.TestFragment;
import com.example.musicplayer_client.ui.online.OnlineMusicFragment;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class MainActivity extends AppCompatActivity {
    private View layoutSearch;
    private View layoutCardButtons;
    private EditText etSearch;
    private static final String TAG_HOME = "home_fragment";
    private static final String TAG_ONLINE = "online_music_fragment";
    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_STORAGE = 100;

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE_STORAGE);
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE);
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions(); // 加上这一句
        layoutSearch = findViewById(R.id.layout_search);
        layoutCardButtons = findViewById(R.id.layout_card_buttons);
        etSearch = findViewById(R.id.et_search);

        Log.d(TAG, "onCreate: 初始化MainActivity");
        // 加载迷你播放器Fragment和主页内容Fragment
        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            Log.d(TAG, "onCreate: 加载MiniPlayerFragment和HomeFragment");
            transaction.replace(R.id.mini_player_container, new MiniPlayerFragment());
            transaction.replace(R.id.fragment_container, new HomeFragment(), TAG_HOME);
            transaction.commit();
            Log.d(TAG, "onCreate: FragmentTransaction已提交");
        }

        // 在线音乐按钮点击事件
        View cardOnlineMusic = findViewById(R.id.card_online_music);
        if (cardOnlineMusic != null) {
            cardOnlineMusic.setOnClickListener(v -> {
                Log.d(TAG, "点击在线音乐按钮");
                switchToOnlineMusic();
            });
        } else {
            Log.e(TAG, "card_online_music未找到，按钮事件未绑定");
        }
        // 本地音乐按钮点击事件
        View cardLocalMusic = findViewById(R.id.card_local_music);
        if (cardLocalMusic != null) {
            cardLocalMusic.setOnClickListener(v -> {
                Log.d(TAG, "点击本地音乐按钮");
                switchToLocalMusic();
            });
        } else {
            Log.e(TAG, "card_local_music未找到，按钮事件未绑定");
        }
        // 默认主页hint
        if (etSearch != null) {
            etSearch.setHint("搜索本地/在线音乐");
        }
    }

    private void switchToOnlineMusic() {
        try {
            Log.d(TAG, "switchToOnlineMusic: 开始切换到OnlineMusicFragment");
            // 隐藏主页卡片按钮区
            if (layoutCardButtons != null) {
                layoutCardButtons.setVisibility(View.GONE);
                Log.d(TAG, "switchToOnlineMusic: 已隐藏卡片按钮区");
            }
            // 设置搜索框hint为"搜索在线音乐"
            if (etSearch != null) {
                etSearch.setHint("搜索在线音乐");
                Log.d(TAG, "switchToOnlineMusic: 已更新搜索框提示");
            }
            // 切换Fragment
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Log.d(TAG, "switchToOnlineMusic: 创建FragmentTransaction");

//            TestFragment fragment = new TestFragment();
//            Log.d(TAG, "switchToOnlineMusic: 创建TestFragment实例");
            OnlineMusicFragment fragment = new OnlineMusicFragment();
            ft.replace(R.id.fragment_container, fragment, TAG_ONLINE)
              .addToBackStack(null);
            Log.d(TAG, "switchToOnlineMusic: FragmentTransaction准备提交");

            ft.commit();
            Log.d(TAG, "switchToOnlineMusic: FragmentTransaction已提交，当前BackStackEntryCount=" + fm.getBackStackEntryCount());
        } catch (Exception e) {
            Log.e(TAG, "switchToOnlineMusic: FragmentTransaction异常", e);
        }
    }

    private void switchToLocalMusic() {
        Log.d(TAG, "switchToLocalMusic: 开始尝试切换到LocalMusicFragment");

        boolean hasPermission;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;

            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        REQUEST_CODE_STORAGE);
                Log.d(TAG, "switchToLocalMusic: 请求READ_MEDIA_AUDIO权限");
                return;
            }
        } else {
            hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;

            if (!hasPermission) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_STORAGE);
                Log.d(TAG, "switchToLocalMusic: 请求READ_EXTERNAL_STORAGE权限");
                return;
            }
        }

        // 有权限，执行原本逻辑
        try {
            if (layoutCardButtons != null) layoutCardButtons.setVisibility(View.GONE);
            if (etSearch != null) etSearch.setHint("搜索本地音乐");

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            com.example.musicplayer_client.ui.local.LocalMusicFragment fragment =
                    new com.example.musicplayer_client.ui.local.LocalMusicFragment();
            ft.replace(R.id.fragment_container, fragment, "local_music_fragment")
                    .addToBackStack(null)
                    .commit();
            Log.d(TAG, "switchToLocalMusic: LocalMusicFragment已提交");
        } catch (Exception e) {
            Log.e(TAG, "switchToLocalMusic: FragmentTransaction异常", e);
        }
    }


    // 监听返回主页时显示卡片按钮区并恢复hint
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Log.d(TAG, "onBackPressed: 当前BackStackEntryCount=" + fm.getBackStackEntryCount());
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            Log.d(TAG, "onBackPressed: 执行popBackStack");
            // 返回主页时显示卡片按钮区
            if (layoutCardButtons != null) layoutCardButtons.setVisibility(View.VISIBLE);
            // 恢复搜索框hint为"搜索本地/在线音乐"
            if (etSearch != null) {
                etSearch.setHint("搜索本地/在线音乐");
            }
        } else {
            Log.d(TAG, "onBackPressed: 无BackStack，调用super");
            super.onBackPressed();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permission", "读取权限已授予");
                switchToLocalMusic(); // 关键：重新进入本地音乐逻辑
            } else {
                Toast.makeText(this, "未授予读取权限，无法加载本地音乐", Toast.LENGTH_LONG).show();
            }
        }
    }



}