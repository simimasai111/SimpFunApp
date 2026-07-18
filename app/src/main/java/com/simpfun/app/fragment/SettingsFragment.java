package com.simpfun.app.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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

import com.simpfun.app.R;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.InstanceDetail;
import com.simpfun.app.model.SftpInfo;
import com.simpfun.app.util.Json;

import org.json.JSONObject;

public class SettingsFragment extends Fragment {
    private String insId;
    private int versionId = -1;
    private SftpInfo sftp;
    private TextView tvSftp, tvVersion;
    private ProgressBar pb;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        insId = getArguments() != null ? getArguments().getString("ins_id") : "";
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_settings, vg, false);
        tvSftp = root.findViewById(R.id.tv_sftp);
        tvVersion = root.findViewById(R.id.tv_version);
        pb = root.findViewById(R.id.pb_settings);

        Button btnRename = root.findViewById(R.id.btn_rename);
        Button btnReinstall = root.findViewById(R.id.btn_reinstall);
        Button btnDelete = root.findViewById(R.id.btn_delete);
        Button btnCopySftp = root.findViewById(R.id.btn_copy_sftp);

        btnRename.setOnClickListener(v -> renameDialog());
        btnReinstall.setOnClickListener(v -> reinstallDialog());
        btnDelete.setOnClickListener(v -> deleteDialog());
        btnCopySftp.setOnClickListener(v -> copySftp());

        load();
        return root;
    }

    private void load() {
        pb.setVisibility(View.VISIBLE);
        Api.get().detail(insId, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                InstanceDetail d = InstanceDetail.from(resp);
                versionId = d.versionId;
                requireActivity().runOnUiThread(() -> {
                    tvVersion.setText("版本：" + d.versionName + "（ID " + d.versionId + "）");
                });
                Api.get().sftp(insId, new Api.CB() {
                    @Override
                    public void ok(JSONObject r2) {
                        sftp = SftpInfo.from(r2);
                        requireActivity().runOnUiThread(() -> {
                            pb.setVisibility(View.GONE);
                            tvSftp.setText("地址：" + sftp.ip + (sftp.port.isEmpty() ? "" : (":" + sftp.port))
                                    + "\n账号：" + sftp.username
                                    + "\n密码：" + sftp.password);
                        });
                    }

                    @Override
                    public void fail(String msg) {
                        requireActivity().runOnUiThread(() -> {
                            pb.setVisibility(View.GONE);
                            tvSftp.setText("获取 SFTP 失败：" + msg);
                        });
                    }
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void renameDialog() {
        EditText et = new EditText(requireContext());
        et.setHint("新名称");
        new AlertDialog.Builder(requireContext())
                .setTitle("重命名实例")
                .setView(et)
                .setPositiveButton("确定", (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (name.isEmpty()) return;
                    Api.get().rename(insId, name, new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "已重命名", Toast.LENGTH_SHORT).show());
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

    private void reinstallDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("重装实例？")
                .setMessage("将使用当前版本重新安装系统，数据可能丢失。")
                .setPositiveButton("重装", (d, w) -> {
                    if (versionId < 0) {
                        Toast.makeText(getContext(), "版本未知，无法重装", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Api.get().reinstall(insId, versionId, new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "已提交重装", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void fail(String msg) {
                            requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "重装失败：" + msg, Toast.LENGTH_LONG).show());
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除实例？")
                .setMessage("此操作不可恢复！")
                .setPositiveButton("删除", (d, w) -> {
                    Api.get().destroy(insId, new Api.CB() {
                        @Override
                        public void ok(JSONObject r) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "已删除实例", Toast.LENGTH_SHORT).show();
                                requireActivity().finish();
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

    private void copySftp() {
        if (sftp == null || sftp.ip.isEmpty()) {
            Toast.makeText(getContext(), "SFTP 信息未加载", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = "地址:" + sftp.ip + (sftp.port.isEmpty() ? "" : (":" + sftp.port))
                + " 账号:" + sftp.username + " 密码:" + sftp.password;
        ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("sftp", text));
            Toast.makeText(getContext(), "SFTP 信息已复制", Toast.LENGTH_SHORT).show();
        }
    }
}
