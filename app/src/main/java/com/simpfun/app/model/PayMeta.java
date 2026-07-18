package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONArray;
import org.json.JSONObject;

/** 充值套餐：GET /api/pay/web/meta -> data:{user, point_price_tiers:[{point,public_recharge_money,pro_recharge_money}]} */
public class PayMeta {
    public int point;                 // 当前积分
    public boolean isPro;
    public int[] tierPoints = new int[0];
    public String[] tierPublic = new String[0];
    public String[] tierPro = new String[0];

    public static PayMeta from(JSONObject o) {
        PayMeta m = new PayMeta();
        if (o == null) return m;
        JSONObject user = Json.optObject(o, "user");
        if (user != null) {
            m.point = Json.optInt(user, 0, "point");
            m.isPro = Json.optBool(user, "is_pro");
        }
        JSONArray tiers = Json.optArray(o, "point_price_tiers");
        if (tiers != null) {
            int n = tiers.length();
            m.tierPoints = new int[n];
            m.tierPublic = new String[n];
            m.tierPro = new String[n];
            for (int i = 0; i < n; i++) {
                JSONObject t = tiers.optJSONObject(i);
                if (t == null) continue;
                m.tierPoints[i] = Json.optInt(t, 0, "point");
                m.tierPublic[i] = Json.optString(t, "public_recharge_money");
                m.tierPro[i] = Json.optString(t, "pro_recharge_money");
            }
        }
        return m;
    }
}
