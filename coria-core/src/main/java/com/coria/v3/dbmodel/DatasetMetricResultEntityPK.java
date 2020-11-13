package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by David Fradin, 2020
 */
public class DatasetMetricResultEntityPK implements Serializable {
    private MetricEntity metric;
    private DatasetEntity dataset;

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "metric_id", referencedColumnName = "metric_id", nullable = false, insertable = false, updatable = false)
    public MetricEntity getMetric() {
        return metric;
    }

    public void setMetric(MetricEntity metric) {
        this.metric = metric;
    }

    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "dataset_id", referencedColumnName = "dataset_id", nullable = false, insertable = false, updatable = false)
    public DatasetEntity getDataset() {
        return dataset;
    }

    public void setDataset(DatasetEntity dataset) {
        this.dataset = dataset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetMetricResultEntityPK that = (DatasetMetricResultEntityPK) o;
        return Objects.equals(metric, that.metric) &&
                Objects.equals(dataset, that.dataset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metric, dataset);
    }
}
