package org.reactome.server.service.params;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoadInstancesAttributesData {
    private List<Long> dbIds;
    private List<String> attributeNames;

    public LoadInstancesAttributesData(
            @JsonProperty("dbIds") List<Long> dbIds,
            @JsonProperty("attributeNames") List attributeNames) {
        this.dbIds = dbIds;
        this.attributeNames = attributeNames;
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
}
