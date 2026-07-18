package com.simpfun.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.fragment.ServerFragment;
import com.simpfun.app.fragment.InviteFragment;
import com.simpfun.app.fragment.ProfileFragment;
import com.simpfun.app.util.Prefs;

/**
 * 主容器：底部三栏导航（服务器 / 邀请 / 我的）
 * 启动时校验 token，缺失则退回登录页。
 */
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView nav;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs prefs = new Prefs(this);
        if (!prefs.hasToken()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        ApiClient.setToken(prefs.getToken());
        setContentView(R.layout.activity_main);

        tvTitle = findViewById(R.id.tv_title);
        nav = findViewById(R.id.bottom_nav);

        switchFragment(new ServerFragment(), R.string.nav_server);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_server) {
                switchFragment(new ServerFragment(), R.string.nav_server);
                return true;
            } else if (id == R.id.nav_invite) {
                switchFragment(new InviteFragment(), R.string.nav_invite);
                return true;
            } else if (id == R.id.nav_profile) {
                switchFragment(new ProfileFragment(), R.string.nav_profile);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment f, int titleRes) {
        if (tvTitle != null) tvTitle.setText(titleRes);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f)
                .commit();
    }
}
