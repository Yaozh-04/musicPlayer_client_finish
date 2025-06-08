package com.example.musicplayer_client.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer_client.R;
import com.example.musicplayer_client.data.model.LoginRequest;
import com.example.musicplayer_client.data.model.ApiResponse;
import com.example.musicplayer_client.data.network.ApiService;
import com.example.musicplayer_client.ui.register.RegisterActivity;
import com.example.musicplayer_client.ui.main.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.animation.ObjectAnimator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.musicplayer_client.utils.Constants;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView btnToRegister;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnToRegister = findViewById(R.id.btnToRegister);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayout);

        // 输入框高亮效果 (动画)
        float density = getResources().getDisplayMetrics().density;
        int defaultStrokeWidth = (int) (1 * density); // 1dp
        int focusedStrokeWidth = (int) (2 * density); // 2dp
        int defaultStrokeColor = Color.parseColor("#E5E5EA"); // 默认灰
        int focusedStrokeColor = Color.parseColor("#FA2C56"); // Apple Music红

        View.OnFocusChangeListener animatedHighlightListener = (v, hasFocus) -> {
            Drawable bg = v.getBackground();
            if (bg instanceof GradientDrawable) {
                GradientDrawable gd = (GradientDrawable) bg;
                if (hasFocus) {
                    // 获得焦点时动画：宽度从1到2，颜色从灰到红
                    ValueAnimator widthAnimator = ValueAnimator.ofInt(defaultStrokeWidth, focusedStrokeWidth);
                    widthAnimator.addUpdateListener(animation -> {
                        gd.setStroke((int) animation.getAnimatedValue(), gd.getColor());
                    });
                    widthAnimator.setDuration(250);
                    widthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    widthAnimator.start();

                    ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), defaultStrokeColor, focusedStrokeColor);
                    colorAnimator.addUpdateListener(animation -> {
                        gd.setColor((int) animation.getAnimatedValue());
                        gd.setStroke(focusedStrokeWidth, (int) animation.getAnimatedValue()); // 同时更新颜色到 stroke
                    });
                    colorAnimator.setDuration(250);
                    colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    colorAnimator.start();

                } else {
                    // 失去焦点时动画：宽度从2到1，颜色从红到灰
                     ValueAnimator widthAnimator = ValueAnimator.ofInt(focusedStrokeWidth, defaultStrokeWidth);
                    widthAnimator.addUpdateListener(animation -> {
                        gd.setStroke((int) animation.getAnimatedValue(), gd.getColor());
                    });
                    widthAnimator.setDuration(250);
                    widthAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    widthAnimator.start();

                    ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), focusedStrokeColor, defaultStrokeColor);
                    colorAnimator.addUpdateListener(animation -> {
                        gd.setColor((int) animation.getAnimatedValue());
                        gd.setStroke(defaultStrokeWidth, (int) animation.getAnimatedValue()); // 同时更新颜色到 stroke
                    });
                    colorAnimator.setDuration(250);
                    colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    colorAnimator.start();
                }
            }
        };
        etUsername.setOnFocusChangeListener(animatedHighlightListener);
        etPassword.setOnFocusChangeListener(animatedHighlightListener);

        // 按钮点击缩放动画
        btnLogin.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    break;
            }
            return false;
        });

        // 禁止密码输入框明文预览
        etPassword.setTransformationMethod(new android.text.method.PasswordTransformationMethod());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL + ":8080/") // Android 模拟器访问本机用 10.0.2.2
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        btnToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        LoginRequest request = new LoginRequest(username, password);
        apiService.login(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "未知错误";
                    if (response.body() != null && response.body().message != null) {
                        errorMsg = response.body().message;
                    } else if (response.errorBody() != null) {
                        try {
                            String errorJson = response.errorBody().string();
                            // 简单提取后端返回的message字段
                            int msgIndex = errorJson.indexOf("\"message\":");
                            if (msgIndex != -1) {
                                int start = errorJson.indexOf('"', msgIndex + 10) + 1;
                                int end = errorJson.indexOf('"', start);
                                if (start > 0 && end > start) {
                                    errorMsg = errorJson.substring(start, end);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(LoginActivity.this, "登录失败：" + errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("Login", "网络错误", t);
                Toast.makeText(LoginActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 