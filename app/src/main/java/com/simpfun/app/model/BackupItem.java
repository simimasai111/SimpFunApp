package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/** 备份 / 回档项模型 */
public class BackupItem {
    public String id = "";
    public String name = "";
    public String time = "";
    public long size = 0;
    public JSONObject raw;

    public static BackupItem from(JSONObject o) {
        BackupItem b = new BackupItem();
        b.raw = o;
        b.id = Json.pick(o, "id", "backup_id", "bid", "uuid", "file_id");
        // 真实接口字段为 tag（备份标签），如「重装时系统生成备份」
        b.name = Json.pick(o, "tag", "backup_name", "name", "filename", "file_name", "title");
        b.time = Json.pick(o, "valid_time", "time", "create_time", "created", "date", "backup_time");
        b.size = Json.pickLong(o, 0, "size", "file_size", "bytes");
        return b;
    }

    @Override
    public String toString() {
        return name.isEmpty() ? id : name;
    }
}
