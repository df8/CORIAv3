package com.coria.v3.model;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type to filter Node and Edge entities based on the name and dataset ID.
 */
public class NodeEdgeFilter {
    UUID datasetId;
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(UUID datasetId) {
        this.datasetId = datasetId;
    }
}
