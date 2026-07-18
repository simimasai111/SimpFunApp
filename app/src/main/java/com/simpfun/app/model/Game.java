package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/**
 * 游戏 / 类别 / 版本 通用选项模型
 * 对齐 /api/games/list, /api/games/kindlist, /api/games/versionlist
 */
public class Game {
    public int id = 0;
    public String name = "";
    public String picPath = "";       // 图片路径 (games/list, kindlist)
    public String description = "";   // 描述 (kindlist, versionlist)
    public int priority = 0;
    // versionlist 特有
    public boolean isWindows = false;

    public JSONObject raw;

    /** 从 games/list 或 kindlist 解析 */
    public static Game from(JSONObject o) {
        Game g = new Game();
        g.raw = o;
        g.id = Json.pickInt(o, 0, "id");
        g.name = Json.optString(o, "name", "");
        g.picPath = Json.optString(o, "pic_path", "");
        g.description = Json.optString(o, "description", "");
        g.priority = Json.pickInt(o, 0, "priority");
        return g;
    }

    @Override
    public String toString() {
        if (!name.isEmpty()) return name;
        return "#" + id;
    }
}
