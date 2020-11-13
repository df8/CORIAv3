package com.coria.v3.metrics;

/**
 * Created by David Fradin, 2020
 */
public class MetricAlgorithmVariantParameter {
    final int index;
    final String id;
    final String description;
    final String defaultValue;
    final boolean required;
    final MetricAlgorithmVariantParameterType type;

    public MetricAlgorithmVariantParameter(int index, String id, String description, MetricAlgorithmVariantParameterType type, String defaultValue, boolean required) {
        this.index = index;
        this.id = id;
        this.description = description;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public int getIndex() {
        return index;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public MetricAlgorithmVariantParameterType getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public enum MetricAlgorithmVariantParameterType {
        FLOAT, INT, STRING
    }
}
