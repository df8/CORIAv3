package com.bigbasti.coria.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Internal representation of an Edge
 * Created by Sebastian Gross
 */
public class CoriaEdge  implements Serializable {
    private String id;
    private String name;
    private String sourceNode;
    private String destinationNode;

    private Map<String, String> attributes;

    public CoriaEdge(){
        this.attributes = new HashMap<String, String>();
    }

    public CoriaEdge(String name) {
        this.name = name;
        this.attributes = new TreeMap<String, String>();
    }

    public CoriaEdge(String name, String sourceNode, String destinationNode) {
        this.name = name;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.attributes = new TreeMap<String, String>();
    }

    public CoriaEdge(String id, String name, String sourceNode, String destinationNode) {
        this.id = id;
        this.name = name;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.attributes = new TreeMap<String, String>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(String sourceNode) {
        this.sourceNode = sourceNode;
    }

    public String getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(String destinationNode) {
        this.destinationNode = destinationNode;
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

    @Override
    public String toString() {
        return "CoriaEdge{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sourceNode=" + sourceNode +
                ", destinationNode=" + destinationNode +
                ", attributes=" + attributes +
                '}';
    }
}
