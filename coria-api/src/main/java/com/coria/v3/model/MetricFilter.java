package com.coria.v3.model;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type to filter a list of Metric entities based on the ID of the related dataset.
 */
public class MetricFilter {
    UUID datasetId;

    public UUID getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(UUID datasetId) {
        this.datasetId = datasetId;
    }
}
