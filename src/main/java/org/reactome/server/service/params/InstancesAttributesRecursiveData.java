package org.reactome.server.service.params;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstancesAttributesRecursiveData {
    private List<Long> dbIds;
    private List<List<String>> classAttributeNames;
    private Boolean recursive;

    public InstancesAttributesRecursiveData(
            @JsonProperty("dbIds") List<Long> dbIds,
            @JsonProperty("classAttributeNames") List<List<String>> attributeNames,
            @JsonProperty("recursive") Boolean recursive) {
        this.dbIds = dbIds;
        this.classAttributeNames = attributeNames;
        this.recursive = recursive;
    }

    public List<Long> getDbIds() {
        return dbIds;
    }

    public void setDbIds(List<Long> dbIds) {
        this.dbIds = dbIds;
    }

    public List<List<String>> getClassAttributeNames() {
        return classAttributeNames;
    }

    public void setClassAttributeNames(List<List<String>> classAttributeNames) {
        this.classAttributeNames = classAttributeNames;
    }

    public Boolean getRecursive() {
        return recursive;
    }

    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }
}
