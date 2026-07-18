package com.simpfun.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.simpfun.app.api.ApiClient;
import com.simpfun.app.util.Prefs;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录页：首次进入无 token 时强制显示；已登录则直接跳主页。
 * 登录接口：POST /api/auth/login  body: username, passwd
 */
public class LoginActivity extends AppCompatActivity {
    private EditText etUser, etPass;
    private Button btnLogin;
    private ProgressBar pb;
    private TextView tvError;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);

        if (prefs.hasToken()) {
            ApiClient.setToken(prefs.getToken());
            startMain();
            return;
        }

        setContentView(R.layout.activity_login);
        etUser = findViewById(R.id.et_username);
        etPass = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        pb = findViewById(R.id.pb_login);
        tvError = findViewById(R.id.tv_login_error);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString();
        if (user.isEmpty() || pass.isEmpty()) {
            tvError.setText("请输入用户名和密码");
            return;
        }
        tvError.setText("");
        btnLogin.setEnabled(false);
        pb.setVisibility(View.VISIBLE);

        Map<String, String> p = new HashMap<>();
        p.put("username", user);
        p.put("passwd", pass);

        ApiClient.post("/api/auth/login", p, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                // ⭐ 真实响应: {code:200, msg:"登录成功", "token":"..."} — token 在根级别
                String token = resp.optString("token", "");
                if (token.isEmpty()) {
                    // 兼容: 尝试 data.token
                    JSONObject data = resp.optJSONObject("data");
                    token = data != null ? data.optString("token", "") : "";
                }
                if (token.isEmpty()) {
                    onError("登录成功但未获取到 token，原始响应: " + resp.toString());
                    return;
                }
                ApiClient.setToken(token);
                prefs.saveToken(token);
                String uname = data != null ? data.optString("username", "") : "";
                if (uname.isEmpty()) uname = user;
                String uid = data != null ? data.optString("uid", "") : "";
                prefs.saveUser(uname, uid);
                startMain();
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    tvError.setText(error);
                });
            }
        });
    }

    private void startMain() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
