package com.simpfun.app.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 健壮的 JSON 取值工具：多 key 兜底、空安全、类型安全。
 * 字段名严格对应实测 API（见 simpfun_real_apis.md）。
 */
public class Json {

    public static String optString(JSONObject o, String... keys) {
        if (o == null) return "";
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) return o.optString(k, "");
        }
        return "";
    }

    public static int optInt(JSONObject o, int def, String... keys) {
        if (o == null) return def;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.optInt(k);
                } catch (Exception ignored) {
                }
            }
        }
        return def;
    }

    public static long optLong(JSONObject o, long def, String... keys) {
        if (o == null) return def;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.optLong(k);
                } catch (Exception ignored) {
                }
            }
        }
        return def;
    }

    public static boolean optBool(JSONObject o, String... keys) {
        if (o == null) return false;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.optBoolean(k, false);
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    public static JSONObject optObject(JSONObject o, String... keys) {
        if (o == null) return null;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                Object v = o.opt(k);
                if (v instanceof JSONObject) return (JSONObject) v;
            }
        }
        return null;
    }

    public static JSONArray toArray(Object v) {
        if (v instanceof JSONArray) return (JSONArray) v;
        return null;
    }

    /** 在对象里尝试多个 key，取第一个 JSONArray */
    public static JSONArray optArray(JSONObject o, String... keys) {
        if (o == null) return null;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                Object v = o.opt(k);
                if (v instanceof JSONArray) return (JSONArray) v;
            }
        }
        return null;
    }
}
