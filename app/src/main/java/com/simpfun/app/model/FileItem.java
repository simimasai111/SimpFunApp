package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/** 文件管理项模型 */
public class FileItem {
    public String name = "";
    public boolean isDir = false;
    public long size = 0;
    public String mode = "";
    public String time = "";
    public JSONObject raw;

    public static FileItem from(JSONObject o) {
        FileItem f = new FileItem();
        f.raw = o;
        f.name = Json.pick(o, "name", "file_name", "filename", "title");
        // 真实接口字段为 file(bool, true=文件)，目录 = !file
        f.isDir = !Json.pickBool(o, "file", "is_file");
        f.size = Json.pickLong(o, 0, "size", "file_size", "bytes");
        f.mode = Json.pick(o, "mode", "permission", "chmod", "rights");
        f.time = Json.pick(o, "modified_at", "time", "modify_time", "mtime", "date", "updated_at");
        return f;
    }

    @Override
    public String toString() {
        return name.isEmpty() ? "/" : name;
    }
}
