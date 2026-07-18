package com.simpfun.app.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simpfun.app.InstanceDetailActivity;
import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.BackupItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 备份 / 回档
 */
public class BackupFragment extends Fragment {
    private RecyclerView list;
    private TextView tvEmpty;
    private String insId = "";
    private final List<BackupItem> items = new ArrayList<>();
    private BackupAdapter adapter;

    public BackupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle s) {
        View root = inf.inflate(R.layout.fragment_backup, vg, false);
        insId = ((InstanceDetailActivity) requireActivity()).getInsId();
        tvEmpty = root.findViewById(R.id.tv_empty_backup);
        list = root.findViewById(R.id.list_backup);
        FloatingActionButton fab = root.findViewById(R.id.fab_backup);

        adapter = new BackupAdapter(items, this::onRollback);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setAdapter(adapter);

        fab.setOnClickListener(v -> createBackup());
        load();
        return root;
    }

    private void load() {
        ApiClient.backupList(insId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                List<BackupItem> parsed = new ArrayList<>();
                JSONArray arr = Json.toArray(resp);
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            parsed.add(BackupItem.from(arr.getJSONObject(i)));
                        } catch (Exception ignore) {
                        }
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    items.clear();
                    items.addAll(parsed);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void createBackup() {
        ApiClient.backupCreate(insId, new HashMap<>(), new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject r) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "备份已创建", Toast.LENGTH_SHORT).show();
                    load();
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void onRollback(BackupItem b) {
        new AlertDialog.Builder(requireContext())
                .setTitle("回档到 " + b.name + " ?")
                .setMessage("回档将用该备份覆盖当前服务器文件，确定继续？")
                .setPositiveButton("回档", (d, w) -> {
                    ApiClient.rollback(insId, b.id, new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject r) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "回档已提交", Toast.LENGTH_SHORT).show());
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

    interface BackupAction {
        void call(BackupItem b);
    }

    static class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.VH> {
        private final List<BackupItem> items;
        private final BackupAction onRollback;

        BackupAdapter(List<BackupItem> items, BackupAction onRollback) {
            this.items = items;
            this.onRollback = onRollback;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_backup, p, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            BackupItem b = items.get(pos);
            h.name.setText(b.name);
            h.meta.setText((b.time.isEmpty() ? "" : b.time + "  ") + b.size + " B");
            h.btn.setText("回档");
            h.btn.setOnClickListener(v -> onRollback.call(b));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView name, meta;
            Button btn;

            VH(View v) {
                super(v);
                name = v.findViewById(R.id.tv_backup_name);
                meta = v.findViewById(R.id.tv_backup_meta);
                btn = v.findViewById(R.id.btn_rollback);
            }
        }
    }
}
