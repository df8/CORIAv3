package com.bigbasti.coria.dataset;

import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.MetricInfo;

import java.io.Serializable;
import java.util.*;

/**
 * Represents an imported DataSet resulting from an import of data
 * Created by Sebastian Gross
 */
public class DataSet implements Serializable {
    private String id;
    private String name;
    private Date created;
    private int nodesCount;
    private int edgesCount;
    private List<CoriaEdge> edges;
    private List<CoriaNode> nodes;
    /**
     * List of email addresses which should be notified when changes
     * are made to this dataset. e.g. metric finishes computing
     */
    private List<String> notificationEmails;
    /**
     * additional attributes and properties for the dataset
     */
    private Map<String, String> attributes;

    /**
     * metricInfos that already had been executed for this dataset
     */
    private List<MetricInfo> metricInfos;

    public DataSet() {
        this.attributes = new TreeMap<String, String>();
        this.edges = new ArrayList<>();
        this.nodes = new ArrayList<>();
        this.metricInfos = new ArrayList<>();
        this.notificationEmails = new ArrayList<>();
    }

    public DataSet(List<CoriaEdge> edges, List<CoriaNode> nodes) {
        this.edges = edges;
        this.nodes = nodes;
        this.attributes = new TreeMap<String, String>();
        this.metricInfos = new ArrayList<>();
        this.notificationEmails = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<CoriaEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<CoriaEdge> edges) {
        this.edges = edges;
    }

    public List<CoriaNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<CoriaNode> nodes) {
        this.nodes = nodes;
    }

    public List<String> getNotificationEmails() {
        return notificationEmails;
    }

    public void setNotificationEmails(List<String> notificationEmails) {
        this.notificationEmails = notificationEmails;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getAttribute(String key){
        return this.attributes.get(key);
    }

    public void setAttribute(String key, String value){
        this.attributes.put(key, value);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MetricInfo> getMetricInfos() {
        return metricInfos;
    }

    public void setMetricInfos(List<MetricInfo> metricInfos) {
        this.metricInfos = metricInfos;
    }

    public int getNodesCount() {
        return nodesCount;
    }

    public void setNodesCount(int nodesCount) {
        this.nodesCount = nodesCount;
    }

    public int getEdgesCount() {
        return edgesCount;
    }

    public void setEdgesCount(int edgesCount) {
        this.edgesCount = edgesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSet dataSet = (DataSet) o;

        return id != null ? id.equals(dataSet.id) : dataSet.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DataSet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", created=" + created +
                ", edges=" + edges +
                ", nodes=" + nodes +
                ", notificationEmails=" + notificationEmails +
                ", attributes=" + attributes +
                '}';
    }
}
