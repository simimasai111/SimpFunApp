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
        f.isDir = Json.pickBool(o, "is_dir", "directory", "dir", "isDir", "is_directory");
        f.size = Json.pickLong(o, 0, "size", "file_size", "bytes");
        f.mode = Json.pick(o, "mode", "permission", "chmod", "rights");
        f.time = Json.pick(o, "time", "modify_time", "mtime", "date", "updated_at");
        return f;
    }

    @Override
    public String toString() {
        return name.isEmpty() ? "/" : name;
    }
}
