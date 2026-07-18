package com.simpfun.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simpfun.app.api.ApiClient;
import com.simpfun.app.model.ShopItem;
import com.simpfun.app.util.Json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商店：浏览商品并购买。购买字段名（id 等）按接口实际命名做了多候选。
 */
public class ShopActivity extends AppCompatActivity {
    private ListView list;
    private TextView tvEmpty;
    private final List<ShopItem> items = new ArrayList<>();
    private ArrayAdapter<ShopItem> adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_shop);
        list = findViewById(R.id.list_shop);
        tvEmpty = findViewById(R.id.tv_empty_shop);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        adapter = new ArrayAdapter<>(this, R.layout.item_shop, R.id.tv_shop_name, items);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this::onItemClick);
        load();
    }

    private void load() {
        ApiClient.shopList(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                List<ShopItem> parsed = new ArrayList<>();
                JSONArray arr = Json.toArray(resp.opt("data"));
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            parsed.add(ShopItem.from(arr.getJSONObject(i)));
                        } catch (Exception ignore) {
                        }
                    }
                }
                runOnUiThread(() -> {
                    items.clear();
                    items.addAll(parsed);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String e) {
                runOnUiThread(() -> {
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(ShopActivity.this, e, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        ShopItem item = items.get(pos);
        new AlertDialog.Builder(this)
                .setTitle(item.name)
                .setMessage((item.desc.isEmpty() ? "" : item.desc + "\n") +
                        "价格：" + item.price + (item.diamond > 0 ? "（含 " + item.diamond + "）" : ""))
                .setPositiveButton("购买", (d, w) -> buy(item))
                .setNegativeButton("取消", null)
                .show();
    }

    private void buy(ShopItem item) {
        Map<String, String> p = new HashMap<>();
        p.put("id", item.id);
        p.put("goods_id", item.id);
        p.put("product_id", item.id);
        ApiClient.shopBuy(p, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject r) {
                runOnUiThread(() -> Toast.makeText(ShopActivity.this, "购买请求已提交", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onError(String e) {
                runOnUiThread(() -> Toast.makeText(ShopActivity.this, e, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
