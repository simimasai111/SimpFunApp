package com.simpfun.app.model;

import org.json.JSONObject;

import com.simpfun.app.util.Json;

/**
 * 服务器实例模型。
 * 由于逆向阶段未实际登录，字段名按 Pterodactyl 风格 + 简幻欢常见命名做多候选兼容，
 * 接口返回的真实字段若不同，可在 from() 中补充候选 key。
 */
public class Instance {
    public String id = "";
    public String name = "";
    public String status = "";
    public String version = "";
    public String image = "";
    public String memory = "";
    public String port = "";
    public JSONObject raw;

    public static Instance from(JSONObject o) {
        Instance i = new Instance();
        i.raw = o;
        i.id = Json.pick(o, "instance_uuid", "uuid", "ins_id", "id", "server_id", "instance_id");
        i.name = Json.pick(o, "friendly_name", "name", "instance_name", "server_name", "nickname", "display_name");
        i.status = Json.pick(o, "state", "status", "running", "container_state");
        i.version = Json.pick(o, "version_name", "version", "image", "game_name", "game");
        i.image = Json.pick(o, "image", "image_name", "docker_image", "template");
        i.memory = Json.pick(o, "memory", "ram", "memory_limit", "max_memory");
        i.port = Json.pick(o, "port", "ports", "game_port", "server_port");
        return i;
    }

    public String displayName() {
        return name.isEmpty() ? (id.isEmpty() ? "(未命名实例)" : id) : name;
    }
}
