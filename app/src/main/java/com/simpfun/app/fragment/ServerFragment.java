package com.simpfun.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simpfun.app.CreateInstanceActivity;
import com.simpfun.app.InstanceDetailActivity;
import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.Instance;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 第一栏：服务器管理
 * 真实 API: GET /api/ins/list → {code:200, list:[{id, name, cpu, ram, disk, state, ptero_id, version_id, area, ...}]}
 */
public class ServerFragment extends Fragment {
    private ListView listView;
    private TextView tvEmpty;
    private Button btnRefresh;
    private final List<Instance> instances = new ArrayList<>();
    private ArrayAdapter<Instance> adapter;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle s) {
        View root = inf.inflate(R.layout.fragment_server, vg, false);
        listView = root.findViewById(R.id.list_server);
        tvEmpty = root.findViewById(R.id.tv_empty_server);
        btnRefresh = root.findViewById(R.id.btn_server_refresh);
        FloatingActionButton fab = root.findViewById(R.id.fab_create);

        adapter = new ArrayAdapter<Instance>(requireContext(), R.layout.item_instance, R.id.tv_item_name, instances) {
            @Override
            public View getView(int pos, View cv, ViewGroup parent) {
                View v = super.getView(pos, cv, parent);
                Instance it = getItem(pos);
                if (it != null) {
                    TextView tvName = v.findViewById(R.id.tv_item_name);
                    TextView tvStatus = v.findViewById(R.id.tv_item_status);
                    TextView tvVer = v.findViewById(R.id.tv_item_version);
                    tvName.setText(it.displayName());
                    tvStatus.setText(it.stateText());
                    tvStatus.setTextColor(statusColor(it.state));
                    tvVer.setText(it.configSummary());
                }
                return v;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, pos, id) -> openDetail(instances.get(pos)));
        btnRefresh.setOnClickListener(v -> loadServers());
        fab.setOnClickListener(v -> startActivity(new Intent(requireContext(), CreateInstanceActivity.class)));

        loadServers();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadServers();
    }

    private void openDetail(Instance ins) {
        if (ins.id <= 0) {
            Toast.makeText(getContext(), "实例 ID 缺失，无法进入", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent it = new Intent(requireContext(), InstanceDetailActivity.class);
        it.putExtra(InstanceDetailActivity.EXTRA_INS_ID, String.valueOf(ins.id));
        it.putExtra(InstanceDetailActivity.EXTRA_INS_NAME, ins.displayName());
        it.putExtra("extra_ptero_id", ins.pteroId);
        startActivity(it);
    }

    private int statusColor(int state) {
        // 0=离线(红), 1=启动中(黄), 2=运行中(绿), 3=停止中(橙)
        try {
            switch (state) {
                case 2: return requireContext().getColor(R.color.status_running);   // 绿
                case 1: return requireContext().getColor(R.color.status_starting); // 黄
                case 3: return requireContext().getColor(R.color.status_starting); // 橙
                default: return requireContext().getColor(R.color.status_stopped); // 红
            }
        } catch (Exception e) {
            return 0xFFE53935; // 默认红
        }
    }

    private void loadServers() {
        btnRefresh.setEnabled(false);
        ApiClient.listInstances(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                // ⭐ 关键修复: 真实响应字段是 "list" 不是 "data"
                JSONArray arr = resp.optJSONArray("list");
                if (arr == null) arr = resp.optJSONArray("data"); // 兼容 fallback
                instances.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            instances.add(Instance.from(arr.getJSONObject(i)));
                        } catch (Exception ignore) {}
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(instances.isEmpty() ? View.VISIBLE : View.GONE);
                    btnRefresh.setEnabled(true);
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    btnRefresh.setEnabled(true);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
