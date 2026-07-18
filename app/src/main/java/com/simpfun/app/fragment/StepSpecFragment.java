package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.CreateInstanceActivity;
import com.simpfun.app.R;
import com.simpfun.app.adapter.SelectAdapter;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.SelectItem;
import com.simpfun.app.model.ShopItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StepSpecFragment extends Fragment {
    private RecyclerView rv;
    private ProgressBar pb;
    private TextView tvEmpty;
    private SelectAdapter adapter;
    private final List<SelectItem> items = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_step, vg, false);
        rv = root.findViewById(R.id.rv_step);
        pb = root.findViewById(R.id.pb_step);
        tvEmpty = root.findViewById(R.id.tv_empty_step);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SelectAdapter(items, i -> {
            ((CreateInstanceActivity) requireActivity()).setItem(Integer.parseInt(i.id));
            ((CreateInstanceActivity) requireActivity()).nextStep();
        });
        rv.setAdapter(adapter);

        int versionId = getArguments() != null ? getArguments().getInt("version_id", -1) : -1;
        if (versionId < 0) versionId = ((CreateInstanceActivity) requireActivity()).getVersionId();
        if (versionId < 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("请先返回选择版本");
            return root;
        }
        load(versionId);
        return root;
    }

    private void load(int versionId) {
        pb.setVisibility(View.VISIBLE);
        Api.get().shopList(versionId, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list");
                items.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        ShopItem s = ShopItem.from(arr.optJSONObject(i));
                        if (s.id > 0 && s.creatable) items.add(s.toSelectItem());
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    tvEmpty.setText("该版本暂无可选规格");
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("加载失败：" + msg);
                });
            }
        });
    }
}
