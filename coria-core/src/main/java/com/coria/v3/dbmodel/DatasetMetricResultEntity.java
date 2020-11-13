package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

/**
 * Created by David Fradin, 2020
 */
@Entity
@Table(name = "dataset_metric_result", schema = "coria")
@IdClass(DatasetMetricResultEntityPK.class)
public class DatasetMetricResultEntity {

    private MetricEntity metric;
    private double value;
    private DatasetEntity dataset;


    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "metric_id", referencedColumnName = "metric_id", nullable = false)
    public MetricEntity getMetric() {
        return metric;
    }

    public void setMetric(MetricEntity metric) {
        this.metric = metric;
    }

    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "dataset_id", referencedColumnName = "dataset_id", nullable = false)
    public DatasetEntity getDataset() {
        return dataset;
    }

    public void setDataset(DatasetEntity dataset) {
        this.dataset = dataset;
    }

    @Basic
    @Column(name = "metric_result_value", nullable = false)
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }


    public DatasetMetricResultEntity() {
    }

    public DatasetMetricResultEntity(double value, MetricEntity metric, DatasetEntity dataset) {
        this.metric = metric;
        this.dataset = dataset;
        this.value = value;
    }
}
