package com.simpfun.app.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.InviteInfo;

import org.json.JSONObject;

/**
 * 第二栏：邀请信息
 * GET /api/invite 展示推荐码与统计；拼接邀请链接并支持复制。
 */
public class InviteFragment extends Fragment {
    private TextView tvCode, tvTimes, tvVerify, tvIncome, tvPro, tvLink;
    private Button btnRefresh, btnCopy;

    public InviteFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle s) {
        View root = inf.inflate(R.layout.fragment_invite, vg, false);
        tvCode = root.findViewById(R.id.tv_invite_code);
        tvTimes = root.findViewById(R.id.tv_register_times);
        tvVerify = root.findViewById(R.id.tv_register_verify);
        tvIncome = root.findViewById(R.id.tv_total_income);
        tvPro = root.findViewById(R.id.tv_pro_income);
        tvLink = root.findViewById(R.id.tv_invite_link);
        btnRefresh = root.findViewById(R.id.btn_invite_refresh);
        btnCopy = root.findViewById(R.id.btn_invite_copy);

        btnRefresh.setOnClickListener(v -> loadInvite());
        btnCopy.setOnClickListener(v -> copyLink());
        loadInvite();
        return root;
    }

    private void loadInvite() {
        btnRefresh.setEnabled(false);
        ApiClient.get("/api/invite", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                if (d == null) d = new JSONObject();
                InviteInfo info = InviteInfo.from(d);
                String link = "https://simpfun.cn/auth?type=register&code=" + info.inviteCode;
                requireActivity().runOnUiThread(() -> {
                    tvCode.setText(info.inviteCode.isEmpty() ? "-" : info.inviteCode);
                    tvTimes.setText(String.valueOf(info.registerTimes));
                    tvVerify.setText(String.valueOf(info.registerVerifyTimes));
                    tvIncome.setText(String.valueOf(info.totalIncome));
                    tvPro.setText(String.valueOf(info.proIncome));
                    tvLink.setText(link);
                    btnRefresh.setEnabled(true);
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show();
                    btnRefresh.setEnabled(true);
                });
            }
        });
    }

    private void copyLink() {
        String link = tvLink.getText().toString();
        if (link.isEmpty()) return;
        ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("invite_link", link));
            Toast.makeText(getContext(), R.string.copied, Toast.LENGTH_SHORT).show();
        }
    }
}
