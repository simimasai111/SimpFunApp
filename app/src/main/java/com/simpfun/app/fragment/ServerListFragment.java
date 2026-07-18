package com.simpfun.app.fragment;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.simpfun.app.CreateInstanceActivity;
import com.simpfun.app.InstanceDetailActivity;
import com.simpfun.app.R;
import com.simpfun.app.adapter.ServerAdapter;
import com.simpfun.app.api.Api;
import com.simpfun.app.model.Instance;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ServerListFragment extends Fragment {
    private RecyclerView rv;
    private ProgressBar pb;
    private TextView tvEmpty;
    private ServerAdapter adapter;
    private final List<Instance> list = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_server_list, vg, false);
        rv = root.findViewById(R.id.rv_servers);
        pb = root.findViewById(R.id.pb_servers);
        tvEmpty = root.findViewById(R.id.tv_empty_servers);
        FloatingActionButton fab = root.findViewById(R.id.fab_create);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ServerAdapter(list, i -> {
            Intent it = new Intent(getActivity(), InstanceDetailActivity.class);
            it.putExtra("ins_id", String.valueOf(i.id));
            it.putExtra("ins_name", i.displayName());
            startActivity(it);
        });
        rv.setAdapter(adapter);

        fab.setOnClickListener(v -> startActivity(new Intent(getActivity(), CreateInstanceActivity.class)));
        load();
        return root;
    }

    private void load() {
        pb.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        Api.get().insList(new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                JSONArray arr = Json.optArray(resp, "list", "data");
                list.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        list.add(Instance.from(arr.optJSONObject(i)));
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void fail(String msg) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "加载失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
