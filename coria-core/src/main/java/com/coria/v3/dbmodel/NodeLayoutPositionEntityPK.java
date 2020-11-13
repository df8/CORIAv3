package com.coria.v3.dbmodel;

import javax.persistence.CascadeType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by David Fradin, 2020
 */
public class NodeLayoutPositionEntityPK implements Serializable {
    private MetricEntity metric;
    private NodeEntity node;

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "metric_id", referencedColumnName = "metric_id", nullable = false, insertable = false, updatable = false)
    public MetricEntity getMetric() {
        return metric;
    }

    public void setMetric(MetricEntity metric) {
        this.metric = metric;
    }

    @Id
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "node_id", referencedColumnName = "node_id", nullable = false, insertable = false, updatable = false)
    public NodeEntity getNode() {
        return node;
    }

    public void setNode(NodeEntity node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeLayoutPositionEntityPK that = (NodeLayoutPositionEntityPK) o;
        return Objects.equals(metric, that.metric) &&
                Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metric, node);
    }
}
