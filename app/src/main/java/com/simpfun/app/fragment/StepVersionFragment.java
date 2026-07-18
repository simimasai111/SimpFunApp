package com.simpfun.app.fragment;

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
import com.simpfun.app.model.SelectItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** 步骤 2：选择分类 + 版本 */
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
                a.selItemId = null;
                a.selItemName = null;
                a.selItemPrice = null;
                loadVersions(k.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spVer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!verUserPicking) return;
                SelectItem ver = vers.get(position);
                CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
                a.selVersionId = ver.id;
                a.selVersionName = ver.title;
                a.selItemId = null;
                a.selItemName = null;
                a.selItemPrice = null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        loadKinds();
        return v;
    }

    private void loadKinds() {
        pb.setVisibility(View.VISIBLE);
        llForm.setVisibility(View.GONE);
        final CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
        ApiClient.getGameKinds(a.selGameId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                kinds.clear();
                JSONArray arr = Json.toArray(resp.opt("data"));
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            JSONObject o = arr.getJSONObject(i);
                            String id = Json.pick(o, "kind_id", "id", "gid");
                            String name = Json.pick(o, "kind_name", "name", "title", "category");
                            kinds.add(new SelectItem(id, name, ""));
                        } catch (Exception ignore) {
                        }
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    kindAdapter.notifyDataSetChanged();
                    // 还原已选分类
                    int sel = -1;
                    for (int i = 0; i < kinds.size(); i++) {
                        if (kinds.get(i).id.equals(a.selKindId)) {
                            sel = i;
                            break;
                        }
                    }
                    pb.setVisibility(View.GONE);
                    llForm.setVisibility(View.VISIBLE);
                    if (sel >= 0) {
                        kindUserPicking = false;
                        spKind.setSelection(sel);
                        loadVersions(a.selKindId);
                    }
                    // 恢复完成后允许用户手动切换分类
                    kindUserPicking = true;
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

    private void loadVersions(String kindId) {
        if (kindId == null || kindId.isEmpty()) return;
        verUserPicking = false;
        vers.clear();
        verAdapter.notifyDataSetChanged();
        ApiClient.getGameVersions(kindId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONArray arr = Json.toArray(resp.opt("data"));
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            JSONObject o = arr.getJSONObject(i);
                            String id = Json.pick(o, "version_id", "id", "vid");
                            String name = Json.pick(o, "version_name", "name", "title", "version");
                            String sub = Json.pick(o, "version", "version_name");
                            vers.add(new SelectItem(id, name, sub));
                        } catch (Exception ignore) {
                        }
                    }
                }
                requireActivity().runOnUiThread(() -> {
                    verAdapter.notifyDataSetChanged();
                    CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();
                    int sel = -1;
                    for (int i = 0; i < vers.size(); i++) {
                        if (vers.get(i).id.equals(a.selVersionId)) {
                            sel = i;
                            break;
                        }
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
                        Toast.makeText(getContext(), e, Toast.LENGTH_SHORT).show());
            }
        });
        verUserPicking = true;
    }
}
