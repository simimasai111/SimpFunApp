package com.simpfun.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/** 本地持久化：token / 用户名 / uid */
public class Prefs {
    private static final String NAME = "simpfun_prefs";
    private final SharedPreferences sp;

    public Prefs(Context c) {
        sp = c.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String t) {
        sp.edit().putString("token", t == null ? "" : t).apply();
    }

    public String getToken() {
        return sp.getString("token", "");
    }

    public void saveUser(String user, String uid) {
        sp.edit().putString("user", user == null ? "" : user)
                .putString("uid", uid == null ? "" : uid).apply();
    }

    public String getUsername() {
        return sp.getString("user", "");
    }

    public void clear() {
        sp.edit().clear().apply();
    }
}
