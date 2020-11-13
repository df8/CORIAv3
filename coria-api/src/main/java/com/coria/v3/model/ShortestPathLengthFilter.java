package com.coria.v3.model;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type to filter ShortestPathLength entities based on the dataset ID and metricAlgorithmImplementationId.
 */
@SuppressWarnings("unused")
public class ShortestPathLengthFilter {
    UUID datasetId;
    String metricAlgorithmImplementationId;

    public UUID getDatasetId() {
        return datasetId;
    }

    @SuppressWarnings("unused")
    public void setDatasetId(UUID datasetId) {
        this.datasetId = datasetId;
    }

    public String getMetricAlgorithmImplementationId() {
        return metricAlgorithmImplementationId;
    }

    @SuppressWarnings("unused")
    public void setMetricAlgorithmImplementationId(String metricAlgorithmImplementationId) {
        this.metricAlgorithmImplementationId = metricAlgorithmImplementationId;
    }

    @Override
    public String toString() {
        return "ShortestPathFilter{" +
                "datasetId=" + datasetId +
                ", metricAlgorithmImplementationId=" + metricAlgorithmImplementationId +
                '}';
    }
}
