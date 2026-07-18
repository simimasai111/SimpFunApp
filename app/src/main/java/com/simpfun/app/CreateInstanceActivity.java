package com.simpfun.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.simpfun.app.api.Api;
import com.simpfun.app.fragment.StepGameFragment;
import com.simpfun.app.fragment.StepKindFragment;
import com.simpfun.app.fragment.StepNameFragment;
import com.simpfun.app.fragment.StepSpecFragment;
import com.simpfun.app.fragment.StepVersionFragment;

import org.json.JSONObject;

public class CreateInstanceActivity extends AppCompatActivity {
    private int step = 0;
    private int gameId = -1, kindId = -1, versionId = -1, itemId = -1;
    private final String[] titles = {"选择游戏", "选择服务端", "选择版本", "选择规格", "命名并创建"};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create);
        MaterialToolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        int g = getIntent().getIntExtra("game_id", -1);
        int v = getIntent().getIntExtra("version_id", -1);
        int it = getIntent().getIntExtra("item_id", -1);
        if (g > 0 && v > 0 && it > 0) {
            gameId = g;
            versionId = v;
            itemId = it;
            step = 4;
        }
        showStep(step);
    }

    public void setGame(int id) { gameId = id; }
    public void setKind(int id) { kindId = id; }
    public void setVersion(int id) { versionId = id; }
    public void setItem(int id) { itemId = id; }
    public int getGameId() { return gameId; }
    public int getKindId() { return kindId; }
    public int getVersionId() { return versionId; }
    public int getItemId() { return itemId; }

    public void nextStep() {
        if (step < 4) showStep(step + 1);
    }

    public void prevStep() {
        if (step > 0) showStep(step - 1);
    }

    private void showStep(int s) {
        step = s;
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(titles[s]);
        Fragment f;
        switch (s) {
            case 0: f = new StepGameFragment(); break;
            case 1: f = new StepKindFragment(); break;
            case 2: f = new StepVersionFragment(); break;
            case 3: f = new StepSpecFragment(); break;
            default: f = new StepNameFragment(); break;
        }
        Bundle args = new Bundle();
        if (s >= 1) args.putInt("game_id", gameId);
        if (s >= 2) args.putInt("kind_id", kindId);
        if (s >= 3) args.putInt("version_id", versionId);
        f.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.step_container, f).commit();
    }

    public void submit(String name) {
        if (itemId < 0 || versionId < 0) {
            Toast.makeText(this, "信息不完整，请重新选择", Toast.LENGTH_LONG).show();
            showStep(0);
            return;
        }
        Api.get().createInstance(itemId, versionId, name, false, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                runOnUiThread(() -> {
                    Toast.makeText(CreateInstanceActivity.this, "创建成功，请在服务器列表查看", Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void fail(String msg) {
                runOnUiThread(() -> Toast.makeText(CreateInstanceActivity.this, "创建失败：" + msg, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (step > 0) prevStep();
        else super.onBackPressed();
    }
}
