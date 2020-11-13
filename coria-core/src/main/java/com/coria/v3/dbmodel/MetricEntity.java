package com.coria.v3.dbmodel;

import com.coria.v3.config.AppContext;
import com.coria.v3.metrics.MetricAlgorithm;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import com.coria.v3.metrics.MetricAlgorithmVariant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
@JacksonXmlRootElement(localName = "metric")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "metric", schema = "coria")
public class MetricEntity implements Serializable {
    private UUID id;
    private MetricAlgorithmImplementation metricAlgorithmImplementation;
    private Timestamp started;
    private Timestamp finished;
    private MetricStatus status;
    private String message;
    private DatasetEntity dataset;
    private Collection<DatasetMetricResultEntity> datasetMetricResults;
    private Collection<EdgeMetricResultEntity> edgeMetricResults;
    private Collection<NodeMetricResultEntity> nodeMetricResults;
    private Collection<NodeLayoutPositionEntity> nodeLayoutPositions;
    private Collection<ShortestPathLengthEntity> shortestPathLengths;

    @JsonIgnore
    @Id
    @Column(name = "metric_id", nullable = false, columnDefinition = "BINARY(16)")
    public UUID getId() {
        return id;
    }

    public void setId(UUID metricId) {
        this.id = metricId;
    }

    @JacksonXmlProperty(localName = "metric-algorithm-implementation", isAttribute = true)
    @Basic
    @Column(name = "metric_algorithm_implementation", nullable = false, length = 128)
    public String getMetricAlgorithmImplementationId() {
        return metricAlgorithmImplementation == null ? null : metricAlgorithmImplementation.getId();
    }

    public void setMetricAlgorithmImplementationId(String metricAlgorithmImplementationId) throws Exception {
        this.metricAlgorithmImplementation = AppContext.getInstance().getMetricAlgorithmImplementation(metricAlgorithmImplementationId);
    }

    @JsonIgnore
    @Transient
    public MetricAlgorithmImplementation getMetricAlgorithmImplementation() {
        return metricAlgorithmImplementation;
    }

    public void setMetricAlgorithmImplementation(MetricAlgorithmImplementation metricAlgorithmImplementation) {
        this.metricAlgorithmImplementation = metricAlgorithmImplementation;
    }

    @JsonIgnore
    @Transient
    public MetricAlgorithm getMetricAlgorithm() {
        return this.metricAlgorithmImplementation.getMetricAlgorithm();
    }

    @JacksonXmlProperty(localName = "started", isAttribute = true)
    @Basic
    @Column(name = "started", columnDefinition = "DATETIME(3)") // parameter (3) defines 3 digits of precision = milliseconds resolution
    public Timestamp getStarted() {
        return started;
    }

    public void setStarted(Timestamp started) {
        this.started = started;
    }

    @JacksonXmlProperty(localName = "finished", isAttribute = true)
    @Basic
    @Column(name = "finished", columnDefinition = "DATETIME(3)")
    public Timestamp getFinished() {
        return finished;
    }

    public void setFinished(Timestamp finished) {
        this.finished = finished;
    }

    @JacksonXmlProperty(localName = "status", isAttribute = true)
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    public MetricStatus getStatus() {
        return status;
    }

    public void setStatus(MetricStatus status) {
        this.status = status;
    }

    @JacksonXmlProperty(localName = "message", isAttribute = true)
    @Basic
    @Column(name = "message", columnDefinition = "TEXT")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "metric", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<DatasetMetricResultEntity> getDatasetMetricResults() {
        return datasetMetricResults;
    }

    private void setDatasetMetricResults(Collection<DatasetMetricResultEntity> datasetMetricResults) {
        if (datasetMetricResults != null)
            datasetMetricResults.forEach(datasetMetricResultEntity -> datasetMetricResultEntity.setMetric(this));
        this.datasetMetricResults = datasetMetricResults;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "metric", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<EdgeMetricResultEntity> getEdgeMetricResults() {
        return edgeMetricResults;
    }

    private void setEdgeMetricResults(Collection<EdgeMetricResultEntity> edgeMetricResults) {
        this.edgeMetricResults = edgeMetricResults;
    }

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", referencedColumnName = "dataset_id", nullable = false)
    public DatasetEntity getDataset() {
        return dataset;
    }

    public void setDataset(DatasetEntity dataset) {
        this.dataset = dataset;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "metric", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<NodeMetricResultEntity> getNodeMetricResults() {
        return nodeMetricResults;
    }

    private void setNodeMetricResults(Collection<NodeMetricResultEntity> nodeMetricResults) {
        this.nodeMetricResults = nodeMetricResults;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "metric", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<NodeLayoutPositionEntity> getNodeLayoutPositions() {
        return nodeLayoutPositions;
    }

    private void setNodeLayoutPositions(Collection<NodeLayoutPositionEntity> nodeLayoutPositions) {
        this.nodeLayoutPositions = nodeLayoutPositions;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "metric", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<ShortestPathLengthEntity> getShortestPathLengths() {
        return shortestPathLengths;
    }

    private void setShortestPathLengths(Collection<ShortestPathLengthEntity> shortestPathLengths) {
        this.shortestPathLengths = shortestPathLengths;
    }

    public MetricEntity() {
        this.id = UUID.randomUUID();
    }

    public MetricEntity(MetricAlgorithmVariant metricAlgorithmVariant, MetricAlgorithmImplementation metricAlgorithmImplementation, Timestamp started, MetricStatus status, DatasetEntity dataset) throws Exception {
        this();
        this.metricAlgorithmImplementation = metricAlgorithmImplementation;
        this.started = started;
        this.status = status;
        this.dataset = dataset;
        this.dataset.addMetric(this);
        switch (metricAlgorithmVariant.getMetricAlgorithm().getType()) {
            case Dataset:
                this.datasetMetricResults = new ArrayList<>();
                break;
            case Node:
                this.nodeMetricResults = new ArrayList<>();
                break;
            case Edge:
                this.edgeMetricResults = new ArrayList<>();
                break;
            case ShortestPathLength:
                this.shortestPathLengths = new ArrayList<>();
                break;
            case LayoutPosition:
                this.nodeLayoutPositions = new ArrayList<>();
                break;
            case Unknown:
                throw new Exception("Unknown metric algorithm type.");
        }
    }

    @Override
    public String toString() {
        String output = "MetricEntity{" +
                "id=" + id +
                ", metricAlgorithmImplementation=" + metricAlgorithmImplementation.getId() +
                ", started=" + started +
                ", finished=" + finished +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", dataset=" + dataset.getName();

        if (datasetMetricResults != null && datasetMetricResults.size() > 0)
            output += ", datasetMetricResults=" + datasetMetricResults.size();

        if (edgeMetricResults != null && edgeMetricResults.size() > 0)
            output += ", edgeMetricResults=" + edgeMetricResults.size();

        if (nodeMetricResults != null && nodeMetricResults.size() > 0)
            output += ", nodeMetricResults=" + nodeMetricResults.size();

        if (nodeLayoutPositions != null && nodeLayoutPositions.size() > 0)
            output += ", nodeLayoutPositions=" + nodeLayoutPositions.size();

        if (shortestPathLengths != null && shortestPathLengths.size() > 0)
            output += ", shortestPathLengths=" + shortestPathLengths.size();

        return output + '}';
    }

    public enum MetricStatus {
        FINISHED,
        RUNNING,
        SCHEDULED,
        FAILED
    }


}

