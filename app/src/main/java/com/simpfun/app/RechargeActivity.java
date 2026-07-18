package com.simpfun.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.simpfun.app.api.ApiClient;
import com.simpfun.app.util.Json;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 充值：输入金额后调用 /api/pay/web/create 获取支付链接，引导到浏览器完成支付。
 */
public class RechargeActivity extends AppCompatActivity {
    private EditText etMoney;
    private TextView tvInfo;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_recharge);
        etMoney = findViewById(R.id.et_money);
        tvInfo = findViewById(R.id.tv_recharge_info);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_pay).setOnClickListener(v -> doPay());
        loadInfo();
    }

    private void loadInfo() {
        ApiClient.recharge(new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                String info = d == null ? resp.toString() : d.toString();
                runOnUiThread(() -> tvInfo.setText(info));
            }

            @Override
            public void onError(String e) {
                runOnUiThread(() -> tvInfo.setText("加载充值方式失败：" + e));
            }
        });
    }

    private void doPay() {
        String money = etMoney.getText().toString().trim();
        if (money.isEmpty()) {
            Toast.makeText(this, "请输入充值金额", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> p = new HashMap<>();
        p.put("money", money);
        ApiClient.payWebCreate(p, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject resp) {
                JSONObject d = resp.optJSONObject("data");
                String url = d == null ? "" : Json.pick(d, "url", "pay_url", "link", "redirect", "qrcode");
                runOnUiThread(() -> {
                    if (!url.isEmpty()) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } else {
                        Toast.makeText(RechargeActivity.this,
                                "未获取到支付链接：" + resp.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String e) {
                runOnUiThread(() -> Toast.makeText(RechargeActivity.this, e, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
