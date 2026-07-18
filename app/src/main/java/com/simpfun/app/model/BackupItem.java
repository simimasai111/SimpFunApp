package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 备份：GET /api/ins/{id}/backup -> list:[{id,status,size,valid_time,tag,is_windows}] */
public class BackupItem {
    public int id;
    public int status;
    public long size;
    public String validTime = "";
    public String tag = "";
    public boolean isWindows;

    public static BackupItem from(JSONObject o) {
        BackupItem b = new BackupItem();
        if (o == null) return b;
        b.id = Json.optInt(o, 0, "id");
        b.status = Json.optInt(o, 0, "status");
        b.size = Json.optLong(o, 0, "size");
        b.validTime = Json.optString(o, "valid_time");
        b.tag = Json.optString(o, "tag");
        b.isWindows = Json.optBool(o, "is_windows");
        return b;
    }

    public String sizeText() {
        if (size < 1024L * 1024L) return String.format("%.1f MB", size / 1024f / 1024f);
        return String.format("%.2f GB", size / 1024f / 1024f / 1024f);
    }
}
