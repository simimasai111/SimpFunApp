package com.simpfun.app.fragment;

import com.simpfun.app.CreateInstanceActivity;

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

/** 步骤 1：选择游戏（基础镜像列表） */
public class StepGameFragment extends Fragment {
    private RecyclerView rv;
    private ProgressBar pb;
    private SelectAdapter adapter;
    private final List<SelectItem> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_step_game, c, false);
        rv = v.findViewById(R.id.rv);
        pb = v.findViewById(R.id.pb);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SelectAdapter(item -> {
            CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
            a.selGameId = item.id;
            a.selGameName = item.title;
            // 切换游戏时清空下游选择
            a.selKindId = null;
            a.selKindName = null;
            a.selVersionId = null;
            a.selVersionName = null;
            a.selItemId = null;
            a.selItemName = null;
            a.selItemPrice = null;
        });
        rv.setAdapter(adapter);
        load();
        return v;
    }

    private void load() {
        pb.setVisibility(View.VISIBLE);
        rv.setVisibility(View.GONE);
        ApiClient.getGames(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                parse(resp);
                requireActivity().runOnUiThread(() -> {
                    adapter.setData(items);
                    String sel = ((CreateInstanceActivity) requireActivity()).selGameId;
                    adapter.setSelectedId(sel);
                    pb.setVisibility(View.GONE);
                    rv.setVisibility(View.VISIBLE);
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

    private void parse(JSONObject resp) {
        items.clear();
        JSONArray arr = Json.toArray(resp.opt("data"));
        if (arr == null) return;
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject o = arr.getJSONObject(i);
                String id = Json.pick(o, "game_id", "id", "gid");
                String name = Json.pick(o, "game_name", "name", "display_name", "title");
                String kind = Json.pick(o, "kind", "category", "type", "game_type");
                items.add(new SelectItem(id, name, kind));
            } catch (Exception ignore) {
            }
        }
    }
}
