package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.util.PrefsHelper;

import org.json.JSONObject;

/**
 * 个人中心 / 用户信息页
 * 真实 API: GET /api/auth/info → {code:200, info:{id, username, point, diamond, verified, qq, ...}}
 */
public class ProfileFragment extends Fragment {

    private TextView tvUser, tvUid, tvPoints, tvDiamond, tvVerified, tvQq, tvRaw;
    private Button btnRefresh, btnLogout;
    private PrefsHelper prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        prefs = new PrefsHelper(requireContext());

        tvUser = root.findViewById(R.id.tv_user);
        tvUid = root.findViewById(R.id.tv_uid);
        tvPoints = root.findViewById(R.id.tv_points);
        tvDiamond = root.findViewById(R.id.tv_diamond);
        tvVerified = root.findViewById(R.id.tv_verified);
        tvQq = root.findViewById(R.id.tv_qq); // 可能没有这个 TextView，容错处理
        if (tvQq == null) tvQq = new TextView(requireContext());
        tvRaw = root.findViewById(R.id.tv_p_raw);

        btnRefresh = root.findViewById(R.id.btn_refresh_profile);
        btnLogout = root.findViewById(R.id.btn_logout);

        btnRefresh.setOnClickListener(v -> loadProfile());
        btnLogout.setOnClickListener(v -> {
            prefs.clearToken();
            Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        });

        // 先显示缓存
        tvUser.setText(prefs.getUsername());
        loadProfile();
        return root;
    }

    private void loadProfile() {
        btnRefresh.setEnabled(false);
        ApiClient.getUserInfo(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                // ⭐ 关键修复: 真实响应字段是 "info" 不是 "data"
                JSONObject info = resp.optJSONObject("info");
                if (info == null) {
                    // 兼容: 也尝试 data 字段
                    info = resp.optJSONObject("data");
                }
                if (info == null) info = new JSONObject();

                final String username = info.optString("username", "");
                final int uid = info.optInt("id", 0);
                final int points = info.optInt("point", 0);
                final int diamond = info.optInt("diamond", 0);
                final boolean verified = info.optBoolean("verified", false);
                final long qq = info.optLong("qq", 0L);

                requireActivity().runOnUiThread(() -> {
                    tvUser.setText(username.isEmpty() ? prefs.getUsername() : username);
                    tvUid.setText(uid > 0 ? String.valueOf(uid) : "-");
                    tvPoints.setText(String.valueOf(points));
                    tvDiamond.setText(String.valueOf(diamond));
                    tvVerified.setText(verified ? "已认证" : "未认证");
                    if (tvQq != null && qq > 0) tvQq.setText(String.valueOf(qq));
                    // 完整原始响应用于诊断
                    tvRaw.setText(resp.toString());
                    btnRefresh.setEnabled(true);
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), e, Toast.LENGTH_LONG).show();
                    tvRaw.setText("错误: " + e);
                    btnRefresh.setEnabled(true);
                });
            }
        });
    }
}
