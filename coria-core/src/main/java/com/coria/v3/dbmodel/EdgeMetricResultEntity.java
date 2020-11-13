package com.coria.v3.dbmodel;

import javax.persistence.*;

/**
 * Created by David Fradin, 2020
 */
@Entity
@Table(name = "edge_metric_result", schema = "coria")
@IdClass(EdgeMetricResultEntityPK.class)
public class EdgeMetricResultEntity {

    private MetricEntity metric;
    private EdgeEntity edge;
    private double value;

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "metric_id", referencedColumnName = "metric_id", nullable = false)
    public MetricEntity getMetric() {
        return metric;
    }

    public void setMetric(MetricEntity metric) {
        this.metric = metric;
    }

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumns({@
            JoinColumn(name = "source_node", referencedColumnName = "source_node", nullable = false),
            @JoinColumn(name = "target_node", referencedColumnName = "target_node", nullable = false)})
    public EdgeEntity getEdge() {
        return edge;
    }

    public void setEdge(EdgeEntity edge) {
        this.edge = edge;
    }

    @Basic
    @Column(name = "metric_result_value", nullable = false)
    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public EdgeMetricResultEntity() {
    }

    public EdgeMetricResultEntity(double value, MetricEntity metric, EdgeEntity edge) {
        this.metric = metric;
        this.edge = edge;
        this.value = value;
    }
}
