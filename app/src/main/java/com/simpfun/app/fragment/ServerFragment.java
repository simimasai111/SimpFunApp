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
 * 列表 GET /api/ins/list；点击实例进入详情（终端/文件/备份/设置）；右下角 FAB 创建实例。
 */
public class ServerFragment extends Fragment {
    private ListView listView;
    private TextView tvEmpty;
    private Button btnRefresh;
    private final List<Instance> instances = new ArrayList<>();
    private ArrayAdapter<Instance> adapter;

    public ServerFragment() {
    }

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
                    tvStatus.setText(it.status.isEmpty() ? "未知状态" : it.status);
                    tvStatus.setTextColor(statusColor(it.status));
                    tvVer.setText(it.version.isEmpty() ? "" : it.version);
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
        if (ins.id.isEmpty()) {
            Toast.makeText(getContext(), "实例 ID 缺失，无法进入", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent it = new Intent(requireContext(), InstanceDetailActivity.class);
        it.putExtra(InstanceDetailActivity.EXTRA_INS_ID, ins.id);
        it.putExtra(InstanceDetailActivity.EXTRA_INS_NAME, ins.displayName());
        startActivity(it);
    }

    private int statusColor(String status) {
        int c = requireContext().getColor(R.color.status_stopped);
        if (status == null) return c;
        switch (status.toLowerCase()) {
            case "running":
                return requireContext().getColor(R.color.status_running);
            case "starting":
            case "installing":
                return requireContext().getColor(R.color.status_starting);
            case "stopped":
            case "offline":
                return requireContext().getColor(R.color.status_stopped);
            default:
                return c;
        }
    }

    private void loadServers() {
        btnRefresh.setEnabled(false);
        ApiClient.listInstances(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONArray arr = toArray(resp.opt("data"));
                instances.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            instances.add(Instance.from(arr.getJSONObject(i)));
                        } catch (Exception ignore) {
                        }
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

    private JSONArray toArray(Object d) {
        if (d instanceof JSONArray) return (JSONArray) d;
        if (d instanceof JSONObject) {
            JSONObject o = (JSONObject) d;
            for (String k : new String[]{"list", "ins", "data", "instances", "servers", "items"}) {
                if (o.optJSONArray(k) != null) return o.optJSONArray(k);
            }
        }
        return null;
    }
}
