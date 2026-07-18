package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.simpfun.app.R;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.InstanceDetail;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

public class OverviewFragment extends Fragment {
    private String insId;
    private InstanceDetail detail;
    private TextView tvStatus, tvGame, tvConfig, tvAddr, tvUsage, tvTraffic, tvTasks;
    private ProgressBar pb;
    private Button btnStart, btnStop, btnRestart;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        insId = getArguments() != null ? getArguments().getString("ins_id") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_overview, vg, false);
        tvStatus = root.findViewById(R.id.tv_status);
        tvGame = root.findViewById(R.id.tv_game);
        tvConfig = root.findViewById(R.id.tv_config);
        tvAddr = root.findViewById(R.id.tv_addr);
        tvUsage = root.findViewById(R.id.tv_usage);
        tvTraffic = root.findViewById(R.id.tv_traffic);
        tvTasks = root.findViewById(R.id.tv_tasks);
        pb = root.findViewById(R.id.pb_overview);
        btnStart = root.findViewById(R.id.btn_start);
        btnStop = root.findViewById(R.id.btn_stop);
        btnRestart = root.findViewById(R.id.btn_restart);

        View.OnClickListener power = v -> {
            String action = v.getId() == R.id.btn_start ? "start"
                    : v.getId() == R.id.btn_stop ? "stop" : "restart";
            doPower(action);
        };
        btnStart.setOnClickListener(power);
        btnStop.setOnClickListener(power);
        btnRestart.setOnClickListener(power);

        load();
        loadTasks();
        return root;
    }

    private void load() {
        pb.setVisibility(View.VISIBLE);
        Api.get().detail(insId, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                detail = InstanceDetail.from(resp);
                requireActivity().runOnUiThread(() -> fill());
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载详情失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void fill() {
        pb.setVisibility(View.GONE);
        if (detail == null) return;
        tvStatus.setText(detail.status.isEmpty() ? ("状态" + detail.state) : detail.status);
        tvGame.setText(detail.gameName + " / " + detail.kindName + " / " + detail.versionName);
        tvConfig.setText(detail.cpu + "核 / " + detail.ram + "G 内存 / " + detail.disk + "G 磁盘");
        tvAddr.setText((detail.ip.isEmpty() ? "—" : detail.ip) + (detail.port.isEmpty() ? "" : (":" + detail.port)));
        long mb = 1024L * 1024L;
        String usage = "CPU " + detail.cpuAbsolute + "%  内存 " + (detail.memBytes / mb) + " MB  磁盘 " + (detail.diskBytes / mb) + " MB";
        tvUsage.setText(usage);
        if (detail.trafficRemain > 0) {
            tvTraffic.setText("流量套餐 " + detail.trafficPlan + "  剩余 " + (detail.trafficRemain / 1073741824L) + " GB");
        } else {
            tvTraffic.setText("流量套餐 " + detail.trafficPlan);
        }
    }

    private void loadTasks() {
        Api.get().tasks(insId, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list");
                int running = Json.optInt(resp, 0, "running");
                int waiting = Json.optInt(resp, 0, "waiting");
                requireActivity().runOnUiThread(() -> {
                    if (tvTasks != null) {
                        tvTasks.setText("队列：运行中 " + running + " / 等待 " + waiting + "，任务数 " + (arr == null ? 0 : arr.length()));
                    }
                });
            }

            @Override
            public void fail(String msg) {
            }
        });
    }

    private void doPower(String action) {
        Api.get().power(insId, action, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "已发送：" + action, Toast.LENGTH_SHORT).show();
                    load();
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "操作失败：" + msg, Toast.LENGTH_LONG).show());
            }
        });
    }
}
