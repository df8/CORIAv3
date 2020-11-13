package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 */
@JacksonXmlRootElement(localName = "dataset")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "dataset", schema = "coria")
public class DatasetEntity {

    private UUID id;
    private String name;
    private Timestamp created;
    private Map<String, String> attributes;
    private Collection<DatasetMetricResultEntity> metricResults;
    private Collection<EdgeEntity> edges;
    private Collection<MetricEntity> metrics;
    private Collection<NodeEntity> nodes;


    @JsonIgnore
    @Id
    @Column(name = "dataset_id", nullable = false, columnDefinition = "BINARY(16)")
    public UUID getId() {
        return id;
    }

    public void setId(UUID datasetId) {
        this.id = datasetId;
    }

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    @Basic
    @Column(name = "name", nullable = false, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JacksonXmlProperty(localName = "created", isAttribute = true)
    @Basic
    @Column(name = "created", columnDefinition = "DATETIME(3)", nullable = false)
    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }


    @JsonIgnore
    @ElementCollection
    @CollectionTable(name = "dataset_attribute", joinColumns = @JoinColumn(name = "dataset_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @JoinColumn(name = "dataset_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Transient
    public void addAttribute(String key, String value) {
        if (this.attributes == null)
            this.attributes = new HashMap<>();
        this.attributes.put(key, value);
    }

    @JsonIgnore
    @OneToMany(mappedBy = "dataset", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<DatasetMetricResultEntity> getMetricResults() {
        return metricResults;
    }

    public void setMetricResults(Collection<DatasetMetricResultEntity> datasetMetricResults) {
        this.metricResults = datasetMetricResults;
    }

    public void addMetricResult(MetricEntity metricEntity, double value) {
        if (this.metricResults == null)
            this.metricResults = new ArrayList<>();
        DatasetMetricResultEntity datasetMetricResultEntity = new DatasetMetricResultEntity(value, metricEntity, this);
        metricEntity.getDatasetMetricResults().add(datasetMetricResultEntity);
        this.metricResults.add(datasetMetricResultEntity);
    }

    @JacksonXmlElementWrapper(localName = "metrics")
    @JacksonXmlProperty(localName = "metric")
    @OneToMany(mappedBy = "dataset", cascade = {CascadeType.ALL})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<MetricEntity> getMetrics() {
        return metrics;
    }

    public void setMetrics(Collection<MetricEntity> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(MetricEntity metric) {
        if (metric != null) {
            metric.setDataset(this);
            if (this.metrics == null)
                this.metrics = new ArrayList<>();
            this.metrics.add(metric);
        }
    }

    @JsonIgnore
    @Transient
    public Map<UUID, MetricEntity> getMetricsAsMap() {
        return this.metrics.stream().collect(Collectors.toMap(MetricEntity::getId, metricEntity -> metricEntity));
    }

    @JacksonXmlElementWrapper(localName = "nodes")
    @JacksonXmlProperty(localName = "node")
    @OneToMany(mappedBy = "dataset", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<NodeEntity> getNodes() {
        return nodes;
    }

    public void setNodes(Collection<NodeEntity> nodes) {
        this.nodes = nodes;
    }

    public void addNode(NodeEntity node) {
        if (node != null) {
            if (this.nodes == null)
                this.nodes = new ArrayList<>();
            this.nodes.add(node);
            node.setDataset(this);
        }
    }

    @JacksonXmlElementWrapper(localName = "edges")
    @JacksonXmlProperty(localName = "edge")
    @OneToMany(mappedBy = "dataset", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<EdgeEntity> getEdges() {
        return edges;
    }

    public void setEdges(Collection<EdgeEntity> edges) {
        this.edges = edges;
    }

    public void addEdge(EdgeEntity edge) {
        if (edge != null) {
            edge.setDataset(this);
            if (this.edges == null)
                this.edges = new ArrayList<>();
            this.edges.add(edge);
        }
    }

    // Helper
    @JsonIgnore
    @Transient
    public Map<UUID, NodeEntity> getNodesAsMap() {
        return this.nodes.stream().collect(Collectors.toMap(NodeEntity::getId, nodeEntity -> nodeEntity));
    }

    @JsonIgnore
    @Transient
    public Map<String, EdgeEntity> getEdgesAsMap() {
        return this.edges.stream().collect(Collectors.toMap(EdgeEntity::getName, edgeEntity -> edgeEntity));
    }

    @JsonProperty("metricResults")
    @Transient
    public Map<String, Double> getMetricResultsAsMap() {
        return this.metricResults.stream().collect(Collectors.toMap(metricResult -> metricResult.getMetric().getMetricAlgorithmImplementationId(), DatasetMetricResultEntity::getValue));
    }
}
