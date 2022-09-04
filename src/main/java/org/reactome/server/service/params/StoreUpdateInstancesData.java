package org.reactome.server.service.params;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StoreUpdateInstancesData {
    private Long storeDbId;
    private Long updateDbId;
    private String storeClassName;
    private String updateClassName;
    private String updateAttributeName;

    public StoreUpdateInstancesData(
            @JsonProperty("storeDbId") Long storeDbId,
            @JsonProperty("storeClassName") String storeClassName,
            @JsonProperty("updateDbId") Long updateDbId,
            @JsonProperty("updateClassName") String updateClassName,
            @JsonProperty("updateAttributeName") String updateAttributeName
            ) {
        this.storeDbId = storeDbId;
        this.storeClassName = storeClassName;
        this.updateDbId = updateDbId;
        this.updateClassName = updateClassName;
        this.updateAttributeName = updateAttributeName;
    }

    public Long getStoreDbId() {
        return storeDbId;
    }

    public void setStoreDbId(Long storeDbId) {
        this.storeDbId = storeDbId;
    }

    public Long getUpdateDbId() {
        return updateDbId;
    }

    public void setUpdateDbId(Long updateDbId) {
        this.updateDbId = updateDbId;
    }

    public String getStoreClassName() {
        return storeClassName;
    }

    public void setStoreClassName(String storeClassName) {
        this.storeClassName = storeClassName;
    }

    public String getUpdateClassName() {
        return updateClassName;
    }

    public void setUpdateClassName(String updateClassName) {
        this.updateClassName = updateClassName;
    }

    public String getUpdateAttributeName() {
        return updateAttributeName;
    }

    public void setUpdateAttributeName(String updateAttributeName) {
        this.updateAttributeName = updateAttributeName;
    }
}
