package com.example.musicplayer_client.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.widget.RadioGroup;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer_client.R;
import com.example.musicplayer_client.data.model.RegisterRequest;
import com.example.musicplayer_client.data.model.ApiResponse;
import com.example.musicplayer_client.data.network.ApiService;
import com.example.musicplayer_client.ui.login.LoginActivity;
import com.example.musicplayer_client.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import java.util.Calendar;
import java.util.Locale;
import android.app.DatePickerDialog;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etConfirmPassword, etBirthday, etNickname, etPhone;
    private Button btnRegister;
    private TextView btnToLogin;
    private ApiService apiService;
    private CheckBox cbAgreement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnToLogin = findViewById(R.id.btnToLogin);
        etBirthday = findViewById(R.id.etBirthday);
        etNickname = findViewById(R.id.etNickname);
        etPhone = findViewById(R.id.etPhone);
        cbAgreement = findViewById(R.id.cbAgreement);

        // 输入框高亮效果
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
        etConfirmPassword.setOnFocusChangeListener(animatedHighlightListener);

        // 按钮点击缩放动画
        btnRegister.setOnTouchListener((v, event) -> {
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL + "/") // Android 模拟器访问本机用 10.0.2.2
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        btnToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });

        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(
                    RegisterActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                        etBirthday.setText(date);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                );
                dialog.show();
            }
        });
    }

    private String getSelectedGender() {
        RadioGroup rgGender = findViewById(R.id.rgGender);
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMale) return "男";
        if (checkedId == R.id.rbFemale) return "女";
        return "保密";
    }

    private void register() {
        if (!cbAgreement.isChecked()) {
            Toast.makeText(this, "请先同意服务协议和隐私政策", Toast.LENGTH_SHORT).show();
            return;
        }
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String fullname = etNickname.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = getSelectedGender();
        String birthday = etBirthday.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)
            || TextUtils.isEmpty(fullname) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(birthday)) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        RegisterRequest request = new RegisterRequest(username, password, fullname, phone, gender, birthday);
        apiService.register(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败：" + (response.body() != null ? response.body().message : "未知错误"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 