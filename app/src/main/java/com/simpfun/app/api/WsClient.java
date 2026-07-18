package com.simpfun.app.api;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * 简幻欢终端 WebSocket 客户端（Pterodactyl Wings 守护进程协议）。
 *
 * 流程：
 *  1) 先调用 REST: GET /api/ins/{id}/ws 拿到 { socket, token }
 *  2) 连接 socket（wss://...），onOpen 后发送 { event:"auth", args:[token] }
 *  3) 收到 "auth success" 后发送 { event:"send logs", args:[] } 拉历史日志
 *  4) 服务端持续推送：
 *       "console output" -> args[0] 日志文本
 *       "status"         -> args[0] 状态(running/stopped/starting/...)
 *       "stats"          -> args[0] 资源统计 JSON
 *       "token expiring"  -> 需重新 REST 拿 token 再 auth
 *  5) 发命令：{ event:"send command", args:[命令] }
 *  6) 强停：  { event:"set state", args:["kill"] }
 */
public class WsClient {
    public interface WsListener {
        void onOpen();

        void onLog(String line);

        void onStatus(String status);

        void onStats(JSONObject stats);

        void onTokenExpiring();

        void onClosed();

        void onError(String msg);
    }

    private final OkHttpClient http = new OkHttpClient.Builder()
            .pingInterval(20, TimeUnit.SECONDS)
            .build();
    private WebSocket ws;
    private final String socketUrl;
    private String token;
    private final WsListener listener;
    private final Handler ui = new Handler(Looper.getMainLooper());

    public WsClient(String socketUrl, String token, WsListener listener) {
        this.socketUrl = socketUrl;
        this.token = token == null ? "" : token;
        this.listener = listener;
    }

    public void setToken(String t) {
        this.token = t == null ? "" : t;
    }

    public void connect() {
        Request req = new Request.Builder().url(socketUrl).build();
        ws = http.newWebSocket(req, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                send("auth", token);
                ui.post(listener::onOpen);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handle(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                handle(bytes.utf8());
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response r) {
                ui.post(() -> listener.onError(t == null ? "连接失败" : t.getMessage()));
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                ui.post(listener::onClosed);
            }
        });
    }

    private void handle(String text) {
        try {
            JSONObject msg = new JSONObject(text);
            String event = msg.optString("event", "");
            String s0 = "";
            JSONObject arg0 = null;
            if (msg.has("args")) {
                Object a = msg.get("args");
                if (a instanceof JSONArray) {
                    JSONArray arr = (JSONArray) a;
                    if (arr.length() > 0) {
                        Object o = arr.get(0);
                        if (o instanceof JSONObject) arg0 = (JSONObject) o;
                        else s0 = String.valueOf(o);
                    }
                } else {
                    s0 = String.valueOf(a);
                }
            }
            switch (event) {
                case "auth success":
                    send("send logs", "");
                    break;
                case "console output":
                    final String log = s0;
                    ui.post(() -> listener.onLog(log));
                    break;
                case "status":
                    final String st = s0;
                    ui.post(() -> listener.onStatus(st));
                    break;
                case "stats":
                    if (arg0 != null) {
                        final JSONObject stats = arg0;
                        ui.post(() -> listener.onStats(stats));
                    }
                    break;
                case "token expiring":
                    ui.post(listener::onTokenExpiring);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            ui.post(() -> listener.onError(e.getMessage()));
        }
    }

    public void sendCommand(String cmd) {
        send("send command", cmd);
    }

    public void requestLogs() {
        send("send logs", "");
    }

    public void reauth() {
        send("auth", token);
    }

    /** 强制停止服务器进程 */
    public void kill() {
        send("set state", "kill");
    }

    private void send(String event, String arg) {
        if (ws == null) return;
        try {
            JSONObject o = new JSONObject();
            o.put("event", event);
            JSONArray args = new JSONArray();
            args.put(arg == null ? "" : arg);
            o.put("args", args);
            ws.send(o.toString());
        } catch (Exception ignore) {
        }
    }

    public void close() {
        if (ws != null) ws.close(1000, "bye");
    }
}
