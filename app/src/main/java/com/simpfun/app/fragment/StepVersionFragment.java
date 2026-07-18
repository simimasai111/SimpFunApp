package com.simpfun.app.fragment;

import com.simpfun.app.CreateInstanceActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simpfun.app.R;
import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.Game;
import com.simpfun.app.model.SelectItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 步骤 3：选择服务端 + 版本（联动）
 * - 先选游戏(Step2)→ 加载 kindlist(?game_id) 显示服务端
 * - 再选服务端→ 加载 versionlist(?kind_id) 显示版本
 *
 * 真实 API:
 *   GET /api/games/kindlist?game_id=X → {code:200, list:[{id, name, description, ...}]}
 *   GET /api/games/versionlist?kind_id=X → {code:200, list:[{id, name, description, ...}], is_windows}
 */
public class StepVersionFragment extends Fragment {
    private Spinner spKind;
    private Spinner spVer;
    private ProgressBar pb;
    private LinearLayout llForm;
    private ArrayAdapter<SelectItem> kindAdapter;
    private ArrayAdapter<SelectItem> verAdapter;
    private final List<SelectItem> kinds = new ArrayList<>();
    private final List<SelectItem> vers = new ArrayList<>();
    private boolean kindUserPicking = false;
    private boolean verUserPicking = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_step_version, c, false);
        spKind = v.findViewById(R.id.sp_kind);
        spVer = v.findViewById(R.id.sp_ver);
        pb = v.findViewById(R.id.pb);
        llForm = v.findViewById(R.id.ll_form);

        kindAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, kinds);
        kindAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spKind.setAdapter(kindAdapter);

        verAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, vers);
        verAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVer.setAdapter(verAdapter);

        spKind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!kindUserPicking) return;
                SelectItem k = kinds.get(position);
                CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
                a.selKindId = k.id;
                a.selKindName = k.title;
                a.selVersionId = null;
                a.selVersionName = null;
                a.selSpecId = null; // 清空下游选择
                loadVersions(k.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spVer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!verUserPicking) return;
                SelectItem ver = vers.get(position);
                CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
                a.selVersionId = ver.id;
                a.selVersionName = ver.title;
                a.selSpecId = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 如果已有游戏 ID，加载服务端列表；否则提示先选游戏
        CreateInstanceActivity act = (CreateInstanceActivity) requireActivity();
        if (act.selGameId != null && !act.selGameId.isEmpty()) {
            loadKinds();
        } else {
            pb.setVisibility(View.GONE);
            llForm.setVisibility(View.VISIBLE);
        }
        return v;
    }

    /** 加载服务端/类别列表 (kindlist) */
    private void loadKinds() {
        pb.setVisibility(View.VISIBLE);
        llForm.setVisibility(View.GONE);
        CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
        ApiClient.getGameKinds(a.selGameId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                // ⭐ 真实响应: list 数组，每项 {id, name, description, pic_path}
                JSONArray arr = resp.optJSONArray("list");
                if (arr == null) arr = resp.optJSONArray("data"); // 兼容
                kinds.clear();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            Game g = Game.from(arr.getJSONObject(i));
                            String sub = g.description.isEmpty() ? "" : g.description;
                            kinds.add(new SelectItem(String.valueOf(g.id), g.name, sub));
                        } catch (Exception ignore) {}
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    kindAdapter.notifyDataSetChanged();
                    int sel = -1;
                    CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
                    for (int i = 0; i < kinds.size(); i++) {
                        if (kinds.get(i).id.equals(a.selKindId)) { sel = i; break; }
                    }
                    pb.setVisibility(View.GONE);
                    llForm.setVisibility(View.VISIBLE);
                    if (sel >= 0) {
                        kindUserPicking = false;
                        spKind.setSelection(sel);
                        loadVersions(a.selKindId);
                    }
                    kindUserPicking = true;
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "加载服务端失败: " + e, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /** 加载版本列表 (versionlist) */
    private void loadVersions(String kindId) {
        if (kindId == null || kindId.isEmpty()) return;
        verUserPicking = false;
        vers.clear();
        verAdapter.notifyDataSetChanged();
        ApiClient.getGameVersions(kindId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                // ⭐ 真实响应: list 数组，每项 {id, name, description, priority}
                JSONArray arr = resp.optJSONArray("list");
                if (arr == null) arr = resp.optJSONArray("data");
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            JSONObject o = arr.getJSONObject(i);
                            String id = o.optString("id", "");
                            String name = o.optString("name", "");
                            String desc = o.optString("description", "");
                            vers.add(new SelectItem(id, name, desc));
                        } catch (Exception ignore) {}
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    verAdapter.notifyDataSetChanged();
                    CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
                    int sel = -1;
                    for (int i = 0; i < vers.size(); i++) {
                        if (vers.get(i).id.equals(a.selVersionId)) { sel = i; break; }
                    }
                    if (sel >= 0) {
                        verUserPicking = false;
                        spVer.setSelection(sel);
                    }
                    verUserPicking = true;
                });
            }

            @Override
            public void onError(String e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "加载版本失败: " + e, Toast.LENGTH_SHORT).show());
            }
        });
        verUserPicking = true;
    }

    /** 暴露给 Activity: 保存当前选择 */
    public void saveSelection() {
        // Spinner 选择通过 onItemSelectedListener 已自动保存到 Activity 字段
    }
}
