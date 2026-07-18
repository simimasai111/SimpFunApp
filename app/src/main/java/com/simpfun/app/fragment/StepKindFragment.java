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
import com.simpfun.app.model.Kind;
import com.simpfun.app.model.SelectItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StepKindFragment extends Fragment {
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
            ((CreateInstanceActivity) requireActivity()).setKind(Integer.parseInt(i.id));
            ((CreateInstanceActivity) requireActivity()).nextStep();
        });
        rv.setAdapter(adapter);

        int gameId = getArguments() != null ? getArguments().getInt("game_id", -1) : -1;
        if (gameId < 0) gameId = ((CreateInstanceActivity) requireActivity()).getGameId();
        if (gameId < 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("请先返回选择游戏");
            return root;
        }
        load(gameId);
        return root;
    }

    private void load(int gameId) {
        pb.setVisibility(View.VISIBLE);
        Api.get().kindList(gameId, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list");
                items.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        Kind k = Kind.from(arr.optJSONObject(i));
                        if (k.id > 0) items.add(k.toSelectItem());
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    tvEmpty.setText("该游戏暂无服务端");
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
