package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "shortest_path_length_metric_results", schema = "coria")
@IdClass(ShortestPathLengthEntityPK.class)
@SuppressWarnings("unused")
public class ShortestPathLengthEntity {
    private NodeEntity nodeSource;
    private NodeEntity nodeTarget;
    private MetricEntity metric;
    private int distance;

    private final Logger logger = LoggerFactory.getLogger(ShortestPathLengthEntity.class);

    public ShortestPathLengthEntity() {
    }

    public ShortestPathLengthEntity(MetricEntity metric, NodeEntity nodeSource, NodeEntity nodeTarget, int distance) {
        this.metric = metric;
        this.nodeSource = nodeSource;
        this.nodeTarget = nodeTarget;
        this.distance = distance;
    }

    /**
     * Returns an artificially generated unique identifying string
     *
     * @return String
     */
    @JsonIgnore
    @Transient
    public String getId() {
        return this.getMetricId().toString() + "--" + this.getNodeSourceId().toString() + "--" + this.getNodeTargetId().toString();
    }

    public void setId(String id) {
    }


    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "source_node", referencedColumnName = "node_id", nullable = false)
    public NodeEntity getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(NodeEntity nodeSource) {
        this.nodeSource = nodeSource;
    }

    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "target_node", referencedColumnName = "node_id", nullable = false)
    public NodeEntity getNodeTarget() {
        return nodeTarget;
    }

    public void setNodeTarget(NodeEntity nodeTarget) {
        this.nodeTarget = nodeTarget;
    }

    @JsonIgnore
    @Transient
    public UUID getMetricId() {
        return metric.getId();
    }

    @JsonIgnore
    @Transient
    public UUID getNodeSourceId() {
        return nodeSource.getId();
    }

    @JsonIgnore
    @Transient
    public UUID getNodeTargetId() {
        return nodeTarget.getId();
    }

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "metric_id", referencedColumnName = "metric_id", nullable = false)
    public MetricEntity getMetric() {
        return metric;
    }

    public void setMetric(MetricEntity metric) {
        this.metric = metric;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "ShortestPathLengthEntity{" +
                "nodeSource=" + nodeSource.getName() +
                ", nodeTarget=" + nodeTarget.getName() +
                ", distance=" + distance +
                '}';
    }

    // Additional helper methods for JSON and XML serialization. Attributes and metric results are stored as key-value maps
    @JsonProperty("nodeSource")
    @Transient
    public String getNodeSourceName() {
        return this.nodeSource.getName();
    }

    @JsonProperty("nodeTarget")
    @Transient
    public String getNodeTargetName() {
        return this.nodeTarget.getName();
    }

}
