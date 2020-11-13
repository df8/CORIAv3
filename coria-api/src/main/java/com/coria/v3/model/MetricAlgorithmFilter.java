package com.coria.v3.model;

import com.coria.v3.metrics.MetricAlgorithmType;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type to filter a list of MetricAlgorithm entities based on the name and type.
 */
public class MetricAlgorithmFilter {
    String name;
    MetricAlgorithmType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricAlgorithmType getType() {
        return type;
    }

    public void setType(MetricAlgorithmType type) {
        this.type = type;
    }
}
