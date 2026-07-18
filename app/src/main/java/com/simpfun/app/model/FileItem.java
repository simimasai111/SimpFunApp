package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** 文件项：GET /api/ins/{id}/file/list?path= -> list:[{name,file(bool),size,mime,modified_at}] */
public class FileItem {
    public String name = "";
    public boolean isFile;   // true=文件, false=目录
    public long size;
    public String mime = "";
    public String modifiedAt = "";

    public static FileItem from(JSONObject o) {
        FileItem f = new FileItem();
        if (o == null) return f;
        f.name = Json.optString(o, "name");
        f.isFile = Json.optBool(o, "file"); // file=true 表示是文件
        f.size = Json.optLong(o, 0, "size");
        f.mime = Json.optString(o, "mime");
        f.modifiedAt = Json.optString(o, "modified_at");
        return f;
    }

    public boolean isDir() {
        return !isFile;
    }

    public String sizeText() {
        if (isDir()) return "<目录>";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024f);
        return String.format("%.1f MB", size / 1024f / 1024f);
    }
}
