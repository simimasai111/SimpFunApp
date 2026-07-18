package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 个人信息：来自 GET /api/auth/info，顶层字段是 info（不是 data） */
public class User {
    public int id;
    public String username = "";
    public int point;
    public int diamond;
    public boolean verified;
    public long qq;
    public boolean isPro;
    public String announcementTitle = "";
    public String announcementText = "";

    public static User from(JSONObject o) {
        User u = new User();
        if (o == null) return u;
        u.id = Json.optInt(o, 0, "id");
        u.username = Json.optString(o, "username");
        u.point = Json.optInt(o, 0, "point");
        u.diamond = Json.optInt(o, 0, "diamond");
        u.verified = Json.optBool(o, "verified");
        u.qq = Json.optLong(o, 0, "qq");
        u.isPro = Json.optBool(o, "is_pro", "pro_valid");
        JSONObject ann = Json.optObject(o, "announcement");
        if (ann != null) {
            u.announcementTitle = Json.optString(ann, "title");
            u.announcementText = Json.optString(ann, "text");
        }
        return u;
    }

    public String pointText() {
        return String.valueOf(point);
    }

    public String diamondText() {
        return String.valueOf(diamond);
    }
}
