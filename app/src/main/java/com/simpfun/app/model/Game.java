package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/** 游戏 / 版本模型（创建实例时选择） */
public class Game {
    public String id = "";
    public String name = "";
    public String kind = "";      // 分类（如 Java 版 / 基岩版）
    public String version = "";   // 版本号
    public JSONObject raw;

    public static Game from(JSONObject o) {
        Game g = new Game();
        g.raw = o;
        g.id = Json.pick(o, "game_id", "id", "gid", "type", "key");
        g.name = Json.pick(o, "game_name", "name", "display_name", "title");
        g.kind = Json.pick(o, "kind", "category", "type", "game_type");
        g.version = Json.pick(o, "version", "version_name", "ver");
        return g;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!name.isEmpty()) sb.append(name);
        if (!version.isEmpty()) sb.append(" ").append(version);
        if (name.isEmpty() && version.isEmpty()) sb.append(id);
        return sb.toString();
    }
}
