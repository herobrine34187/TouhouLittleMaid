package com.github.tartaricacid.touhoulittlemaid.ai.manager.setting.bean;

import java.util.List;

@SuppressWarnings("all")
public class MetaData {
    private int version = 0;
    private String author = "";
    private List<String> modelId = List.of();
    private String lanuage = "zh_cn";

    public int getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public List<String> getModelId() {
        return modelId;
    }

    public String getLanuage() {
        return lanuage;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 这里的字段名没有遵循 java 规范，因为这个类是用来和 snake yaml 进行序列化的
     * <p>
     * snake yaml 库不支持别名功能……很无语
     */
    public void setModel_id(List<String> modelId) {
        this.modelId = modelId;
    }

    public void setLanuage(String lanuage) {
        this.lanuage = lanuage;
    }
}
