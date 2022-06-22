package org.reactome.server.service.params;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstancesAttributesValuesData {
    private List<Long> dbIds;
    private List<String> attributeNames;
    private String className;
    private List<String> values;

    public InstancesAttributesValuesData(
            @JsonProperty("dbIds") List<Long> dbIds,
            @JsonProperty("className") String className,
            @JsonProperty("attributeNames") List<String> attributeNames,
            @JsonProperty("values") List<String> values) {
        this.dbIds = dbIds;
        this.className = className;
        this.attributeNames = attributeNames;
        this.values = values;
    }

    public List<Long> getDbIds() {
        return dbIds;
    }

    public void setDbIds(List<Long> dbIds) {
        this.dbIds = dbIds;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
