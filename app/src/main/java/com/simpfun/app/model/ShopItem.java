package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/**
 * 规格/商品：GET /api/shop/list?version_id=X
 * list:[{id(=item_id), point(价格), cpu, ram, disk, traffic, spec,
 *        area_grade, area_vendor, area_is_windows, creatable}]
 */
public class ShopItem {
    public int id;          // 即提交创建时的 item_id
    public int point;       // 价格（积分）
    public int cpu;
    public int ram;
    public int disk;
    public int traffic;
    public String spec = "";
    public String areaGrade = "";
    public String areaVendor = "";
    public boolean areaIsWindows;
    public boolean creatable;

    public static ShopItem from(JSONObject o) {
        ShopItem s = new ShopItem();
        if (o == null) return s;
        s.id = Json.optInt(o, 0, "id");
        s.point = Json.optInt(o, 0, "point");
        s.cpu = Json.optInt(o, 0, "cpu");
        s.ram = Json.optInt(o, 0, "ram");
        s.disk = Json.optInt(o, 0, "disk");
        s.traffic = Json.optInt(o, 0, "traffic");
        s.spec = Json.optString(o, "spec");
        s.areaGrade = Json.optString(o, "area_grade");
        s.areaVendor = Json.optString(o, "area_vendor");
        s.areaIsWindows = Json.optBool(o, "area_is_windows");
        s.creatable = Json.optBool(o, "creatable");
        return s;
    }

    public SelectItem toSelectItem() {
        String sub = point + " 积分  ·  " + cpu + "核/" + ram + "G/" + disk + "G";
        if (traffic > 0) sub += "  ·  流量 " + traffic + "G";
        if (!spec.isEmpty()) sub += "  ·  " + spec;
        return new SelectItem(String.valueOf(id), areaGrade + (areaVendor.isEmpty() ? "" : " · " + areaVendor), sub);
    }

    public String priceText() {
        return point + " 积分";
    }

    public String areaText() {
        return areaGrade + (areaVendor.isEmpty() ? "" : " · " + areaVendor);
    }
}
