package com.simpfun.app.util;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * JSON 取值工具：按多候选 key 顺序取，兼容简幻欢接口字段命名不统一的情况。
 */
public final class Json {
    private Json() {
    }

    public static String pick(JSONObject o, String... keys) {
        if (o == null) return "";
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.getString(k);
                } catch (Exception ignore) {
                }
            }
        }
        return "";
    }

    public static int pickInt(JSONObject o, int def, String... keys) {
        if (o == null) return def;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.getInt(k);
                } catch (Exception ignore) {
                }
            }
        }
        return def;
    }

    public static long pickLong(JSONObject o, long def, String... keys) {
        if (o == null) return def;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.getLong(k);
                } catch (Exception ignore) {
                }
            }
        }
        return def;
    }

    public static boolean pickBool(JSONObject o, String... keys) {
        if (o == null) return false;
        for (String k : keys) {
            if (o.has(k) && !o.isNull(k)) {
                try {
                    return o.getBoolean(k);
                } catch (Exception ignore) {
                }
                try {
                    return o.getInt(k) != 0;
                } catch (Exception ignore) {
                }
            }
        }
        return false;
    }

    /** 从 data 中按常见 key 取出 JSONArray（兼容 data 直接是数组或包在对象里） */
    public static JSONArray toArray(Object d) {
        if (d instanceof JSONArray) return (JSONArray) d;
        if (d instanceof JSONObject) {
            JSONObject o = (JSONObject) d;
            for (String k : new String[]{"list", "data", "items", "rows", "files", "backups", "games", "shops", "result", "content"}) {
                if (o.optJSONArray(k) != null) return o.optJSONArray(k);
            }
        }
        return null;
    }
}
