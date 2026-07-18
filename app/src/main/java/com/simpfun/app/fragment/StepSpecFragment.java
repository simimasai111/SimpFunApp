package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.R;
import com.simpfun.app.adapter.SelectAdapter;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.SelectItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** 步骤 3：选择规格 / 价格（按 version_id 筛选商店套餐） */
public class StepSpecFragment extends Fragment {
    private RecyclerView rv;
    private ProgressBar pb;
    private SelectAdapter adapter;
    private final List<SelectItem> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_step_spec, c, false);
        rv = v.findViewById(R.id.rv);
        pb = v.findViewById(R.id.pb);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SelectAdapter(item -> {
            CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
            a.selItemId = item.id;
            a.selItemName = item.title;
            a.selItemPrice = item.sub;
        });
        rv.setAdapter(adapter);
        load();
        return v;
    }

    private void load() {
        final CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
        if (a.selVersionId == null || a.selVersionId.isEmpty()) {
            pb.setVisibility(View.GONE);
            Toast.makeText(getContext(), "请先选择版本", Toast.LENGTH_SHORT).show();
            return;
        }
        pb.setVisibility(View.VISIBLE);
        rv.setVisibility(View.GONE);
        ApiClient.getShopList(a.selVersionId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                items.clear();
                JSONArray arr = Json.toArray(resp.opt("data"));
                if (arr == null) arr = Json.toArray(resp.opt("list"));
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            JSONObject o = arr.getJSONObject(i);
                            String id = Json.pick(o, "item_id", "id", "gid");
                            String name = Json.pick(o, "item_name", "name", "title",
                                    "goods_name", "spec_name", "plan_name");
                            String price = Json.pick(o, "price", "money", "cost", "amount", "fee");
                            items.add(new SelectItem(id, name, price == null ? "" : ("¥" + price)));
                        } catch (Exception ignore) {
                        }
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    adapter.setData(items);
                    adapter.setSelectedId(a.selItemId);
                    pb.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
                    if (items.isEmpty()) {
                        Toast.makeText(getContext(), "该版本暂无可选规格", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
