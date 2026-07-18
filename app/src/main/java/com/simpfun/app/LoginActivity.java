package com.simpfun.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.simpfun.app.api.Api;
import com.simpfun.app.util.Prefs;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etUser, etPass;
    private Button btnLogin;
    private ProgressBar pb;
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_login);
        prefs = new Prefs(this);
        Api.get().initFromPrefs(prefs);

        etUser = findViewById(R.id.et_user);
        etPass = findViewById(R.id.et_pass);
        btnLogin = findViewById(R.id.btn_login);
        pb = findViewById(R.id.pb_login);

        etUser.setText(prefs.getUsername());

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString();
        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show();
            return;
        }
        btnLogin.setEnabled(false);
        pb.setVisibility(View.VISIBLE);
        Api.get().login(user, pass, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                String token = resp.optString("token");
                if (token.isEmpty()) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "登录成功但未返回 token", Toast.LENGTH_LONG).show();
                        reset();
                    });
                    return;
                }
                prefs.saveToken(token);
                Api.get().setToken(token);
                prefs.saveUser(user, "");
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void fail(String msg) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "登录失败：" + msg, Toast.LENGTH_LONG).show();
                    reset();
                });
            }
        });
    }

    private void reset() {
        runOnUiThread(() -> {
            btnLogin.setEnabled(true);
            pb.setVisibility(View.GONE);
        });
    }
}
