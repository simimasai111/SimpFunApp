package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/** 商店商品模型 */
public class ShopItem {
    public String id = "";
    public String name = "";
    public String desc = "";
    public int price = 0;     // 价格（积分 / 元，按接口含义）
    public int diamond = 0;   // 对应钻石 / 数量
    public String type = "";
    public JSONObject raw;

    public static ShopItem from(JSONObject o) {
        ShopItem s = new ShopItem();
        s.raw = o;
        s.id = Json.pick(o, "id", "sid", "product_id", "goods_id", "item_id");
        s.name = Json.pick(o, "name", "title", "goods_name", "product_name");
        s.desc = Json.pick(o, "desc", "description", "info", "remark");
        s.price = Json.pickInt(o, 0, "price", "money", "cost", "amount", "points");
        s.diamond = Json.pickInt(o, 0, "diamond", "num", "count", "value", "quota");
        s.type = Json.pick(o, "type", "category", "kind", "tag");
        return s;
    }

    @Override
    public String toString() {
        return name.isEmpty() ? id : name;
    }
}
