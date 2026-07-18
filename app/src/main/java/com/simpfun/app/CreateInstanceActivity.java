package com.simpfun.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.Game;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 创建实例：选择游戏 / 版本，填写名称与内存后提交。
 * 提交字段名（name/game_id/version/memory 等）按接口实际命名做了多候选，如不符请在 doCreate 调整。
 */
public class CreateInstanceActivity extends AppCompatActivity {
    public static final int REQ = 1001;

    private Spinner spGame;
    private Spinner spVer;
    private EditText etName;
    private EditText etMem;
    private final List<Game> games = new ArrayList<>();
    private final List<Game> vers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_create_instance);
        spGame = findViewById(R.id.sp_game);
        spVer = findViewById(R.id.sp_ver);
        etName = findViewById(R.id.et_name);
        etMem = findViewById(R.id.et_mem);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_create).setOnClickListener(v -> doCreate());

        loadGames();
        loadVersions();
    }

    private void loadGames() {
        ApiClient.getGames(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                parseInto(resp, games);
                runOnUiThread(() -> {
                    ArrayAdapter<Game> a = new ArrayAdapter<>(CreateInstanceActivity.this,
                            android.R.layout.simple_spinner_item, games);
                    a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spGame.setAdapter(a);
                });
            }

            @Override
            public void onError(String e) {
                runOnUiThread(() -> Toast.makeText(CreateInstanceActivity.this, e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadVersions() {
        ApiClient.getGameVersions(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                parseInto(resp, vers);
                runOnUiThread(() -> {
                    ArrayAdapter<Game> a = new ArrayAdapter<>(CreateInstanceActivity.this,
                            android.R.layout.simple_spinner_item, vers);
                    a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spVer.setAdapter(a);
                });
            }

            @Override
            public void onError(String e) {
                runOnUiThread(() -> Toast.makeText(CreateInstanceActivity.this, e, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void parseInto(JSONObject resp, List<Game> target) {
        target.clear();
        JSONArray arr = Json.toArray(resp.opt("data"));
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                try {
                    target.add(Game.from(arr.getJSONObject(i)));
                } catch (Exception ignore) {
                }
            }
        }
    }

    private void doCreate() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "请填写实例名称", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> p = new HashMap<>();
        p.put("name", name);
        if (!games.isEmpty()) {
            Game g = games.get(spGame.getSelectedItemPosition());
            p.put("game_id", g.id);
            p.put("game", g.id);
        }
        if (!vers.isEmpty()) {
            Game v = vers.get(spVer.getSelectedItemPosition());
            p.put("version", v.version);
        }
        String mem = etMem.getText().toString().trim();
        if (!mem.isEmpty()) p.put("memory", mem);

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
                runOnUiThread(() -> Toast.makeText(CreateInstanceActivity.this, e, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
