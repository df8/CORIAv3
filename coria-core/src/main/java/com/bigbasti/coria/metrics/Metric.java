package com.bigbasti.coria.metrics;

import java.util.Date;

/**
 * Created by Sebastian Gross
 */
public class Metric {
    private String id;
    private String name;
    private String shortcut;
    private String provider;
    private String technology;
    private Date executionStarted;
    private Date executionFinished;

    public Metric() {
    }

    public Metric(String id, String name, String shortcut, String provider, String technology, Date executionStarted, Date executionFinished) {
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

    @Override
    public String toString() {
        return "Metric{" +
                "id='" + id + '\'' +
                ", shortcut='" + shortcut + '\'' +
                ", technology='" + technology + '\'' +
                ", executionStarted=" + executionStarted +
                ", executionFinished=" + executionFinished +
                '}';
    }
}
