package com.simpfun.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.CreateInstanceActivity;
import com.simpfun.app.R;
import com.simpfun.app.adapter.ShopAdapter;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.Game;
import com.simpfun.app.model.ShopItem;
import com.simpfun.app.model.Version;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShopFragment extends Fragment {
    private Spinner spGame, spVersion;
    private RecyclerView rv;
    private ProgressBar pb;
    private TextView tvEmpty;
    private ShopAdapter adapter;
    private final List<ShopItem> specs = new ArrayList<>();
    private final List<Game> games = new ArrayList<>();
    private final List<Version> versions = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_shop, vg, false);
        spGame = root.findViewById(R.id.sp_game);
        spVersion = root.findViewById(R.id.sp_version);
        rv = root.findViewById(R.id.rv_shop);
        pb = root.findViewById(R.id.pb_shop);
        tvEmpty = root.findViewById(R.id.tv_empty_shop);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ShopAdapter(specs, item -> {
            Intent it = new Intent(getActivity(), CreateInstanceActivity.class);
            it.putExtra("game_id", games.isEmpty() ? -1 : games.get(spGame.getSelectedItemPosition()).id);
            it.putExtra("version_id", versions.isEmpty() ? -1 : versions.get(spVersion.getSelectedItemPosition()).id);
            it.putExtra("item_id", item.id);
            startActivity(it);
        });
        rv.setAdapter(adapter);

        spGame.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                onGamePicked(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });

        spVersion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                onVersionPicked(pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> p) {
            }
        });

        loadGames();
        return root;
    }

    private void loadGames() {
        pb.setVisibility(View.VISIBLE);
        Api.get().gamesList(new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list");
                games.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) games.add(Game.from(arr.optJSONObject(i)));
                }
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    ArrayAdapter<Game> ad = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, games);
                    ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spGame.setAdapter(ad);
                    if (!games.isEmpty()) onGamePicked(0);
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载游戏失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onGamePicked(int pos) {
        if (pos < 0 || pos >= games.size()) return;
        Game g = games.get(pos);
        pb.setVisibility(View.VISIBLE);
        Api.get().kindList(g.id, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list");
                int kindId = -1;
                if (arr != null && arr.length() > 0) {
                    JSONObject first = arr.optJSONObject(0);
                    if (first != null) kindId = Json.optInt(first, 0, "id");
                }
                if (kindId < 0) {
                    requireActivity().runOnUiThread(() -> {
                        pb.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "该游戏暂无可用的服务端", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                Api.get().versionList(kindId, new Api.CB() {
                    @Override
                    public void ok(JSONObject resp2) {
                        JSONArray va = Json.optArray(resp2, "list");
                        versions.clear();
                        if (va != null) {
                            for (int i = 0; i < va.length(); i++) versions.add(Version.from(va.optJSONObject(i)));
                        }
                        requireActivity().runOnUiThread(() -> {
                            ArrayAdapter<Version> ad = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, versions);
                            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spVersion.setAdapter(ad);
                            specs.clear();
                            adapter.notifyDataSetChanged();
                            if (!versions.isEmpty()) onVersionPicked(0);
                            else pb.setVisibility(View.GONE);
                        });
                    }

                    @Override
                    public void fail(String msg) {
                        requireActivity().runOnUiThread(() -> {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "加载版本失败：" + msg, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载服务端失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void onVersionPicked(int pos) {
        if (pos < 0 || pos >= versions.size()) return;
        Version v = versions.get(pos);
        pb.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        Api.get().shopList(v.id, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list");
                specs.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) specs.add(ShopItem.from(arr.optJSONObject(i)));
                }
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(specs.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "加载规格失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
