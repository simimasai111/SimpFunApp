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
import com.simpfun.app.model.ShopItem;
import com.simpfun.app.model.SelectItem;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 创建实例向导 - 第 5 步：选择规格/套餐
 * API: GET /api/shop/list?version_id=X → {code:200, list:[{id=item_id, point, cpu, ram, disk, ...}]}
 */
public class StepSpecFragment extends Fragment {

    private RecyclerView rv;
    private ProgressBar pb;
    private SelectAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup vg, @Nullable Bundle si) {
        View root = inf.inflate(R.layout.fragment_step_spec, vg, false);
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
        if (adapter.getItemCount() == 0) loadSpecs();
    }

    private void loadSpecs() {
        CreateInstanceActivity act = (CreateInstanceActivity) requireActivity();
        if (act.selVersionId == null || act.selVersionId.isEmpty()) {
            Toast.makeText(getContext(), "请先选择版本", Toast.LENGTH_SHORT).show();
            return;
        }

        pb.setVisibility(View.VISIBLE);
        rv.setVisibility(View.GONE);

        ApiClient.getShopList(act.selVersionId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONArray arr = resp.optJSONArray("list");
                if (arr == null) arr = resp.optJSONArray("data");
                java.util.List<SelectItem> items = new java.util.ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            ShopItem s = ShopItem.from(arr.getJSONObject(i));
                            if (!s.creatable) continue; // 跳过不可创建的
                            // 标题: 规格名 + 配置摘要
                            String title = s.displayTitle();
                            String sub = s.priceText() + " | " + s.areaText();
                            items.add(new SelectItem(String.valueOf(s.id), title, sub));
                        } catch (Exception ignore) {}
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapter.setData(items);
                    CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
                    adapter.setSelectedId(a.selSpecId);
                    pb.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    if (items.isEmpty()) showEmptyDialog(resp.toString());
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载规格失败: " + e, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /** 保存选中的规格 ID (= item_id，提交创建时使用) */
    public void saveSelection() {
        SelectItem sel = adapter.getSelectedItem();
        if (sel != null) {
            ((CreateInstanceActivity) requireActivity()).selSpecId = sel.id;
        }
    }

    private void showEmptyDialog(String raw) {
        new android.app.AlertDialog.Builder(requireContext())
                .title("该版本暂无可选规格")
                .setMessage("原始响应:\n" + (raw.length() > 500 ? raw.substring(0, 500) + "..." : raw))
                .setPositiveButton("确定", null)
                .show();
    }
}
