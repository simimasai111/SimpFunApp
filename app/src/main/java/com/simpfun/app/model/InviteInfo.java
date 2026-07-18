package com.simpfun.app.model;

import org.json.JSONObject;

/** 邀请信息模型（GET /api/invite 的 data 字段） */
public class InviteInfo {
    public String inviteCode = "";
    public int registerTimes = 0;
    public int registerVerifyTimes = 0;
    public int totalIncome = 0;
    public int proIncome = 0;

    public static InviteInfo from(JSONObject d) {
        InviteInfo i = new InviteInfo();
        if (d == null) return i;
        i.inviteCode = d.optString("invite_code", "");
        i.registerTimes = d.optInt("register_times", 0);
        i.registerVerifyTimes = d.optInt("register_verify_times", 0);
        i.totalIncome = d.optInt("register_total_income", 0);
        i.proIncome = d.optInt("register_total_income_from_pro", 0);
        return i;
    }
}
