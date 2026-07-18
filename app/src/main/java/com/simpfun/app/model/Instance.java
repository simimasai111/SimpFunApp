package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/**
 * 服务器实例模型 — 对齐 2026-07-18 真实 API 响应 /api/ins/list
 */
public class Instance {
    public int id = 0;
    public String name = "";
    public String cpu = "";
    public String ram = "";
    public String disk = "";
    public int state = 0;          // 0=离线
    public int pteroId = 0;
    public int versionId = 0;
    public int area = 0;
    public String createTime = "";
    public String lastPaidTime = "";
    public JSONObject raw;

    public static Instance from(JSONObject o) {
        Instance i = new Instance();
        i.raw = o;
        i.id = Json.pickInt(o, 0, "id");
        i.name = Json.optString(o, "name", "");
        i.cpu = Json.optString(o, "cpu", "");
        i.ram = Json.optString(o, "ram", "");
        i.disk = Json.optString(o, "disk", "");
        i.state = Json.pickInt(o, 0, "state");
        i.pteroId = Json.pickInt(o, 0, "ptero_id", "pterodactyl_id");
        i.versionId = Json.pickInt(o, 0, "version_id");
        i.area = Json.pickInt(o, 0, "area");
        i.createTime = Json.optString(o, "create_time", "");
        i.lastPaidTime = Json.optString(o, "last_paid_time", "last_paidtime");
        return i;
    }

    public String displayName() {
        return name.isEmpty() ? ("未命名实例 #" + id) : name;
    }

    /** 返回状态文字 */
    public String stateText() {
        switch (state) {
            case 0: return "离线";
            case 1: return "启动中";
            case 2: return "运行中";
            case 3: return "停止中";
            default: return "未知(" + state + ")";
        }
    }

    /** 返回配置摘要如 "CPU 4核 | 内存 16GB | 磁盘 16GB" */
    public String configSummary() {
        StringBuilder sb = new StringBuilder();
        if (!cpu.isEmpty()) sb.append("CPU ").append(cpu).append("核");
        if (!ram.isEmpty()) { if (sb.length() > 0) sb.append(" | "); sb.append("内存 ").append(ram).append("GB"); }
        if (!disk.isEmpty()) { if (sb.length() > 0) sb.append(" | "); sb.append("磁盘 ").append(disk).append("GB"); }
        return sb.toString();
    }
}
