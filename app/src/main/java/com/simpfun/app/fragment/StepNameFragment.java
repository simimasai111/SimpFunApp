package com.simpfun.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.simpfun.app.CreateInstanceActivity;
import com.simpfun.app.R;

public class StepNameFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle b) {
        View root = inf.inflate(R.layout.fragment_step_name, vg, false);
        EditText etName = root.findViewById(R.id.et_name);
        TextView tvSummary = root.findViewById(R.id.tv_summary);
        Button btnConfirm = root.findViewById(R.id.btn_confirm);

        CreateInstanceActivity act = (CreateInstanceActivity) requireActivity();
        tvSummary.setText("规格 ID：" + act.getItemId() + "\n版本 ID：" + act.getVersionId());

        btnConfirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            act.submit(name);
        });
        return root;
    }
}
