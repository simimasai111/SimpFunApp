package com.simpfun.app.model;

/** 通用列表选项：用于创建实例向导中的「游戏 / 规格」等单选列表 */
public class SelectItem {
    public final String id;
    public final String title;
    public final String sub;

    public SelectItem(String id, String title, String sub) {
        this.id = id == null ? "" : id;
        this.title = title == null ? "" : title;
        this.sub = sub == null ? "" : sub;
    }

    @Override
    public String toString() {
        return title;
    }
}
