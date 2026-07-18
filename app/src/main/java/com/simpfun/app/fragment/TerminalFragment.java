package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.simpfun.app.InstanceDetailActivity;
import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.api.WsClient;

import org.json.JSONObject;

/**
 * 终端：通过 WebSocket 实时接收服务器日志、发送命令。
 */
public class TerminalFragment extends Fragment implements WsClient.WsListener {
    private TextView tvLog;
    private TextView tvStatus;
    private EditText etCmd;
    private ScrollView scroll;
    private WsClient ws;
    private boolean closed = false;

    public TerminalFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle s) {
        View root = inf.inflate(R.layout.fragment_terminal, vg, false);
        tvLog = root.findViewById(R.id.tv_terminal_log);
        tvStatus = root.findViewById(R.id.tv_terminal_status);
        etCmd = root.findViewById(R.id.et_terminal_cmd);
        Button btnSend = root.findViewById(R.id.btn_terminal_send);
        scroll = root.findViewById(R.id.scroll_terminal);

        btnSend.setOnClickListener(v -> {
            String c = etCmd.getText().toString().trim();
            if (!c.isEmpty() && ws != null) {
                ws.sendCommand(c);
                append("> " + c);
                etCmd.setText("");
            }
        });

        String insId = ((InstanceDetailActivity) requireActivity()).getInsId();
        connectWs(insId);
        return root;
    }

    private void connectWs(String insId) {
        append("正在连接终端…");
        ApiClient.getWsToken(insId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                if (d == null) {
                    append("获取终端地址失败：data 为空");
                    return;
                }
                String socket = d.optString("socket", d.optString("url", ""));
                String token = d.optString("token", "");
                if (socket.isEmpty()) {
                    append("获取终端地址失败：socket 为空");
                    return;
                }
                ws = new WsClient(socket, token, TerminalFragment.this);
                ws.connect();
            }

            @Override
            public void onError(String e) {
                append("获取终端地址失败：" + e);
            }
        });
    }

    private void append(String line) {
        if (tvLog == null) return;
        String t = tvLog.getText().toString();
        String nl = t.isEmpty() ? "" : "\n";
        tvLog.setText(t + nl + translate(line));
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    /** 简幻欢前端日志汉化映射（常见 Pterodactyl 英文 -> 中文） */
    private String translate(String line) {
        line = line.replace("[Pterodactyl Daemon]", "[简幻欢]")
                .replace("[Pterodactyl]", "[简幻欢]");
        if (line.contains("Server marked as starting")) return "服务器正在启动…";
        if (line.contains("Server is running")) return "服务器正在运行";
        if (line.contains("Server is stopping")) return "服务器正在停止";
        if (line.contains("Server is offline")) return "服务器已停止";
        if (line.contains("Container crashed") || line.contains("process has crashed"))
            return "服务器进程已崩溃";
        if (line.contains("Out of memory")) return "内存不足导致崩溃";
        if (line.contains("Unknown command")) return "未知指令";
        return line;
    }

    @Override
    public void onOpen() {
        append("【已连接】");
    }

    @Override
    public void onLog(String line) {
        append(line);
    }

    @Override
    public void onStatus(String status) {
        if (tvStatus != null) tvStatus.setText("状态：" + status);
    }

    @Override
    public void onStats(JSONObject stats) {
        // 资源统计（CPU/内存/网络）可在此解析展示，简化为保留接口
    }

    @Override
    public void onTokenExpiring() {
        append("【token 即将过期，刷新中…】");
        String insId = ((InstanceDetailActivity) requireActivity()).getInsId();
        ApiClient.getWsToken(insId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                if (d != null && ws != null) {
                    ws.setToken(d.optString("token", ""));
                    ws.reauth();
                }
            }

            @Override
            public void onError(String e) {
                append("刷新 token 失败：" + e);
            }
        });
    }

    @Override
    public void onClosed() {
        if (!closed) append("【连接已关闭】");
    }

    @Override
    public void onError(String msg) {
        append("【错误】" + msg);
    }

    @Override
    public void onDestroy() {
        closed = true;
        if (ws != null) ws.close();
        super.onDestroy();
    }
}
