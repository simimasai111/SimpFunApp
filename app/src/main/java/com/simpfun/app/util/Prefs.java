package com.simpfun.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 轻量偏好存储：保存登录 token 与基础用户信息（等价于网页端的 localStorage）
 */
public class Prefs {
    private static final String NAME = "simpfun_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_UID = "uid";

    private final SharedPreferences sp;

    public Prefs(Context ctx) {
        sp = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sp.edit().putString(KEY_TOKEN, token == null ? "" : token).apply();
    }

    public String getToken() {
        return sp.getString(KEY_TOKEN, "");
    }

    public boolean hasToken() {
        return !getToken().isEmpty();
    }

    public void saveUser(String username, String uid) {
        sp.edit().putString(KEY_USERNAME, username == null ? "" : username)
                .putString(KEY_UID, uid == null ? "" : uid).apply();
    }

    public String getUsername() {
        return sp.getString(KEY_USERNAME, "");
    }

    public String getUid() {
        return sp.getString(KEY_UID, "");
    }

    public void clear() {
        sp.edit().clear().apply();
    }
}
