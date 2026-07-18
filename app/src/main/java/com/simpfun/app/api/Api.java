package com.simpfun.app.api;

import android.os.Handler;
import android.os.Looper;

import com.simpfun.app.util.Prefs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 简幻欢 API 客户端（单例）。
 * 所有端点严格对应前端 Client.825f18dc.js 反编译 + 实测（见 simpfun_real_apis.md）。
 * - 鉴权头：Authorization: <token>（非 Bearer）
 * - POST 表单：application/x-www-form-urlencoded
 * - 响应外壳：{"code":200,"msg":"...","data|info|list":...}
 */
public class Api {
    public static final String BASE = "https://api.simpfun.cn";
    private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded");

    private static Api INSTANCE;
    private final OkHttpClient client;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private String token = "";

    public interface CB {
        void ok(JSONObject resp);

        void fail(String msg);
    }

    private Api() {
        client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .build();
    }

    public static Api get() {
        if (INSTANCE == null) INSTANCE = new Api();
        return INSTANCE;
    }

    public void setToken(String t) {
        token = t == null ? "" : t;
    }

    public String getToken() {
        return token;
    }

    public void initFromPrefs(Prefs p) {
        token = p.getToken();
    }

    // ===================== 底层请求 =====================

    private void get(String path, Map<String, String> q, CB cb) {
        HttpUrl.Builder b = HttpUrl.parse(BASE + path).newBuilder();
        if (q != null) {
            for (Map.Entry<String, String> e : q.entrySet()) {
                if (e.getValue() != null) b.addQueryParameter(e.getKey(), e.getValue());
            }
        }
        Request.Builder rb = new Request.Builder().url(b.build())
                .addHeader("Accept", "application/json");
        if (!token.isEmpty()) rb.addHeader("Authorization", token);
        exec(client.newCall(rb.build()), cb);
    }

    private void post(String path, Map<String, String> form, CB cb) {
        FormBody.Builder fb = new FormBody.Builder();
        if (form != null) {
            for (Map.Entry<String, String> e : form.entrySet()) {
                fb.add(e.getKey(), e.getValue() == null ? "" : e.getValue());
            }
        }
        Request.Builder rb = new Request.Builder().url(BASE + path)
                .addHeader("Accept", "application/json")
                .post(fb.build());
        if (!token.isEmpty()) rb.addHeader("Authorization", token);
        exec(client.newCall(rb.build()), cb);
    }

    private void patch(String path, Map<String, String> form, CB cb) {
        FormBody.Builder fb = new FormBody.Builder();
        if (form != null) {
            for (Map.Entry<String, String> e : form.entrySet()) {
                fb.add(e.getKey(), e.getValue() == null ? "" : e.getValue());
            }
        }
        Request.Builder rb = new Request.Builder().url(BASE + path)
                .addHeader("Accept", "application/json")
                .patch(fb.build());
        if (!token.isEmpty()) rb.addHeader("Authorization", token);
        exec(client.newCall(rb.build()), cb);
    }

    private void delete(String path, Map<String, String> form, CB cb) {
        FormBody.Builder fb = new FormBody.Builder();
        if (form != null) {
            for (Map.Entry<String, String> e : form.entrySet()) {
                fb.add(e.getKey(), e.getValue() == null ? "" : e.getValue());
            }
        }
        Request.Builder rb = new Request.Builder().url(BASE + path)
                .addHeader("Accept", "application/json")
                .delete(fb.build());
        if (!token.isEmpty()) rb.addHeader("Authorization", token);
        exec(client.newCall(rb.build()), cb);
    }

