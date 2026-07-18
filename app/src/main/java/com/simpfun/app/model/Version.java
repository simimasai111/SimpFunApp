package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 版本：GET /api/games/versionlist?kind_id=X -> list:[{id,name,description,priority}] + is_windows */
public class Version {
    public int id;
    public String name = "";
    public String description = "";
    public int priority;
    public boolean isWindows;

    public static Version from(JSONObject o) {
        Version v = new Version();
        if (o == null) return v;
        v.id = Json.optInt(o, 0, "id");
        v.name = Json.optString(o, "name");
        v.description = Json.optString(o, "description");
        v.priority = Json.optInt(o, 0, "priority");
        v.isWindows = Json.optBool(o, "is_windows");
        return v;
    }

    public SelectItem toSelectItem() {
        return new SelectItem(String.valueOf(id), name, description);
    }
}
