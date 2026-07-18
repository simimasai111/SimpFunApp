package com.simpfun.app.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simpfun.app.InstanceDetailActivity;
import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.FileItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 文件管理：目录浏览、查看/编辑、新建、重命名、删除、下载、上传。
 */
public class FileFragment extends Fragment {
    private RecyclerView list;
    private TextView tvPath;
    private TextView tvEmpty;
    private String insId = "";
    private String currentDir = "/";
    private final List<FileItem> items = new ArrayList<>();
    private FileAdapter adapter;

    private final ActivityResultLauncher<String> pickFile =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::doUpload);

    public FileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle s) {
        View root = inf.inflate(R.layout.fragment_file, vg, false);
        insId = ((InstanceDetailActivity) requireActivity()).getInsId();

        tvPath = root.findViewById(R.id.tv_path);
        tvEmpty = root.findViewById(R.id.tv_empty_file);
        list = root.findViewById(R.id.list_file);
        Button btnUp = root.findViewById(R.id.btn_up);
        FloatingActionButton fab = root.findViewById(R.id.fab_file);

        adapter = new FileAdapter(items, this::onItemClick, this::onItemLongClick);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        list.setAdapter(adapter);

        btnUp.setOnClickListener(v -> {
            currentDir = parentDir(currentDir);
            load();
        });
        fab.setOnClickListener(v -> showCreateMenu());

        load();
        return root;
    }

    private void load() {
        tvPath.setText("路径：" + currentDir);
        ApiClient.fileList(insId, currentDir, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                List<FileItem> parsed = parseList(resp);
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

    private List<FileItem> parseList(JSONObject resp) {
        List<FileItem> out = new ArrayList<>();
        JSONArray arr = Json.toArray(resp);
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    out.add(FileItem.from(arr.getJSONObject(i)));
                } catch (Exception ignore) {
                }
            }
        }
        return out;
    }

    private void onItemClick(FileItem f) {
        if (f.isDir) {
            currentDir = joinDir(currentDir, f.name);
            load();
        } else {
            openFile(f);
        }
    }

    private void onItemLongClick(FileItem f) {
        String[] ops = {"查看/编辑", "重命名", "删除", "下载"};
        new AlertDialog.Builder(requireContext())
                .setTitle(f.name)
                .setItems(ops, (d, which) -> {
                    switch (which) {
                        case 0:
                            openFile(f);
                            break;
                        case 1:
                            showRename(f);
                            break;
                        case 2:
                            showDelete(f);
                            break;
                        case 3:
                            download(f);
                            break;
                    }
                }).show();
    }

    private void openFile(FileItem f) {
        String path = joinDir(currentDir, f.name);
        ApiClient.fileFetch(insId, path, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                String content = d == null ? "" : Json.pick(d, "file_body", "content", "body", "code", "text");
                requireActivity().runOnUiThread(() -> showEditor(path, content));
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showEditor(String path, String content) {
        EditText et = new EditText(getContext());
        et.setGravity(android.view.Gravity.TOP);
        et.setMinLines(8);
        et.setText(content);
        new AlertDialog.Builder(requireContext())
                .setTitle("编辑：" + path)
                .setView(et)
                .setPositiveButton("保存", (d, w) -> {
                    Map<String, String> p = new HashMap<>();
                    p.put("path", path);
                    p.put("content", et.getText().toString());
                    ApiClient.fileSave(insId, p, new ApiClient.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject r) {
                            Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String e) {
                            Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showRename(FileItem f) {
        EditText et = new EditText(getContext());
        et.setText(f.name);
        new AlertDialog.Builder(requireContext())
                .setTitle("重命名")
                .setView(et)
                .setPositiveButton("确定", (d, w) -> {
                    String newName = et.getText().toString().trim();
                    if (newName.isEmpty()) return;
                    Map<String, String> p = new HashMap<>();
                    p.put("origin", joinDir(currentDir, f.name));
                    p.put("target", joinDir(currentDir, newName));
                    ApiClient.fileRename(insId, p, cb("已重命名"));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDelete(FileItem f) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除 " + f.name + " ?")
                .setPositiveButton("删除", (d, w) -> {
                    Map<String, String> p = new HashMap<>();
                    p.put("list", "[\"" + joinDir(currentDir, f.name) + "\"]");
                    ApiClient.fileDelete(insId, p, cb("已删除"));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void download(FileItem f) {
        ApiClient.fileDownload(insId, joinDir(currentDir, f.name), new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                String url = d == null ? "" : Json.pick(d, "url", "download", "link", "src");
                requireActivity().runOnUiThread(() -> {
                    if (!url.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else {
                        Toast.makeText(getContext(), "未获取到下载地址", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showCreateMenu() {
        String[] ops = {"新建文件", "新建目录", "上传文件"};
        new AlertDialog.Builder(requireContext())
                .setTitle("操作")
                .setItems(ops, (d, which) -> {
                    if (which == 0) createEntry(false);
                    else if (which == 1) createEntry(true);
                    else pickFile.launch("*/*");
                }).show();
    }

    private void createEntry(boolean dir) {
        EditText et = new EditText(getContext());
        et.setHint("名称");
        new AlertDialog.Builder(requireContext())
                .setTitle(dir ? "新建目录" : "新建文件")
                .setView(et)
                .setPositiveButton("确定", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) return;
                    Map<String, String> p = new HashMap<>();
                    p.put("mode", dir ? "dir" : "file");
                    p.put("root", currentDir);
                    p.put("name", name);
                    ApiClient.fileCreate(insId, p, cb(dir ? "已创建目录" : "已创建文件"));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void doUpload(Uri uri) {
        if (uri == null) return;
        ApiClient.fileUploadUrl(insId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                String url = d == null ? "" : Json.pick(d, "url", "upload", "link", "src");
                if (url.isEmpty()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "未获取到上传地址", Toast.LENGTH_SHORT).show());
                    return;
                }
                uploadTo(url, uri);
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void uploadTo(String url, Uri uri) {
        new Thread(() -> {
            try {
                byte[] data = readUri(uri);
                OkHttpClient c = new OkHttpClient();
                RequestBody fileBody = RequestBody.create(data, MediaType.parse("*/*"));
                MultipartBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", fileName(uri), fileBody)
                        .build();
                Request req = new Request.Builder().url(url).post(body).build();
                try (Response r = c.newCall(req).execute()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "上传完成", Toast.LENGTH_SHORT).show();
                        load();
                    });
                }
            } catch (final Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "上传失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private byte[] readUri(Uri uri) throws Exception {
        java.io.InputStream in = requireContext().getContentResolver().openInputStream(uri);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        return out.toByteArray();
    }

    private String fileName(Uri uri) {
        String name = null;
        try (android.database.Cursor cur = requireContext().getContentResolver()
                .query(uri, new String[]{MediaStore.Files.FileColumns.DISPLAY_NAME}, null, null, null)) {
            if (cur != null && cur.moveToFirst()) {
                name = cur.getString(0);
            }
        } catch (Exception ignore) {
        }
        if (name == null) name = Uri.parse(uri.toString()).getLastPathSegment();
        return name == null ? "upload.bin" : name;
    }

    private ApiClient.ApiCallback cb(String okMsg) {
        return new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject r) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), okMsg, Toast.LENGTH_SHORT).show();
                    load();
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        };
    }

    private String parentDir(String dir) {
        if (dir.equals("/") || dir.isEmpty()) return "/";
        String d = dir.endsWith("/") ? dir.substring(0, dir.length() - 1) : dir;
        int idx = d.lastIndexOf('/');
        return idx <= 0 ? "/" : d.substring(0, idx);
    }

    private String joinDir(String base, String name) {
        if (base.endsWith("/")) return base + name;
        return base + "/" + name;
    }

    interface FileAction {
        void call(FileItem f);
    }

    static class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {
        private final List<FileItem> items;
        private final FileAction onClick;
        private final FileAction onLong;

        FileAdapter(List<FileItem> items, FileAction onClick, FileAction onLong) {
            this.items = items;
            this.onClick = onClick;
            this.onLong = onLong;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup p, int v) {
            View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_file, p, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            FileItem f = items.get(pos);
            h.icon.setText(f.isDir ? "📁" : "📄");
            h.name.setText(f.name);
            StringBuilder meta = new StringBuilder();
            if (!f.isDir) meta.append(f.size).append(" B");
            if (!f.time.isEmpty()) meta.append("  ").append(f.time);
            h.meta.setText(meta.toString());
            h.itemView.setOnClickListener(v -> onClick.call(f));
            h.itemView.setOnLongClickListener(v -> {
                onLong.call(f);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView icon, name, meta;

            VH(View v) {
                super(v);
                icon = v.findViewById(R.id.tv_file_icon);
                name = v.findViewById(R.id.tv_file_name);
                meta = v.findViewById(R.id.tv_file_meta);
            }
        }
    }
}