    private void exec(Call call, CB cb) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ui.post(() -> cb.fail("网络错误：" + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                String body;
                try {
                    body = response.body() != null ? response.body().string() : "";
                } catch (IOException e) {
                    body = "";
                }
                final String fb = body;
                final int code = response.code();
                ui.post(() -> {
                    if (code < 200 || code >= 300) {
                        cb.fail("HTTP " + code);
                        return;
                    }
                    try {
                        JSONObject j = new JSONObject(fb);
                        int c = j.optInt("code", 200);
                        if (c == 200) {
                            cb.ok(j);
                        } else {
                            cb.fail(j.optString("msg", "code=" + c));
                        }
                    } catch (Exception e) {
                        cb.fail("响应解析失败：" + fb.substring(0, Math.min(120, fb.length())));
                    }
                });
            }
        });
    }

    private static Map<String, String> q(Object... kv) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            m.put(String.valueOf(kv[i]), String.valueOf(kv[i + 1]));
        }
        return m;
    }

    // ===================== 鉴权 / 账户 =====================

    public void login(String user, String pass, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("username", user);
        f.put("passwd", pass);
        post("/api/auth/login", f, cb);
    }

    public void info(CB cb) {
        get("/api/auth/info", null, cb);
    }

    public void invite(CB cb) {
        get("/api/invite", null, cb);
    }

    public void diamondHistory(CB cb) {
        get("/api/diamondhistory", null, cb);
    }

    public void announcement(CB cb) {
        get("/api/announcement", null, cb);
    }

    public void announcementRead(CB cb) {
        post("/api/announcement_read", new HashMap<>(), cb);
    }

    // ===================== 服务器列表 / 积分 =====================

    public void insList(CB cb) {
        get("/api/ins/list", null, cb);
    }

    public void pointHistory(CB cb) {
        get("/api/pointhistory", q("page", 1, "size", 20), cb);
    }

    // ===================== 创建实例向导 =====================

    public void gamesList(CB cb) {
        get("/api/games/list", null, cb);
    }

    public void kindList(int gameId, CB cb) {
        get("/api/games/kindlist", q("game_id", gameId), cb);
    }

    public void versionList(int kindId, CB cb) {
        get("/api/games/versionlist", q("kind_id", kindId), cb);
    }

    public void shopList(int versionId, CB cb) {
        get("/api/shop/list", q("version_id", versionId), cb);
    }

    public void createInstance(int itemId, int versionId, String name, boolean custom, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("item_id", String.valueOf(itemId));
        f.put("version_id", String.valueOf(versionId));
        if (name != null && !name.isEmpty()) f.put("name", name);
        if (custom) f.put("custom", "true");
        post("/api/ins/create", f, cb);
    }

    // ===================== 实例详情 / 控制 =====================

    public void detail(String insId, CB cb) {
        get("/api/ins/" + insId + "/detail", null, cb);
    }

    public void stat(String insId, CB cb) {
        get("/api/ins/" + insId + "/stat", null, cb);
    }

    public void tasks(String insId, CB cb) {
        get("/api/ins/" + insId + "/tasks", null, cb);
    }

    public void sftp(String insId, CB cb) {
        get("/api/ins/" + insId + "/sftp", null, cb);
    }

    public void power(String insId, String action, CB cb) {
        get("/api/ins/" + insId + "/power", q("action", action), cb);
    }

    public void reinstall(String insId, int versionId, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("version_id", String.valueOf(versionId));
        post("/api/ins/" + insId + "/reinstall", f, cb);
    }

    public void change(String insId, int itemId, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("item_id", String.valueOf(itemId));
        post("/api/ins/" + insId + "/change", f, cb);
    }

    public void rename(String insId, String name, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("name", name);
        post("/api/ins/" + insId + "/rename", f, cb);
    }

    public void destroy(String insId, CB cb) {
        post("/api/ins/" + insId + "/delete", new HashMap<>(), cb);
    }

    public void wsToken(String insId, CB cb) {
        get("/api/ins/" + insId + "/ws", null, cb);
    }

    // ===================== 备份 =====================

    public void backupList(String insId, CB cb) {
        get("/api/ins/" + insId + "/backup", null, cb);
    }

    public void backupCreate(String insId, String tag, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("tag", tag == null ? "" : tag);
        post("/api/ins/" + insId + "/backup", f, cb);
    }

    public void backupRestore(String insId, int backupId, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("backup_id", String.valueOf(backupId));
        patch("/api/ins/" + insId + "/backup", f, cb);
    }

    public void backupDelete(String insId, int backupId, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("backup_id", String.valueOf(backupId));
        delete("/api/ins/" + insId + "/backup", f, cb);
    }

    public void backupRename(String insId, int backupId, String newTag, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("backup_id", String.valueOf(backupId));
        f.put("new_tag", newTag == null ? "" : newTag);
        patch("/api/ins/" + insId + "/backup", f, cb);
    }

    // ===================== 文件管理 =====================

    public void fileList(String insId, String path, CB cb) {
        get("/api/ins/" + insId + "/file/list", q("path", path), cb);
    }

    public void fileFetch(String insId, String path, CB cb) {
        get("/api/ins/" + insId + "/file/fetch", q("path", path), cb);
    }

    public void fileSave(String insId, String path, String content, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("path", path);
        f.put("content", content == null ? "" : content);
        post("/api/ins/" + insId + "/file/save", f, cb);
    }

    public void fileCreate(String insId, String mode, String root, String name, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("mode", mode);          // file | dir
        f.put("root", root);
        f.put("name", name);
        post("/api/ins/" + insId + "/file/create", f, cb);
    }

    public void fileRename(String insId, String origin, String target, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("origin", origin);
        f.put("target", target);
        post("/api/ins/" + insId + "/file/rename", f, cb);
    }

    public void fileDelete(String insId, List<String> paths, CB cb) {
        Map<String, String> f = new HashMap<>();
        JSONArray arr = new JSONArray();
        for (String p : paths) arr.put(p);
        f.put("list", arr.toString());
        post("/api/ins/" + insId + "/file/delete", f, cb);
    }

    public void fileDownload(String insId, String path, CB cb) {
        get("/api/ins/" + insId + "/file/download", q("path", path), cb);
    }

    // ===================== 充值 / 支付 =====================

    public void payMeta(CB cb) {
        get("/api/pay/web/meta", null, cb);
    }

    public void recharge(int point, String method, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("point", String.valueOf(point));
        f.put("method", method == null ? "" : method);
        post("/api/recharge", f, cb);
    }

    public void payWebCreate(String item, int num, String method, CB cb) {
        Map<String, String> f = new HashMap<>();
        f.put("item", item);
        f.put("num", String.valueOf(num));
        f.put("method", method == null ? "" : method);
        post("/api/pay/web/create", f, cb);
    }
}
