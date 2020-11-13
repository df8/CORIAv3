package com.coria.v3.dbmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Entity
@Table(name = "node", schema = "coria")
public class NodeEntity {
    private UUID id;
    private String name;
    private Collection<EdgeEntity> edgesOutgoing;
    private Collection<EdgeEntity> edgesIncoming;
    private DatasetEntity dataset;
    private Map<String, String> attributes;
    private Collection<NodeMetricResultEntity> metricResults;
    private Collection<NodeLayoutPositionEntity> layoutPositions;
    private Collection<ShortestPathLengthEntity> shortestPathLengthsBySourceNode;
    private Collection<ShortestPathLengthEntity> shortestPathLengthsByTargetNode;
    private Collection<NodeASOrganizationEntity> organizations;

    @JsonIgnore
    @Id
    @Column(name = "node_id", nullable = false, columnDefinition = "BINARY(16)")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    @Basic
    @Column(name = "name", nullable = false, length = 128)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "nodeSource", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<EdgeEntity> getEdgesOutgoing() {
        return edgesOutgoing;
    }

    public void setEdgesOutgoing(Collection<EdgeEntity> edgesOutgoing) {
        this.edgesOutgoing = edgesOutgoing;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "nodeTarget", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<EdgeEntity> getEdgesIncoming() {
        return edgesIncoming;
    }

    public void setEdgesIncoming(Collection<EdgeEntity> edgesIncoming) {
        this.edgesIncoming = edgesIncoming;
    }

    @Transient
    @JsonIgnore
    public Set<NodeEntity> getNeighbours() {
        Set<NodeEntity> neighbours =
                this.edgesIncoming.stream().map(EdgeEntity::getNodeSource).collect(Collectors.toSet());
        neighbours.addAll(
                this.edgesOutgoing.stream().map(EdgeEntity::getNodeTarget).collect(Collectors.toSet())
        );
        return neighbours;
    }

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", referencedColumnName = "dataset_id", nullable = false)
    public DatasetEntity getDataset() {
        return dataset;
    }

    public void setDataset(DatasetEntity dataset) {
        this.dataset = dataset;
    }

    @ElementCollection
    @CollectionTable(name = "node_attribute", joinColumns = @JoinColumn(name = "node_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @JoinColumn(name = "node_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Map<String, String> getAttributes() {
        if (this.attributes == null)
            this.attributes = new HashMap<>();
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
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<NodeMetricResultEntity> getMetricResults() {
        return metricResults;
    }

    public void setMetricResults(Collection<NodeMetricResultEntity> nodeMetricResults) {
        this.metricResults = nodeMetricResults;
    }

    @Transient
    public void addMetricResult(MetricEntity metricEntity, double value) {
        if (this.metricResults == null)
            this.metricResults = new ArrayList<>();
        NodeMetricResultEntity nodeMetricResultEntity = new NodeMetricResultEntity(value, metricEntity, this);
        metricEntity.getNodeMetricResults().add(nodeMetricResultEntity);
        this.metricResults.add(nodeMetricResultEntity);
    }

    @JsonIgnore
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<NodeLayoutPositionEntity> getLayoutPositions() {
        return layoutPositions;
    }

    public void setLayoutPositions(Collection<NodeLayoutPositionEntity> nodeLayoutPositions) {
        this.layoutPositions = nodeLayoutPositions;
    }

    @Transient
    public void addLayoutPosition(MetricEntity metricEntity, double x, double y) {
        if (this.layoutPositions == null)
            this.layoutPositions = new ArrayList<>();
        NodeLayoutPositionEntity nodeLayoutPositionEntity = new NodeLayoutPositionEntity(x, y, metricEntity, this);
        metricEntity.getNodeLayoutPositions().add(nodeLayoutPositionEntity);
        this.layoutPositions.add(nodeLayoutPositionEntity);
    }

    @JsonIgnore
    @OneToMany(mappedBy = "nodeSource", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<ShortestPathLengthEntity> getShortestPathLengthsBySourceNode() {
        return shortestPathLengthsBySourceNode;
    }

    public void setShortestPathLengthsBySourceNode(Collection<ShortestPathLengthEntity> shortestPathLengthsBySourceNode) {
        this.shortestPathLengthsBySourceNode = shortestPathLengthsBySourceNode;
    }


    @JsonIgnore
    @OneToMany(mappedBy = "nodeTarget", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<ShortestPathLengthEntity> getShortestPathLengthsByTargetNode() {
        return shortestPathLengthsByTargetNode;
    }

    public void setShortestPathLengthsByTargetNode(Collection<ShortestPathLengthEntity> shortestPathLengthsByTargetNode) {
        this.shortestPathLengthsByTargetNode = shortestPathLengthsByTargetNode;
    }


    @JsonIgnore
    @OneToMany(mappedBy = "node", cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Collection<NodeASOrganizationEntity> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(Collection<NodeASOrganizationEntity> nodeOrganizations) {
        this.organizations = nodeOrganizations;
    }

    @Transient
    public void addOrganization(ASOrganizationEntity organization, String changeDate) {
        if (this.organizations == null)
            this.organizations = new ArrayList<>();
        NodeASOrganizationEntity nodeASOrganizationEntity = new NodeASOrganizationEntity(organization, this, changeDate);
        organization.addNode(nodeASOrganizationEntity);
        this.organizations.add(nodeASOrganizationEntity);
    }

    @Override
    public String toString() {
        return "NodeEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", attributes=" + attributes +
                ", metricResults=" + metricResults +
                ", layoutPositions=" + layoutPositions +
                ", edgesOutgoing=" + edgesOutgoing +
                ", edgesIncoming=" + edgesIncoming +
                '}';
    }

    // Additional helper methods for JSON and XML serialization. Attributes and metric results are stored as key-value maps
    @JsonProperty("metricResults")
    @Transient
    public Map<String, Double> getMetricResultsAsMap() {
        return this.metricResults.stream().collect(Collectors.toMap(metricResult -> metricResult.getMetric().getMetricAlgorithmImplementationId(), NodeMetricResultEntity::getValue));
    }
}
