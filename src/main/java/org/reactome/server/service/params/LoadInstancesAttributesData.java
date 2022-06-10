package org.reactome.server.service.params;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoadInstancesAttributesData {
    private List<Long> dbIds;
    private List<List> classAttributeNames;
    private Boolean recursive;

    public LoadInstancesAttributesData(
            @JsonProperty("dbIds") List<Long> dbIds,
            @JsonProperty("classAttributeNames") List<List> classAttributeNames,
            @JsonProperty("recursive") Boolean recursive) {
        this.dbIds = dbIds;
        this.classAttributeNames = classAttributeNames;
        this.recursive = recursive;
    }

    public List<Long> getDbIds() {
        return dbIds;
    }

    public void setDbIds(List<Long> dbIds) {
        this.dbIds = dbIds;
    }

    public List<List> getClassAttributeNames() {
        return classAttributeNames;
    }

    public void setClassAttributeNames(List<List> classAttributeNames) {
        this.classAttributeNames = classAttributeNames;
    }

    public Boolean getRecursive() {
        return recursive;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }
}
