package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.simpfun.app.R;
import com.simpfun.app.api.Ws;

public class TerminalFragment extends Fragment implements Ws.Listener {
    private String insId;
    private Ws ws;
    private TextView tvLog;
    private TextView tvStatus;
    private EditText etCmd;
    private ScrollView sv;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        insId = getArguments() != null ? getArguments().getString("ins_id") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_terminal, vg, false);
        tvLog = root.findViewById(R.id.tv_log);
        tvStatus = root.findViewById(R.id.tv_term_status);
        etCmd = root.findViewById(R.id.et_cmd);
        sv = root.findViewById(R.id.sv_log);
        Button btnSend = root.findViewById(R.id.btn_send);

        btnSend.setOnClickListener(v -> {
            String c = etCmd.getText().toString();
            if (c.isEmpty()) return;
            if (ws != null) ws.sendCommand(c);
            appendLog("$ " + c);
            etCmd.setText("");
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ws == null) {
            ws = new Ws(insId, this);
            ws.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ws != null) {
            ws.disconnect();
            ws = null;
        }
    }

    private void appendLog(String line) {
        if (tvLog == null) return;
        tvLog.append(line + "\n");
        sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onOpen() {
        tvStatus.setText("连接中…");
    }

    @Override
    public void onLog(String line) {
        appendLog(line);
    }

    @Override
    public void onStatus(String status) {
        tvStatus.setText("状态：" + status);
    }

    @Override
    public void onError(String msg) {
        tvStatus.setText("错误：" + msg);
        Toast.makeText(getContext(), "终端：" + msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClosed() {
        tvStatus.setText("已断开");
    }
}
