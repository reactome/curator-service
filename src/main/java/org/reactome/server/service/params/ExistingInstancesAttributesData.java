package org.reactome.server.service.params;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ExistingInstancesAttributesData {
    private List<Long> dbIds;
    private Boolean checkCache;
    private Boolean inverse;

    public ExistingInstancesAttributesData(
            @JsonProperty("dbIds") List<Long> dbIds,
            @JsonProperty("checkCache") Boolean checkCache,
            @JsonProperty("inverse") Boolean inverse) {
        this.dbIds = dbIds;
        this.checkCache = checkCache;
        this.inverse = inverse;
    }

    public List<Long> getDbIds() {
        return dbIds;
    }

    public void setDbIds(List<Long> dbIds) {
        this.dbIds = dbIds;
    }

    public Boolean getCheckCache() {
        return checkCache;
    }

    public void setCheckCache(Boolean checkCache) {
        this.checkCache = checkCache;
    }

    public Boolean getInverse() {
        return inverse;
    }

    public void setInverse(Boolean inverse) {
        this.inverse = inverse;
    }
}
