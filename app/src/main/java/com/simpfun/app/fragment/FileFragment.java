package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.simpfun.app.adapter.FileAdapter;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.FileItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileFragment extends Fragment {
    private String insId;
    private String currentPath = "/";
    private RecyclerView rv;
    private ProgressBar pb;
    private TextView tvEmpty, tvPath;
    private FileAdapter adapter;
    private final List<FileItem> list = new ArrayList<>();

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        insId = getArguments() != null ? getArguments().getString("ins_id") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_file, vg, false);
        rv = root.findViewById(R.id.rv_files);
        pb = root.findViewById(R.id.pb_files);
        tvEmpty = root.findViewById(R.id.tv_empty_files);
        tvPath = root.findViewById(R.id.tv_path);
        Button btnUp = root.findViewById(R.id.btn_up);
        FloatingActionButton fab = root.findViewById(R.id.fab_file);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FileAdapter(list, this::onItem, this::showRenameDelete);
        rv.setAdapter(adapter);

        btnUp.setOnClickListener(v -> goUp());
        fab.setOnClickListener(v -> showCreateMenu());

        tvPath.setText(currentPath);
        load();
        return root;
    }

    private String joinDir(String base, String name) {
        if (base.equals("/")) return "/" + name;
        return base + "/" + name;
    }

    private void onItem(FileItem f) {
        if (f.isDir()) {
            currentPath = joinDir(currentPath, f.name);
            tvPath.setText(currentPath);
            load();
        } else {
            showFileMenu(f);
        }
    }

    private void goUp() {
        if (currentPath.equals("/")) return;
        int idx = currentPath.lastIndexOf('/');
        currentPath = idx <= 0 ? "/" : currentPath.substring(0, idx);
        tvPath.setText(currentPath);
        load();
    }

    private void load() {
        pb.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        Api.get().fileList(insId, currentPath, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list", "data");
                list.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) list.add(FileItem.from(arr.optJSONObject(i)));
                }
                Collections.sort(list, (a, b) -> {
                    if (a.isDir() != b.isDir()) return a.isDir() ? -1 : 1;
                    return a.name.compareToIgnoreCase(b.name);
                });
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
                    Toast.makeText(getContext(), "加载文件失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showFileMenu(FileItem f) {
        String[] items = {"查看 / 编辑", "下载"};
        new AlertDialog.Builder(requireContext())
                .setTitle(f.name)
                .setItems(items, (d, which) -> {
                    if (which == 0) viewFile(f);
                    else download(f);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void viewFile(FileItem f) {
        String full = joinDir(currentPath, f.name);
        pb.setVisibility(View.VISIBLE);
        Api.get().fileFetch(insId, full, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                requireActivity().runOnUiThread(() -> pb.setVisibility(View.GONE));
                String content;
                JSONObject data = Json.optObject(resp, "data");
                if (data != null) content = Json.optString(data, "content", "data");
                else content = Json.optString(resp, "data", "content");
                requireActivity().runOnUiThread(() -> editDialog(f, full, content));
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "读取失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void editDialog(FileItem f, String full, String content) {
        EditText et = new EditText(requireContext());
        et.setLines(10);
        et.setGravity(android.view.Gravity.TOP);
        et.setText(content);
        new AlertDialog.Builder(requireContext())
                .setTitle("编辑：" + f.name)
                .setView(et)
                .setPositiveButton("保存", (d, w) -> {
                    Api.get().fileSave(insId, full, et.getText().toString(), new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void fail(String msg) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "保存失败：" + msg, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void download(FileItem f) {
        String full = joinDir(currentPath, f.name);
        Api.get().fileDownload(insId, full, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                String url = Json.optString(resp, "data", "url", "download");
                requireActivity().runOnUiThread(() -> {
                    if (url.isEmpty()) {
                        Toast.makeText(getContext(), "未获取到下载链接", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        Intent it = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                        startActivity(it);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "下载链接：" + url, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "获取下载链接失败：" + msg, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void showCreateMenu() {
        String[] items = {"新建文件", "新建目录"};
        new AlertDialog.Builder(requireContext())
                .setTitle("新建")
                .setItems(items, (d, which) -> promptName(which == 1 ? "dir" : "file"))
                .show();
    }

    private void promptName(String mode) {
        EditText et = new EditText(requireContext());
        et.setHint("名称");
        new AlertDialog.Builder(requireContext())
                .setTitle(mode.equals("dir") ? "新建目录" : "新建文件")
                .setView(et)
                .setPositiveButton("创建", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) return;
                    Api.get().fileCreate(insId, mode, currentPath, name, new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "已创建", Toast.LENGTH_SHORT).show();
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

    private void showRenameDelete(final FileItem f) {
        String[] items = {"重命名", "删除"};
        new AlertDialog.Builder(requireContext())
                .setTitle(f.name)
                .setItems(items, (d, which) -> {
                    String full = joinDir(currentPath, f.name);
                    if (which == 0) {
                        EditText et = new EditText(requireContext());
                        et.setText(f.name);
                        new AlertDialog.Builder(requireContext())
                                .setTitle("重命名")
                                .setView(et)
                                .setPositiveButton("确定", (d2, w2) -> {
                                    String nn = et.getText().toString().trim();
                                    if (nn.isEmpty()) return;
                                    Api.get().fileRename(insId, full, joinDir(currentPath, nn), new Api.CB() {
                                        @Override
                                        public void ok(JSONObject r) {
                                            requireActivity().runOnUiThread(() -> { Toast.makeText(getContext(), "已重命名", Toast.LENGTH_SHORT).show(); load(); });
                                        }

                                        @Override
                                        public void fail(String msg) {
                                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "重命名失败：" + msg, Toast.LENGTH_LONG).show());
                                        }
                                    });
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    } else {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("确认删除？")
                                .setMessage(f.name)
                                .setPositiveButton("删除", (d2, w2) -> {
                                    List<String> paths = new ArrayList<>();
                                    paths.add(full);
                                    Api.get().fileDelete(insId, paths, new Api.CB() {
                                        @Override
                                        public void ok(JSONObject r) {
                                            requireActivity().runOnUiThread(() -> { Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show(); load(); });
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
                })
                .show();
    }
}
