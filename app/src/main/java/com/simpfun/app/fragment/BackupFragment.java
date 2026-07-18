package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simpfun.app.R;
import com.simpfun.app.adapter.BackupAdapter;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.BackupItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BackupFragment extends Fragment {
    private String insId;
    private RecyclerView rv;
    private ProgressBar pb;
    private TextView tvEmpty;
    private BackupAdapter adapter;
    private final List<BackupItem> list = new ArrayList<>();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        insId = getArguments() != null ? getArguments().getString("ins_id") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_backup, vg, false);
        rv = root.findViewById(R.id.rv_backup);
        pb = root.findViewById(R.id.pb_backup);
        tvEmpty = root.findViewById(R.id.tv_empty_backup);
        FloatingActionButton fab = root.findViewById(R.id.fab_backup);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BackupAdapter(list, this::showMenu);
        rv.setAdapter(adapter);

        fab.setOnClickListener(v -> createDialog());
        load();
        return root;
    }

    private void load() {
        pb.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        Api.get().backupList(insId, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list", "data");
                list.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) list.add(BackupItem.from(arr.optJSONObject(i)));
                }
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "加载备份失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showMenu(BackupItem b) {
        String[] items = {"恢复此备份", "重命名", "删除"};
        new AlertDialog.Builder(requireContext())
                .setTitle(b.tag.isEmpty() ? ("备份 #" + b.id) : b.tag)
                .setItems(items, (d, which) -> {
                    if (which == 0) restore(b);
                    else if (which == 1) renameDialog(b);
                    else deleteDialog(b);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void restore(BackupItem b) {
        new AlertDialog.Builder(requireContext())
                .setTitle("恢复备份？")
                .setMessage("将把服务器恢复到该备份点，当前数据会被覆盖。")
                .setPositiveButton("恢复", (d, w) -> {
                    Api.get().backupRestore(insId, b.id, new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "已开始恢复", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void fail(String msg) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "恢复失败：" + msg, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteDialog(BackupItem b) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除备份？")
                .setMessage(b.tag.isEmpty() ? ("备份 #" + b.id) : b.tag)
                .setPositiveButton("删除", (d, w) -> {
                    Api.get().backupDelete(insId, b.id, new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
                                load();
                            });
                        }

                        @Override
                        public void fail(String msg) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "删除失败：" + msg, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void renameDialog(BackupItem b) {
        EditText et = new EditText(requireContext());
        et.setText(b.tag);
        new AlertDialog.Builder(requireContext())
                .setTitle("重命名备份")
                .setView(et)
                .setPositiveButton("确定", (d, w) -> {
                    String tag = et.getText().toString().trim();
                    Api.get().backupRename(insId, b.id, tag, new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "已重命名", Toast.LENGTH_SHORT).show();
                                load();
                            });
                        }

                        @Override
                        public void fail(String msg) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "重命名失败：" + msg, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void createDialog() {
        EditText et = new EditText(requireContext());
        et.setHint("备份备注（可选）");
        new AlertDialog.Builder(requireContext())
                .setTitle("创建备份")
                .setView(et)
                .setPositiveButton("创建", (d, w) -> {
                    Api.get().backupCreate(insId, et.getText().toString().trim(), new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "已创建备份", Toast.LENGTH_SHORT).show();
                                load();
                            });
                        }

                        @Override
                        public void fail(String msg) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "创建失败：" + msg, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
