package com.simpfun.app.model;

/** 通用选择项，用于创建实例向导各步骤（游戏/服务端/版本/规格） */
public class SelectItem {
    public final String id;
    public final String title;
    public final String subtitle; // 副标题：描述 / 价格 / 规格等
    public final String iconUrl;  // 可选图片（游戏/服务端 logo）

    public SelectItem(String id, String title, String subtitle) {
        this(id, title, subtitle, "");
    }

    public SelectItem(String id, String title, String subtitle, String iconUrl) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.iconUrl = iconUrl == null ? "" : iconUrl;
    }
}
