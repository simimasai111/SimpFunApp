package com.simpfun.app.api;

import android.os.Handler;
import android.os.Looper;

import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * 终端 WebSocket 客户端（Pterodactyl Wings 协议）。
 * 实测流程：GET /api/ins/{id}/ws -> {data:{token(JWT), socket(wss)}}
 * 连接地址 = socket + "?token=" + JWT
 * 事件：auth success / console output / status / ping(->pong) / token expired(->重连)
 */
public class Ws {
    public interface Listener {
        void onOpen();

        void onLog(String line);

        void onStatus(String status);

        void onError(String msg);

        void onClosed();
    }

    private final String insId;
    private final Listener listener;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private OkHttpClient client;
    private WebSocket ws;
    private boolean closedByUser = false;

    public Ws(String insId, Listener l) {
        this.insId = insId;
        this.listener = l;
    }

    public void connect() {
        Api.get().wsToken(insId, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONObject data = Json.optObject(resp, "data");
                if (data == null) {
                    ui.post(() -> listener.onError("未获取到终端地址"));
                    return;
                }
                String socket = Json.optString(data, "socket");
                String token = Json.optString(data, "token");
                if (socket.isEmpty() || token.isEmpty()) {
                    ui.post(() -> listener.onError("终端地址或令牌为空"));
                    return;
                }
                open(socket + "?token=" + token);
            }

            @Override
            public void fail(String msg) {
                ui.post(() -> listener.onError("获取终端令牌失败：" + msg));
            }
        });
    }

    private void open(String url) {
        client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
        Request req = new Request.Builder().url(url).build();
        ws = client.newWebSocket(req, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket w, Response response) {
                ui.post(listener::onOpen);
            }

            @Override
            public void onMessage(WebSocket w, String text) {
                handle(text);
            }

            @Override
            public void onFailure(WebSocket w, Throwable t, Response response) {
                if (closedByUser) return;
                ui.post(() -> listener.onError("连接失败：" + t.getMessage()));
            }

            @Override
            public void onClosed(WebSocket w, int code, String reason) {
                if (!closedByUser) ui.post(listener::onClosed);
            }
        });
    }

    private void handle(String text) {
        try {
            JSONObject o = new JSONObject(text);
            String event = o.optString("event", "");
            JSONArray args = Json.toArray(o.opt("args"));
            switch (event) {
                case "auth success":
                    ui.post(() -> listener.onStatus("已连接"));
                    break;
                case "console output":
                case "console":
                    if (args != null) {
                        for (int i = 0; i < args.length(); i++) {
                            final String line = args.optString(i);
                            ui.post(() -> listener.onLog(line));
                        }
                    }
                    break;
                case "status":
                    if (args != null && args.length() > 0) {
                        final String s = args.optString(0);
                        ui.post(() -> listener.onStatus(s));
                    }
                    break;
                case "ping":
                    if (ws != null) {
                        try {
                            ws.send("{\"event\":\"pong\",\"args\":[]}");
                        } catch (Exception ignored) {
                        }
                    }
                    break;
                case "token expiring":
                case "token expired":
                    reconnect();
                    break;
                default:
                    if (args != null && args.length() > 0) {
                        final String line = args.optString(0);
                        ui.post(() -> listener.onLog(line));
                    }
                    break;
            }
        } catch (Exception e) {
            ui.post(() -> listener.onLog(text));
        }
    }

    public void sendCommand(String cmdText) {
        if (ws == null) return;
        try {
            JSONObject o = new JSONObject();
            o.put("event", "send command");
            JSONArray a = new JSONArray();
            a.put(cmdText);
            o.put("args", a);
            ws.send(o.toString());
        } catch (Exception ignored) {
        }
    }

    public void reconnect() {
        if (closedByUser) return;
        if (ws != null) {
            ws.close(1000, "reconnect");
            ws = null;
        }
        connect();
    }

    public void disconnect() {
        closedByUser = true;
        if (ws != null) ws.close(1000, "user close");
        if (client != null) client.dispatcher().executorService().shutdown();
    }
}
