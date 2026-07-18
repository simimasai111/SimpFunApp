package com.simpfun.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.simpfun.app.api.Api;
import com.simpfun.app.model.PayMeta;
import com.simpfun.app.util.Json;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 充值：GET /api/pay/web/meta 拉取档位与当前积分；
 * 点击档位 -> POST /api/pay/web/create {item,num,method} 获取支付链接，跳转浏览器完成支付。
 * 注意：/api/recharge 当前维护（403 通道调试中），因此只走 pay/web/create。
 */
public class RechargeActivity extends AppCompatActivity {
    private ProgressBar pb;
    private TextView tvPoint;
    private LinearLayout llTiers;
    private Spinner spMethod;
    private PayMeta meta;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_recharge);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("充值");
        tb.setNavigationOnClickListener(v -> finish());

        pb = findViewById(R.id.pb_recharge);
        tvPoint = findViewById(R.id.tv_point);
        llTiers = findViewById(R.id.ll_tiers);
        spMethod = findViewById(R.id.sp_method);

        List<String> methods = new ArrayList<>();
        methods.add("alipay");
        methods.add("wxpay");
        methods.add("qq");
        ArrayAdapter<String> ad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, methods);
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMethod.setAdapter(ad);

        loadMeta();
    }

    private void loadMeta() {
        pb.setVisibility(View.VISIBLE);
        Api.get().payMeta(new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    meta = PayMeta.from(Json.optObject(resp, "data"));
                    tvPoint.setText(String.valueOf(meta.point));
                    renderTiers();
                });
            }

            @Override
            public void fail(String msg) {
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(RechargeActivity.this, "加载充值档位失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void renderTiers() {
        llTiers.removeAllViews();
        if (meta == null || meta.tierPoints.length == 0) {
            TextView t = new TextView(this);
            t.setText("暂无可用充值档位");
            t.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            llTiers.addView(t);
            return;
        }
        for (int i = 0; i < meta.tierPoints.length; i++) {
            final int point = meta.tierPoints[i];
            String price = meta.isPro ? meta.tierPro[i] : meta.tierPublic[i];
            Button btn = new Button(this);
            btn.setText(point + " 积分  ·  ¥" + price);
            btn.setOnClickListener(v -> doPay(point));
            llTiers.addView(btn);
        }
    }

    private void doPay(final int point) {
        String method = (String) spMethod.getSelectedItem();
        if (method == null || method.isEmpty()) {
            Toast.makeText(this, "请选择付款方式", Toast.LENGTH_SHORT).show();
            return;
        }
        pb.setVisibility(View.VISIBLE);
        Api.get().payWebCreate(String.valueOf(point), 1, method, new Api.CB() {
            @Override
            public void ok(JSONObject resp) {
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    JSONObject d = Json.optObject(resp, "data");
                    String url = d == null ? "" : Json.optString(d, "url", "pay_url", "link", "redirect", "qrcode", "redirect_url");
                    if (!url.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else {
                        Toast.makeText(RechargeActivity.this,
                                "未获取到支付链接，请稍后在网页端完成支付", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void fail(String msg) {
                runOnUiThread(() -> {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(RechargeActivity.this, "下单失败：" + msg, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
