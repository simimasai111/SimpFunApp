package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RecyclerView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.simpfun.app.R;
import com.simpfun.app.CreateInstanceActivity;
import com.simpfun.app.adapter.SelectAdapter;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.Game;
import com.simpfun.app.model.SelectItem;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 创建实例向导 - 第 2 步：选择游戏类别
 * API: GET /api/games/list → {code:200, list:[{id, name, pic_path, priority}]}
 */
public class StepGameFragment extends Fragment {

    private RecyclerView rv;
    private ProgressBar pb;
    private SelectAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup vg, @Nullable Bundle si) {
        View root = inf.inflate(R.layout.fragment_step_game, vg, false);
        rv = root.findViewById(R.id.rv_step_items);
        pb = root.findViewById(R.id.pb_step);
        adapter = new SelectAdapter();
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter.getItemCount() == 0) loadGames();
    }

    private void loadGames() {
        pb.setVisibility(View.VISIBLE);
        rv.setVisibility(View.GONE);
        ApiClient.getGames(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                // ⭐ 真实响应: list 数组
                JSONArray arr = resp.optJSONArray("list");
                if (arr == null) arr = resp.optJSONArray("data");
                java.util.List<SelectItem> items = new java.util.ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            Game g = Game.from(arr.getJSONObject(i));
                            items.add(new SelectItem(String.valueOf(g.id), g.name, g.picPath));
                        } catch (Exception ignore) {}
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapter.setData(items);
                    // 恢复选中状态
                    CreateInstanceActivity act = (CreateInstanceActivity) requireActivity();
                    adapter.setSelectedId(act.selGameId);
                    pb.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    if (items.isEmpty()) {
                        showEmptyDialog(resp.toString());
                    }
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载游戏列表失败: " + e, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /** 保存选中的游戏 ID */
    public void saveSelection() {
        SelectItem sel = adapter.getSelectedItem();
        if (sel != null) {
            ((CreateInstanceActivity) requireActivity()).selGameId = sel.id;
        }
    }

    private void showEmptyDialog(String raw) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("游戏列表为空")
                .setMessage("原始响应:\n" + (raw.length() > 500 ? raw.substring(0, 500) + "..." : raw))
                .setPositiveButton("确定", null)
                .show();
    }
}
