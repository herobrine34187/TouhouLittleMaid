package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.bean;

public final class Setting {
    private MetaData meta = new MetaData();
    private String setting = "";

    public MetaData getMeta() {
        return meta;
    }

    public void setMeta(MetaData meta) {
        this.meta = meta;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }
}
