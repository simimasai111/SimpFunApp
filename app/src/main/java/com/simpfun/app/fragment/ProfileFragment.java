package com.simpfun.app.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.simpfun.app.LoginActivity;
import com.simpfun.app.R;
import com.simpfun.app.RechargeActivity;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.Invite;
import com.simpfun.app.model.User;
import com.simpfun.app.util.Json;
import com.simpfun.app.util.Prefs;

import org.json.JSONObject;

public class ProfileFragment extends Fragment {
    private TextView tvUser, tvUid, tvPoint, tvDiamond, tvVerified, tvQq, tvInvite, tvRaw;
    private ProgressBar pb;
    private Prefs prefs;
    private long inviteCode;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_profile, vg, false);
        prefs = new Prefs(requireContext());

        tvUser = root.findViewById(R.id.tv_user);
        tvUid = root.findViewById(R.id.tv_uid);
        tvPoint = root.findViewById(R.id.tv_point);
        tvDiamond = root.findViewById(R.id.tv_diamond);
        tvVerified = root.findViewById(R.id.tv_verified);
        tvQq = root.findViewById(R.id.tv_qq);
        tvInvite = root.findViewById(R.id.tv_invite);
        tvRaw = root.findViewById(R.id.tv_raw);
        pb = root.findViewById(R.id.pb_profile);

        Button btnRecharge = root.findViewById(R.id.btn_recharge);
        Button btnLogout = root.findViewById(R.id.btn_logout);
        Button btnCopy = root.findViewById(R.id.btn_copy_invite);

        btnRecharge.setOnClickListener(v -> startActivity(new Intent(getActivity(), RechargeActivity.class)));
        btnLogout.setOnClickListener(v -> {
            prefs.clear();
            Api.get().setToken("");
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });
        btnCopy.setOnClickListener(v -> {
            if (inviteCode == 0) {
                Toast.makeText(getContext(), "邀请码暂未加载", Toast.LENGTH_SHORT).show();
                return;
            }
            ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("invite", String.valueOf(inviteCode)));
                Toast.makeText(getContext(), "邀请码已复制：" + inviteCode, Toast.LENGTH_SHORT).show();
            }
        });

        load();
        return root;
    }

    private void load() {
        pb.setVisibility(View.VISIBLE);
        Api.get().info(new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONObject info = Json.optObject(resp, "info", "data");
                User u = User.from(info);
                String raw = resp.toString();
                Api.get().invite(new Api.CB() {
                    @Override
                    public void ok(JSONObject r2) {
                        Invite inv = Invite.from(Json.optObject(r2, "data"));
                        requireActivity().runOnUiThread(() -> fill(u, inv, raw));
                    }

                    @Override
                    public void fail(String m) {
                        requireActivity().runOnUiThread(() -> fill(u, null, raw));
                    }
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载个人信息失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void fill(User u, Invite inv, String raw) {
        pb.setVisibility(View.GONE);
        tvUser.setText(u.username);
        tvUid.setText("UID: " + u.id);
        tvPoint.setText(String.valueOf(u.point));
        tvDiamond.setText(String.valueOf(u.diamond));
        tvVerified.setText(u.verified ? "已实名" : "未实名");
        tvQq.setText(u.qq > 0 ? String.valueOf(u.qq) : "未绑定");
        if (inv != null) {
            inviteCode = inv.inviteCode;
            tvInvite.setText(String.valueOf(inv.inviteCode));
        }
        tvRaw.setText(raw);
    }
}
