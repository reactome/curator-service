package org.reactome.server.service.params;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstancesClassData {
    private List<Long> dbIds;
    private String className;

    public InstancesClassData(
            @JsonProperty("dbIds") List<Long> dbIds,
            @JsonProperty("className") String className) {
        this.dbIds = dbIds;
        this.className = className;
    }

    public List<Long> getDbIds() {
        return dbIds;
    }

    public void setDbIds(List<Long> dbIds) {
        this.dbIds = dbIds;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
