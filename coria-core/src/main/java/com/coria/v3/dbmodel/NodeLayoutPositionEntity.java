package com.coria.v3.dbmodel;

import javax.persistence.*;

/**
 * Created by David Fradin, 2020
 */
@Entity
@Table(name = "node_layout", schema = "coria")
@IdClass(NodeLayoutPositionEntityPK.class)
public class NodeLayoutPositionEntity {

    private MetricEntity metric;
    private NodeEntity node;
    private double x;
    private double y;

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
    @Column(name = "layout_x", nullable = false)
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Basic
    @Column(name = "layout_y", nullable = false)
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }


    public NodeLayoutPositionEntity() {
    }

    public NodeLayoutPositionEntity(double x, double y, MetricEntity metric, NodeEntity node) {
        this.metric = metric;
        this.node = node;
        this.x = x;
        this.y = y;
    }
}
