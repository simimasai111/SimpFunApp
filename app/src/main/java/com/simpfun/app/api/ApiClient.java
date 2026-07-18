package com.simpfun.app.api;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 简幻欢 API 客户端
 * - 域名：https://api.simpfun.cn
 * - 鉴权：请求头 Authorization: <token>
 * - 非 GET 请求体：application/x-www-form-urlencoded
 * - 响应：统一 { code, msg, data }，code==200 视为成功
 * 网络请求放在子线程，回调通过主线程 Handler 抛回 UI 线程。
 */
public final class ApiClient {
    public static final String BASE_URL = "https://api.simpfun.cn";
    private static String sToken = "";

    public static void setToken(String token) {
        sToken = token == null ? "" : token;
    }

    public static String getToken() {
        return sToken;
    }

    public interface ApiCallback {
        void onSuccess(JSONObject response);

        void onError(String error);
    }

    private ApiClient() {
    }

    private static void postToUi(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static void get(String endpoint, ApiCallback cb) {
        request("GET", endpoint, null, cb);
    }

    public static void post(String endpoint, Map<String, String> params, ApiCallback cb) {
        request("POST", endpoint, params, cb);
    }

    private static void request(final String method, final String endpoint,
                                final Map<String, String> params, final ApiCallback cb) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("Accept", "application/json");
                if (!sToken.isEmpty()) {
                    conn.setRequestProperty("Authorization", sToken);
                }

                if (params != null && !params.isEmpty()) {
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setDoOutput(true);
                    StringBuilder body = new StringBuilder();
                    boolean first = true;
                    for (Map.Entry<String, String> e : params.entrySet()) {
                        if (!first) body.append("&");
                        body.append(enc(e.getKey())).append("=").append(enc(e.getValue() == null ? "" : e.getValue()));
                        first = false;
                    }
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                    }
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                if (is != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                    }
                }
                String respText = sb.toString();

                if (code >= 200 && code < 300) {
                    JSONObject json = new JSONObject(respText);
                    int apiCode = json.optInt("code", code);
                    if (apiCode == 200) {
                        final JSONObject full = json;
                        postToUi(() -> cb.onSuccess(full));
                    } else {
                        final String msg = json.optString("msg", "请求失败 (code=" + apiCode + ")");
                        postToUi(() -> cb.onError(msg));
                    }
                } else {
                    final String err = "HTTP " + code + (respText.isEmpty() ? "" : ": " + respText);
                    postToUi(() -> cb.onError(err));
                }
            } catch (final Exception e) {
                final String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                postToUi(() -> cb.onError(msg));
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    // ===================== 鉴权 / 用户 =====================
    public static void login(Map<String, String> params, ApiCallback cb) {
        post("/api/auth/login", params, cb);
    }

    public static void getUserInfo(ApiCallback cb) {
        get("/api/auth/info", cb);
    }

    // ===================== 邀请 =====================
    public static void getInvite(ApiCallback cb) {
        get("/api/invite", cb);
    }

    // ===================== 实例管理 =====================
    public static void listInstances(ApiCallback cb) {
        get("/api/ins/list", cb);
    }

    public static void getInstanceDetail(String insId, ApiCallback cb) {
        get("/api/ins/" + insId + "/detail", cb);
    }

    public static void power(String insId, String action, ApiCallback cb) {
        get("/api/ins/" + insId + "/power?action=" + action, cb);
    }

    public static void createInstance(Map<String, String> params, ApiCallback cb) {
        post("/api/ins/create", params, cb);
    }

    public static void reinstall(String insId, ApiCallback cb) {
        get("/api/ins/" + insId + "/reinstall", cb);
    }

    public static void renameInstance(String insId, Map<String, String> params, ApiCallback cb) {
        post("/api/ins/" + insId + "/rename", params, cb);
    }

    public static void deleteInstance(String insId, ApiCallback cb) {
        post("/api/ins/" + insId + "/delete", null, cb);
    }

    // ===================== 游戏 / 版本选择 =====================
    public static void getGames(ApiCallback cb) {
        get("/api/games/list", cb);
    }

    public static void getGameKinds(ApiCallback cb) {
        get("/api/games/kindlist", cb);
    }

    public static void getGameVersions(ApiCallback cb) {
        get("/api/games/versionlist", cb);
    }

    // ===================== 终端 WebSocket =====================
    public static void getWsToken(String insId, ApiCallback cb) {
        get("/api/ins/" + insId + "/ws", cb);
    }

    // ===================== 文件管理 =====================
    public static void fileList(String insId, String dir, ApiCallback cb) {
        get("/api/ins/" + insId + "/file/list?file_dir=" + enc(dir == null ? "" : dir), cb);
    }

    public static void fileFetch(String insId, String dir, ApiCallback cb) {
        get("/api/ins/" + insId + "/file/fetch?file_dir=" + enc(dir == null ? "" : dir), cb);
    }

    public static void fileSave(String insId, Map<String, String> params, ApiCallback cb) {
        post("/api/ins/" + insId + "/file/save", params, cb);
    }

    public static void fileCreate(String insId, Map<String, String> params, ApiCallback cb) {
        post("/api/ins/" + insId + "/file/create", params, cb);
    }

    public static void fileRename(String insId, Map<String, String> params, ApiCallback cb) {
        post("/api/ins/" + insId + "/file/rename", params, cb);
    }

    public static void fileDelete(String insId, Map<String, String> params, ApiCallback cb) {
        post("/api/ins/" + insId + "/file/delete", params, cb);
    }

    public static void fileArchive(String insId, String dir, ApiCallback cb) {
        get("/api/ins/" + insId + "/file/archive?file_dir=" + enc(dir == null ? "" : dir), cb);
    }

    public static void fileUploadUrl(String insId, String dir, ApiCallback cb) {
        get("/api/ins/" + insId + "/file/upload?file_dir=" + enc(dir == null ? "" : dir), cb);
    }

    // ===================== 备份 / 回档 =====================
    public static void backupList(String insId, ApiCallback cb) {
        get("/api/ins/" + insId + "/backup", cb);
    }

    public static void backupCreate(String insId, Map<String, String> params, ApiCallback cb) {
        post("/api/ins/" + insId + "/backup", params, cb);
    }

    public static void rollback(String insId, String backupId, ApiCallback cb) {
        get("/api/ins/" + insId + "/rollback?backup_id=" + enc(backupId == null ? "" : backupId), cb);
    }

    // ===================== 商店 / 充值 =====================
    public static void shopList(ApiCallback cb) {
        get("/api/shop/list", cb);
    }

    public static void shopBuy(Map<String, String> params, ApiCallback cb) {
        post("/api/shop/confirmation", params, cb);
    }

    public static void recharge(ApiCallback cb) {
        get("/api/recharge", cb);
    }

    public static void payWebCreate(Map<String, String> params, ApiCallback cb) {
        post("/api/pay/web/create", params, cb);
    }
}
