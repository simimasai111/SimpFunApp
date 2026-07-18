package com.simpfun.app.model;

import com.simpfun.app.util.Json;
import org.json.JSONObject;

/** SFTP 连接信息：GET /api/ins/{id}/sftp -> {ip, port, username, password}（可能包在 data 里） */
public class SftpInfo {
    public String ip = "";
    public String port = "";
    public String username = "";
    public String password = "";

    public static SftpInfo from(JSONObject root) {
        SftpInfo s = new SftpInfo();
        if (root == null) return s;
        // 先试平铺，再试 data 包裹
        JSONObject o = Json.optObject(root, "data");
        if (o == null) o = root;
        s.ip = Json.optString(o, "ip", "host", "address");
        s.port = Json.optString(o, "port");
        s.username = Json.optString(o, "username", "user", "account");
        s.password = Json.optString(o, "password", "passwd", "pwd");
        return s;
    }
}
