package com.bigbasti.coria.metrics;

import java.util.Date;

/**
 * Created by Sebastian Gross
 */
public class MetricInfo {
    private String id;
    private String name;
    private String shortcut;
    private String provider;
    private String technology;
    private Date executionStarted;
    private Date executionFinished;
    private MetricStatus status;
    private String message;

    /**
     * defines what this moetric is met for:<br/>
     * DATASET: MetricInfo Value concerns the whole dataset and should be diaplayed on the dataset details page<br/>
     * NODE: MetricInfo is only relevant for single nodes<br/>
     * EDGE: MetricInfo is only relevant for single edges<br/>
     * dependent on type this metric could be shown or hidden in certain views
     */
    private MetricType type;
    /**
     * the value is not always present, only if the metric has a single outcome and concerns only a single entity eg. a dataset
     */
    private String value;

    public MetricInfo() {
        this.status = MetricStatus.RUNNING;
    }

    public MetricInfo(String id, String name, String shortcut, String provider, String technology, Date executionStarted, Date executionFinished) {
        this.status = MetricStatus.RUNNING;
        this.id = id;
        this.name = name;
        this.shortcut = shortcut;
        this.provider = provider;
        this.technology = technology;
        this.executionStarted = executionStarted;
        this.executionFinished = executionFinished;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortcut() {
        return shortcut;
    }

    public void setShortcut(String shortcut) {
        this.shortcut = shortcut;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public Date getExecutionStarted() {
        return executionStarted;
    }

    public void setExecutionStarted(Date executionStarted) {
        this.executionStarted = executionStarted;
    }

    public Date getExecutionFinished() {
        return executionFinished;
    }

    public void setExecutionFinished(Date executionFinished) {
        this.executionFinished = executionFinished;
    }

    public MetricType getType() {
        return type;
    }

    public void setType(MetricType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public MetricStatus getStatus() {
        return status;
    }

    public void setStatus(MetricStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MetricInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortcut='" + shortcut + '\'' +
                ", provider='" + provider + '\'' +
                ", technology='" + technology + '\'' +
                ", executionStarted=" + executionStarted +
                ", executionFinished=" + executionFinished +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }

    public enum MetricType{
        DATASET,
        NODE,
        EDGE
    }

    public enum MetricStatus{
        FINISHED,
        RUNNING,
        FAILED
    }

}
