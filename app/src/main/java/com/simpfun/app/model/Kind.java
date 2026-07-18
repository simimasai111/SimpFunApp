package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 服务端：GET /api/games/kindlist?game_id=X -> list:[{id,name,description,pic_path,priority}] */
public class Kind {
    public int id;
    public String name = "";
    public String description = "";
    public String picPath = "";
    public int priority;

    public static Kind from(JSONObject o) {
        Kind k = new Kind();
        if (o == null) return k;
        k.id = Json.optInt(o, 0, "id");
        k.name = Json.optString(o, "name");
        k.description = Json.optString(o, "description");
        k.picPath = Json.optString(o, "pic_path");
        k.priority = Json.optInt(o, 0, "priority");
        return k;
    }

    public SelectItem toSelectItem() {
        return new SelectItem(String.valueOf(id), name, description, picPath);
    }
}
