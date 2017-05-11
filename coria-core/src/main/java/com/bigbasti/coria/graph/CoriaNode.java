package com.bigbasti.coria.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal representation of a node
 * Created by Sebastian Gross
 */
public class CoriaNode {

    private String id;
    private String name;

    private List<CoriaNode> neighbours;
    private Map<String, String> attributes;

    public CoriaNode(String name) {
        this.name = name;
        this.neighbours = new ArrayList<CoriaNode>();
        this.attributes = new HashMap<String, String>();
    }

    public CoriaNode(String id, String name) {
        this.id = id;
        this.name = name;
        this.neighbours = new ArrayList<CoriaNode>();
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

    public List<CoriaNode> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<CoriaNode> neighbours) {
        this.neighbours = neighbours;
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
        return "CoriaNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", neighbours=" + neighbours +
                ", attributes=" + attributes +
                '}';
    }
}
