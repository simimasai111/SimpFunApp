package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 服务器实例：来自 GET /api/ins/list，顶层字段是 list */
public class Instance {
    public int id;
    public String name = "";
    public String cpu = "";
    public String ram = "";
    public String disk = "";
    public int state;
    public int versionId;
    public int area;
    public String createTime = "";
    public String lastPaidTime = "";
    public int pteroId;

    public static Instance from(JSONObject o) {
        Instance i = new Instance();
        if (o == null) return i;
        i.id = Json.optInt(o, 0, "id");
        i.name = Json.optString(o, "name");
        i.cpu = Json.optString(o, "cpu");
        i.ram = Json.optString(o, "ram");
        i.disk = Json.optString(o, "disk");
        i.state = Json.optInt(o, 0, "state");
        i.versionId = Json.optInt(o, 0, "version_id");
        i.area = Json.optInt(o, 0, "area");
        i.createTime = Json.optString(o, "create_time");
        i.lastPaidTime = Json.optString(o, "last_paid_time");
        i.pteroId = Json.optInt(o, 0, "ptero_id");
        return i;
    }

    public String displayName() {
        return (name == null || name.isEmpty()) ? ("服务器 #" + id) : name;
    }

    public String stateText() {
        switch (state) {
            case 0: return "离线";
            case 1: return "启动中";
            case 2: return "运行中";
            default: return "状态 " + state;
        }
    }

    public int stateColor() {
        switch (state) {
            case 0: return 0xFF9E9E9E; // 灰
            case 1: return 0xFFFFB300; // 琥珀
            case 2: return 0xFF4CAF50; // 绿
            default: return 0xFF9E9E9E;
        }
    }

    public String configSummary() {
        return cpu + "核 / " + ram + "G 内存 / " + disk + "G 磁盘";
    }
}
