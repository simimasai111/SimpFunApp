package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/**
 * 商店规格/套餐模型 — 对齐 /api/shop/list 真实响应
 * 每个 item 代表一个可购买的配置方案（item_id = 提交创建实例时用）
 */
public class ShopItem {
    public int id = 0;             // item_id — 创建实例提交时使用
    public int point = 0;          // 价格（积分）
    public int cpu = 0;
    public int ram = 0;            // 内存 GB
    public int disk = 0;           // 磁盘 GB
    public int traffic = 0;        // 流量 GB
    public String spec = "";       // 规格名: M, M+, L, L+, XL, 2XL, 3XL 等
    public String areaGrade = "";  // 区域等级: A, B-, B+, B++, C+, S, S+
    public String areaVendor = ""; // 区域厂商: I, A
    public boolean areaIsWindows = false;
    public boolean creatable = true;
    public String description = "";

    public JSONObject raw;

    public static ShopItem from(JSONObject o) {
        ShopItem s = new ShopItem();
        s.raw = o;
        s.id = Json.pickInt(o, 0, "id");
        s.point = Json.pickInt(o, 0, "point");
        s.cpu = Json.pickInt(o, 0, "cpu");
        s.ram = Json.pickInt(o, 0, "ram");
        s.disk = Json.pickInt(o, 0, "disk");
        s.traffic = Json.pickInt(o, 0, "traffic");
        s.spec = Json.optString(o, "spec", "");
        s.areaGrade = Json.optString(o, "area_grade", "");
        s.areaVendor = Json.optString(o, "area_vendor", "");
        s.areaIsWindows = Json.optBool(o, false, "area_is_windows");
        s.creatable = Json.optBool(o, true, "creatable");
        s.description = Json.optString(o, "description", "");
        return s;
    }

    /** 返回展示标题如 "M+ | CPU 4核 | 内存 12GB | 磁盘 16GB | 90积分" */
    public String displayTitle() {
        StringBuilder sb = new StringBuilder();
        if (!spec.isEmpty()) sb.append(spec);
        else if (id > 0) sb.append("#").append(id);
        sb.append(" | ").append(cpu).append("核 | ");
        sb.append(ram).append("GB内存 | ").append(disk).append("GB磁盘");
        if (traffic > 0) sb.append(" | ").append(traffic).append("GB流量");
        return sb.toString();
    }

    /** 返回价格文字 */
    public String priceText() {
        return point + " 积分";
    }

    /** 区域信息 */
    public String areaText() {
        if (!areaGrade.isEmpty()) return areaGrade + "区" + (areaIsWindows ? "(Win)" : "");
        return "";
    }

    @Override
    public String toString() {
        return spec.isEmpty() ? ("#" + id) : spec;
    }
}
