package com.simpfun.app.fragment;

import com.simpfun.app.CreateInstanceActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.simpfun.app.R;

/** 步骤 4：确认配置摘要并填写实例名称 */
public class StepConfirmFragment extends Fragment {
    private EditText etName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup c, @Nullable Bundle b) {
        View v = inflater.inflate(R.layout.fragment_step_confirm, c, false);
        CreateInstanceActivity a = (CreateInstanceActivity) requireActivity();

        ((TextView) v.findViewById(R.id.tv_game)).setText(nz(a.selGameName));
        ((TextView) v.findViewById(R.id.tv_ver)).setText(nz(a.selVersionName));
        ((TextView) v.findViewById(R.id.tv_spec)).setText(nz(a.selItemName));
        ((TextView) v.findViewById(R.id.tv_price)).setText(nz(a.selItemPrice));

        etName = v.findViewById(R.id.et_name);
        if (a.selName != null) etName.setText(a.selName);
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a2) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ((CreateInstanceActivity) requireActivity()).selName = s.toString().trim();
            }
        });
        return v;
    }

    public String getName() {
        return etName == null ? "" : etName.getText().toString().trim();
    }

    private String nz(String s) {
        return (s == null || s.isEmpty()) ? "—" : s;
    }
}
