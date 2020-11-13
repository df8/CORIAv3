package com.coria.v3.dbmodel;

import javax.persistence.*;

/**
 * Created by David Fradin, 2020
 */
@Entity
@Table(name = "node_metric_result", schema = "coria")
@IdClass(NodeMetricResultEntityPK.class)
public class NodeMetricResultEntity {

    private MetricEntity metric;
    private NodeEntity node;
    private double value;

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "metric_id", referencedColumnName = "metric_id", nullable = false)
    public MetricEntity getMetric() {
        return metric;
    }

    public void setMetric(MetricEntity metric) {
        this.metric = metric;
    }

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "node_id", referencedColumnName = "node_id", nullable = false)
    public NodeEntity getNode() {
        return node;
    }

    public void setNode(NodeEntity node) {
        this.node = node;
    }

    @Basic
    @Column(name = "metric_result_value", nullable = false)
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }


    public NodeMetricResultEntity() {
    }

    public NodeMetricResultEntity(double value, MetricEntity metric, NodeEntity node) {
        this.metric = metric;
        this.node = node;
        this.value = value;
    }

    @Override
    public String toString() {
        return "NodeMetricResultEntity{" +
                "node=" + node.getName() +
                ", value=" + value +
                ", metricImpl=" + (metric != null ? metric.getMetricAlgorithmImplementationId() : "null") +
                '}';
    }
}
