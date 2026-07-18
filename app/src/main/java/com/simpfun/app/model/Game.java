package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 游戏类别：GET /api/games/list -> list:[{id,name,pic_path,priority}] */
public class Game {
    public int id;
    public String name = "";
    public String picPath = "";
    public int priority;

    public static Game from(JSONObject o) {
        Game g = new Game();
        if (o == null) return g;
        g.id = Json.optInt(o, 0, "id");
        g.name = Json.optString(o, "name");
        g.picPath = Json.optString(o, "pic_path");
        g.priority = Json.optInt(o, 0, "priority");
        return g;
    }

    public SelectItem toSelectItem() {
        return new SelectItem(String.valueOf(id), name, "", picPath);
    }
}
