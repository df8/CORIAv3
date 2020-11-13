package com.coria.v3.dbmodel;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by David Fradin, 2020
 */
public class EdgeMetricResultEntityPK implements Serializable {
    private MetricEntity metric;
    private EdgeEntity edge;

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "metric_id", referencedColumnName = "metric_id", nullable = false, insertable = false, updatable = false)
    public MetricEntity getMetric() {
        return metric;
    }

    public void setMetric(MetricEntity metric) {
        this.metric = metric;
    }

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumns({
            @JoinColumn(name = "source_node", referencedColumnName = "source_node", nullable = false, insertable = false, updatable = false),
            @JoinColumn(name = "target_node", referencedColumnName = "target_node", nullable = false, insertable = false, updatable = false)})
    public EdgeEntity getEdge() {
        return edge;
    }

    public void setEdge(EdgeEntity edge) {
        this.edge = edge;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeMetricResultEntityPK that = (EdgeMetricResultEntityPK) o;
        return Objects.equals(metric, that.metric) &&
                Objects.equals(edge, that.edge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metric, edge);
    }
}
