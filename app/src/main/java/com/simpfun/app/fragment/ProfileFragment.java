package com.simpfun.app.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.simpfun.app.LoginActivity;
import com.simpfun.app.R;
import com.simpfun.app.RechargeActivity;
import com.simpfun.app.ShopActivity;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.util.Prefs;

import org.json.JSONObject;

/**
 * 第三栏：个人信息
 * GET /api/auth/info 展示用户资料（字段名多候选兼容）；提供退出登录。
 */
public class ProfileFragment extends Fragment {
    private TextView tvUser, tvUid, tvPoints, tvDiamond, tvVerified, tvRaw;
    private Button btnRefresh, btnLogout;
    private Prefs prefs;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle s) {
        View root = inf.inflate(R.layout.fragment_profile, vg, false);
        prefs = new Prefs(requireContext());
        tvUser = root.findViewById(R.id.tv_p_user);
        tvUid = root.findViewById(R.id.tv_p_uid);
        tvPoints = root.findViewById(R.id.tv_p_points);
        tvDiamond = root.findViewById(R.id.tv_p_diamond);
        tvVerified = root.findViewById(R.id.tv_p_verified);
        tvRaw = root.findViewById(R.id.tv_p_raw);
        btnRefresh = root.findViewById(R.id.btn_p_refresh);
        btnLogout = root.findViewById(R.id.btn_p_logout);

        btnRefresh.setOnClickListener(v -> loadProfile());
        btnLogout.setOnClickListener(v -> logout());
        Button btnShop = root.findViewById(R.id.btn_p_shop);
        Button btnRecharge = root.findViewById(R.id.btn_p_recharge);
        if (btnShop != null) btnShop.setOnClickListener(v -> startActivity(new Intent(requireContext(), ShopActivity.class)));
        if (btnRecharge != null) btnRecharge.setOnClickListener(v -> startActivity(new Intent(requireContext(), RechargeActivity.class)));
        loadProfile();
        return root;
    }

    private void loadProfile() {
        btnRefresh.setEnabled(false);
        ApiClient.get("/api/auth/info", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                if (d == null) d = new JSONObject();
                final JSONObject fd = d;
                String user = pick(d, "username", "user", "nickname", "name");
                String uid = pick(d, "uid", "user_id", "id");
                int points = optInt(d, "points", "point", "integral");
                int diamond = optInt(d, "diamond", "diamonds");
                boolean verified = optBool(d, "verified", "is_verified", "realname");
                requireActivity().runOnUiThread(() -> {
                    tvUser.setText(user.isEmpty() ? prefs.getUsername() : user);
                    tvUid.setText(uid.isEmpty() ? "-" : uid);
                    tvPoints.setText(String.valueOf(points));
                    tvDiamond.setText(String.valueOf(diamond));
                    tvVerified.setText(verified ? "已认证" : "未认证");
                    // 显示完整响应，便于对齐全字段（若字段为空请把此内容发回）
                    tvRaw.setText(resp.toString());
                    btnRefresh.setEnabled(true);
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    tvRaw.setText(e);
                    Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show();
                    btnRefresh.setEnabled(true);
                });
            }
        });
    }

    private void logout() {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    prefs.clear();
                    ApiClient.setToken("");
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    requireActivity().finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static String pick(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.getString(k);
                } catch (Exception ignore) {
                }
            }
        }
        return "";
    }

    private static int optInt(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.getInt(k);
                } catch (Exception ignore) {
                }
            }
        }
        return 0;
    }

    private static boolean optBool(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.getBoolean(k);
                } catch (Exception ignore) {
                }
                try {
                    return o.getInt(k) == 1;
                } catch (Exception ignore) {
                }
            }
        }
        return false;
    }
}
