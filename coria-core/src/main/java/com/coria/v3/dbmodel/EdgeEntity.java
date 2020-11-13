package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "edge", schema = "coria")
@IdClass(EdgeEntityPK.class)
public class EdgeEntity {
    private String name;
    private NodeEntity nodeSource;
    private NodeEntity nodeTarget;
    private DatasetEntity dataset;
    private Map<String, String> attributes;
    private Collection<EdgeMetricResultEntity> metricResults;
    private Collection<EdgeASLocationEntity> locations;


    private final Logger logger = LoggerFactory.getLogger(EdgeEntity.class);

    /**
     * Returns an artificially generated unique identifying string
     *
     * @return String
     */
    @JsonIgnore
    @Transient
    public String getId() {
        return this.getNodeSourceId().toString() + "--" + this.getNodeTargetId().toString();
    }

    public void setId(String id) {
    }

    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    public NodeEntity getNodeSource() {
        return nodeSource;
    }

    public void setNodeSource(NodeEntity nodeBySourceNode) {
        this.nodeSource = nodeBySourceNode;
    }

    @JsonIgnore
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE})
    public NodeEntity getNodeTarget() {
        return nodeTarget;
    }

    public void setNodeTarget(NodeEntity nodeTarget) {
        this.nodeTarget = nodeTarget;
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

    @JsonIgnore
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    @ManyToOne(cascade = {CascadeType.MERGE})
    @JoinColumn(name = "dataset_id", referencedColumnName = "dataset_id", nullable = false)
    public DatasetEntity getDataset() {
        return dataset;
    }

    public void setDataset(DatasetEntity dataset) {
        this.dataset = dataset;
    }

    @JsonIgnore
    @ElementCollection
    @CollectionTable(name = "edge_attribute", joinColumns = {
            @JoinColumn(name = "source_node", referencedColumnName = "source_node", nullable = false),
            @JoinColumn(name = "target_node", referencedColumnName = "target_node", nullable = false)}
    )
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @JoinColumn(name = "source_node", referencedColumnName = "source_node", nullable = false)
    //@JoinColumn(name = "target_node", referencedColumnName = "target_node", nullable = false)
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

    @OneToMany(mappedBy = "edge", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<EdgeMetricResultEntity> getMetricResults() {
        return metricResults;
    }

    public void setMetricResults(Collection<EdgeMetricResultEntity> edgeMetricResults) {
        this.metricResults = edgeMetricResults;
    }

    @Transient
    public void addMetricResult(MetricEntity metricEntity, double value) {
        if (this.metricResults == null)
            this.metricResults = new ArrayList<>();
        EdgeMetricResultEntity edgeMetricResultEntity = new EdgeMetricResultEntity(value, metricEntity, this);
        metricEntity.getEdgeMetricResults().add(edgeMetricResultEntity);
        this.metricResults.add(edgeMetricResultEntity);
    }

    @JacksonXmlElementWrapper(localName = "locations")
    @JacksonXmlProperty(localName = "location")
    @OneToMany(mappedBy = "edge", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<EdgeASLocationEntity> getLocations() {
        return locations;
    }

    public void setLocations(Collection<EdgeASLocationEntity> locations) {
        this.locations = locations;
    }

    public void addLocation(ASLocationEntity asLocationEntity, String source) {
        if (this.locations == null)
            this.locations = new ArrayList<>();
        EdgeASLocationEntity edgeASLocationEntity = new EdgeASLocationEntity(this, asLocationEntity, source);
        asLocationEntity.getEdgeLocations().add(edgeASLocationEntity);
        this.locations.add(edgeASLocationEntity);
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

    @JsonProperty("metricResults")
    @Transient
    public Map<String, Double> getMetricResultsAsMap() {
        return this.metricResults.stream().collect(Collectors.toMap(metricResult -> metricResult.getMetric().getMetricAlgorithmImplementationId(), EdgeMetricResultEntity::getValue));
    }

    /*@JsonProperty("locations")
    @Transient
    public List<String> getLocationsAsList() {
        return this.locations.stream().map(edgeASLocationEntity -> edgeASLocationEntity.getLocation().getId()).collect(Collectors.toList());
    }*/
}
