package com.coria.v3.metrics;

import com.coria.v3.utility.Slugify;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Fradin, 2020
 */
@SuppressWarnings("unused")
public class MetricAlgorithmVariant {
    protected final String name;
    protected final String description;
    protected final List<MetricAlgorithmVariantParameter> parameters;
    protected final List<MetricAlgorithmVariant> dependencies;
    protected MetricAlgorithm metricAlgorithm;
    protected List<MetricAlgorithmImplementation> implementations;

    public MetricAlgorithmVariant(String name, String description) {
        this(name, description, null, null);
    }

    public MetricAlgorithmVariant(String name, String description, List<MetricAlgorithmVariant> dependencies) {
        this(name, description, dependencies, null);
    }

    public MetricAlgorithmVariant(String name, String description, List<MetricAlgorithmVariant> dependencies, List<MetricAlgorithmVariantParameter> parameters) {
        this.name = name;
        this.description = description;
        this.dependencies = dependencies;
        this.parameters = parameters;
        this.implementations = new ArrayList<>();
    }

    public String getId() {
        return this.getMetricAlgorithm().getId() + "--" + Slugify.toSlug(this.name);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        if (name.equals("Default") && description.length() == 0)
            return "Computes " + metricAlgorithm.getName() + " without applying any further post-processing.";
        return description;
    }

    public MetricAlgorithm getMetricAlgorithm() {
        return metricAlgorithm;
    }

    public void setMetricAlgorithm(MetricAlgorithm metricAlgorithm) {
        this.metricAlgorithm = metricAlgorithm;
    }

    public List<MetricAlgorithmVariant> getDependencies() {
        return dependencies;
    }

    public List<MetricAlgorithmImplementation> getImplementations() {
        return implementations;
    }

    public void setImplementations(List<MetricAlgorithmImplementation> implementations) {
        this.implementations = implementations;
    }

    public void addImplementation(MetricAlgorithmImplementation implementation) {
        if (this.implementations == null)
            this.implementations = new ArrayList<>();

        this.implementations.add(implementation);
        this.implementations.sort((mai1, mai2) -> mai2.getSpeedIndex() == mai1.getSpeedIndex() ? mai1.getId().compareTo(mai2.getId()) : mai2.getSpeedIndex() - mai1.getSpeedIndex());
    }

    public List<MetricAlgorithmVariantParameter> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "MetricAlgorithmVariant{" +
                "metricAlgorithm='" + metricAlgorithm.getName() + '\'' +
                ", name='" + name + '\'' +
                ", implementations=(" + implementations.size() + ")" +
                ", description='" + description + '\'' +
                '}';
    }
}
