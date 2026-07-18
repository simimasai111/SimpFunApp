package com.simpfun.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.simpfun.app.api.ApiClient;
import com.simpfun.app.fragment.StepConfirmFragment;
import com.simpfun.app.fragment.StepGameFragment;
import com.simpfun.app.fragment.StepSpecFragment;
import com.simpfun.app.fragment.StepVersionFragment;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建实例（多步向导）：
 *   第1步 选择游戏 → 第2步 选择分类/版本 → 第3步 选择规格/价格 → 第4步 确认并提交
 * 提交字段：item_id（规格）+ version_id（版本）+ name（实例名，可选）+ custom（可选）。
 */
public class CreateInstanceActivity extends AppCompatActivity {
    public static final int REQ = 1001;

    // —— 跨步骤选择状态 ——
    public String selGameId, selGameName;
    public String selKindId, selKindName;
    public String selVersionId, selVersionName;
    public String selItemId, selItemName, selItemPrice;
    public String selName;

    private static final int TOTAL = 4;
    private int step = 0;
    private TextView[] dots;
    private Button btnPrev, btnNext;
    private boolean submitting = false;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create_instance);
        dots = new TextView[]{
                findViewById(R.id.dot0), findViewById(R.id.dot1),
                findViewById(R.id.dot2), findViewById(R.id.dot3)
        };
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        btnPrev.setOnClickListener(v -> {
            if (step > 0) showStep(step - 1);
        });
        btnNext.setOnClickListener(v -> onNext());

        showStep(0);
    }

    private void showStep(int idx) {
        step = idx;
        Fragment f;
        switch (idx) {
            case 0: f = new StepGameFragment(); break;
            case 1: f = new StepVersionFragment(); break;
            case 2: f = new StepSpecFragment(); break;
            default: f = new StepConfirmFragment(); break;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, f).commit();
        updateIndicator();
        updateNav();
    }

    private void updateIndicator() {
        for (int i = 0; i < TOTAL; i++) {
            dots[i].setBackgroundResource(i == step
                    ? R.drawable.bg_step_dot_active
                    : R.drawable.bg_step_dot_inactive);
        }
    }

    private void updateNav() {
        btnPrev.setVisibility(step == 0 ? View.GONE : View.VISIBLE);
        btnNext.setText(step == TOTAL - 1 ? "创建实例" : "下一步");
    }

    private void onNext() {
        if (!validateStep(step)) return;
        if (step == TOTAL - 1) {
            doCreate();
        } else {
            showStep(step + 1);
        }
    }

    private boolean validateStep(int s) {
        switch (s) {
            case 0:
                if (selGameId == null || selGameId.isEmpty()) {
                    toast("请选择一个游戏");
                    return false;
                }
                break;
            case 1:
                if (selVersionId == null || selVersionId.isEmpty()) {
                    toast("请选择分类与版本");
                    return false;
                }
                break;
            case 2:
                if (selItemId == null || selItemId.isEmpty()) {
                    toast("请选择一个规格");
                    return false;
                }
                break;
        }
        return true;
    }

    private void doCreate() {
        if (submitting) return;
        String name = selName == null ? "" : selName;
        if (name.isEmpty()) {
            // 允许无名称创建（服务端可能自动命名），但给出提示更友好
            toast("请填写实例名称");
            return;
        }
        submitting = true;
        btnNext.setEnabled(false);
        btnPrev.setEnabled(false);

        Map<String, String> p = new HashMap<>();
        p.put("item_id", selItemId);
        p.put("version_id", selVersionId);
        p.put("name", name);

        ApiClient.createInstance(p, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject r) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateInstanceActivity.this, "创建成功", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                });
            }

            @Override
            public void onError(String e) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateInstanceActivity.this, e, Toast.LENGTH_SHORT).show();
                    submitting = false;
                    btnNext.setEnabled(true);
                    btnPrev.setEnabled(true);
                });
            }
        });
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /** 拉到空数据时弹出完整原始响应，便于用户复制反馈以对齐字段 */
    public void showRawDialog(JSONObject resp) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("原始响应（请复制发我以对齐字段）");
        TextView tv = new TextView(this);
        tv.setText(resp == null ? "(null)" : resp.toString());
        tv.setTextIsSelectable(true);
        tv.setPadding(40, 30, 40, 30);
        tv.setTextSize(11);
        ScrollView sv = new ScrollView(this);
        sv.addView(tv);
        b.setView(sv);
        b.setPositiveButton(android.R.string.ok, null);
        b.show();
    }
}
