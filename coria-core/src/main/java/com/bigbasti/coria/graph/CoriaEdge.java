package com.bigbasti.coria.graph;

import java.util.HashMap;
import java.util.Map;

/**
 * Internal representation of an Edge
 * Created by Sebastian Gross
 */
public class CoriaEdge {
    private String id;
    private String name;
    private CoriaNode sourceNode;
    private CoriaNode destinationNode;

    private Map<String, String> attributes;

    public CoriaEdge(String name, CoriaNode sourceNode, CoriaNode destinationNode) {
        this.name = name;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.attributes = new HashMap<String, String>();
    }

    public CoriaEdge(String id, String name, CoriaNode sourceNode, CoriaNode destinationNode) {
        this.id = id;
        this.name = name;
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.attributes = new HashMap<String, String>();
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

    public CoriaNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(CoriaNode sourceNode) {
        this.sourceNode = sourceNode;
    }

    public CoriaNode getDestinationNode() {
        return destinationNode;
    }

    public void setDestinationNode(CoriaNode destinationNode) {
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