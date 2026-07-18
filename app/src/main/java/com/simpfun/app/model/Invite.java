package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 邀请信息：GET /api/invite -> data:{register_times, register_verify_times,
 *  register_total_income, register_total_income_from_pro, invite_code} */
public class Invite {
    public int registerTimes;
    public int registerVerifyTimes;
    public int registerTotalIncome;
    public int registerTotalIncomeFromPro;
    public long inviteCode;

    public static Invite from(JSONObject o) {
        Invite i = new Invite();
        if (o == null) return i;
        i.registerTimes = Json.optInt(o, 0, "register_times");
        i.registerVerifyTimes = Json.optInt(o, 0, "register_verify_times");
        i.registerTotalIncome = Json.optInt(o, 0, "register_total_income");
        i.registerTotalIncomeFromPro = Json.optInt(o, 0, "register_total_income_from_pro");
        i.inviteCode = Json.optLong(o, 0, "invite_code");
        return i;
    }
}
