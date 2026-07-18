package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/**
 * 实例详情：GET /api/ins/{id}/detail -> data:{...}
 * 字段严格按实测响应（simpfun_real_apis.md 第 10 条）。
 */
public class InstanceDetail {
    public int id;
    public String name = "";
    public String status = "";        // offline/starting/running/stopping
    public int state;
    public String gameName = "";
    public String kindName = "";
    public String versionName = "";
    public int cpu;
    public int ram;
    public int disk;
    public int point;
    public String areaGrade = "";
    public String ip = "";
    public String port = "";
    public String uuid = "";
    public long uptime;
    public double cpuAbsolute;
    public long memBytes;
    public long diskBytes;
    public long netRx;
    public long netTx;
    public String trafficPlan = "";
    public long trafficRemain;
    public boolean isPro;

    public static InstanceDetail from(JSONObject root) {
        InstanceDetail d = new InstanceDetail();
        if (root == null) return d;
        JSONObject o = Json.optObject(root, "data");
        if (o == null) o = root;
        d.id = Json.optInt(o, 0, "id");
        d.name = Json.optString(o, "name");
        d.status = Json.optString(o, "status");
        d.state = Json.optInt(o, 0, "state");
        d.cpu = Json.optInt(o, 0, "cpu");
        d.ram = Json.optInt(o, 0, "ram");
        d.disk = Json.optInt(o, 0, "disk");
        d.point = Json.optInt(o, 0, "point");
        d.areaGrade = Json.optString(o, "area_grade");
        d.uuid = Json.optString(o, "uuid");
        d.isPro = Json.optBool(o, "is_pro");

        JSONObject gi = Json.optObject(o, "game_info");
        if (gi != null) {
            d.gameName = Json.optString(gi, "game_name");
            d.kindName = Json.optString(gi, "kind_name");
            d.versionName = Json.optString(gi, "version_name");
        }
        JSONObject da = Json.optObject(o, "default_allocation");
        if (da != null) {
            d.ip = Json.optString(da, "ip");
            d.port = Json.optString(da, "port");
        }
        JSONObject u = Json.optObject(o, "utilization");
        if (u != null) {
            d.uptime = Json.optLong(u, 0, "uptime");
            d.cpuAbsolute = Json.optInt(u, 0, "cpu_absolute");
            d.memBytes = Json.optLong(u, 0, "memory_bytes");
            d.diskBytes = Json.optLong(u, 0, "disk_bytes");
            d.netRx = Json.optLong(u, 0, "network_rx_bytes");
            d.netTx = Json.optLong(u, 0, "network_tx_bytes");
        }
        JSONObject tr = Json.optObject(o, "traffic");
        if (tr != null) {
            d.trafficPlan = Json.optString(tr, "plan");
            d.trafficRemain = Json.optLong(tr, 0, "remain_bytes");
        }
        return d;
    }
}
