package com.taobao.weex.analyzer.pojo;

import java.util.List;
import java.util.Map;

public class NodeInfo {
    public String simpleName;
    public String realName;
    public int layer;
    public Map<String,Object> styles;
    public List<NodeInfo> children;

    @Override
    public String toString() {
        return "NodeInfo{" +
                "simpleName='" + simpleName + '\'' +
                ", realName='" + realName + '\'' +
                ", layer=" + layer +
                ", styles=" + styles +
                ", children=" + children +
                '}';
    }
}