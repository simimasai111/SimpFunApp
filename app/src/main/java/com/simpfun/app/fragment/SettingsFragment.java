package com.simpfun.app.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.simpfun.app.InstanceDetailActivity;
import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 实例设置：电源操作、重装、更名、删除
 */
public class SettingsFragment extends Fragment {
    private String insId = "";
    private TextView tvInfo;

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle s) {
        View root = inf.inflate(R.layout.fragment_instance_settings, vg, false);
        insId = ((InstanceDetailActivity) requireActivity()).getInsId();

        tvInfo = root.findViewById(R.id.tv_ins_info);
        Button btnStart = root.findViewById(R.id.btn_start);
        Button btnStop = root.findViewById(R.id.btn_stop);
        Button btnRestart = root.findViewById(R.id.btn_restart);
        Button btnReinstall = root.findViewById(R.id.btn_reinstall);
        Button btnRename = root.findViewById(R.id.btn_rename);
        Button btnDelete = root.findViewById(R.id.btn_delete);

        btnStart.setOnClickListener(v -> power("start"));
        btnStop.setOnClickListener(v -> power("stop"));
        btnRestart.setOnClickListener(v -> power("restart"));
        btnReinstall.setOnClickListener(v -> power("reinstall"));
        btnRename.setOnClickListener(v -> showRename());
        btnDelete.setOnClickListener(v -> showDelete());

        loadDetail();
        return root;
    }

    private void loadDetail() {
        ApiClient.getInstanceDetail(insId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                StringBuilder sb = new StringBuilder();
                if (d != null) {
                    for (String k : new String[]{"instance_uuid", "uuid", "friendly_name", "name",
                            "state", "status", "image", "memory", "version_name", "version", "port"}) {
                        if (d.has(k) && !d.isNull(k)) {
                            sb.append(k).append("：").append(d.optString(k)).append("\n");
                        }
                    }
                }
                if (sb.length() == 0) sb.append("（未获取到实例详情，可尝试电源操作）");
                requireActivity().runOnUiThread(() -> tvInfo.setText(sb.toString()));
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> tvInfo.setText("详情加载失败：" + e));
            }
        });
    }

    private void power(String action) {
        ApiClient.power(insId, action, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject r) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "已发送：" + action, Toast.LENGTH_SHORT).show();
                    loadDetail();
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showRename() {
        EditText et = new EditText(getContext());
        et.setHint("新名称");
        new AlertDialog.Builder(requireContext())
                .setTitle("更名")
                .setView(et)
                .setPositiveButton("确定", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) return;
                    Map<String, String> p = new HashMap<>();
                    p.put("name", name);
                    ApiClient.renameInstance(insId, p, new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject r) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "已更名", Toast.LENGTH_SHORT).show();
                                loadDetail();
                            });
                        }

                        @Override
                        public void onError(String e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除实例？")
                .setMessage("删除后数据不可恢复！")
                .setPositiveButton("删除", (d, w) -> {
                    ApiClient.deleteInstance(insId, new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject r) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
                                requireActivity().finish();
                            });
                        }

                        @Override
                        public void onError(String e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
