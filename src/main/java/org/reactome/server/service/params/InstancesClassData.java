package org.reactome.server.service.params;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstancesClassData {
    private List<Long> dbIds;
    private List<String> classNames;

    public InstancesClassData(
            @JsonProperty("dbIds") List<Long> dbIds,
            @JsonProperty("classNames") List<String> classNames) {
        this.dbIds = dbIds;
        this.classNames = classNames;
    }

    public List<Long> getDbIds() {
        return dbIds;
    }

    public void setDbIds(List<Long> dbIds) {
        this.dbIds = dbIds;
    }

    public List<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(List<String> classNames) {
        this.classNames = classNames;
    }
}
