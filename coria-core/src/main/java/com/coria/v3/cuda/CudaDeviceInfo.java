package com.coria.v3.cuda;


import com.coria.v3.metrics.ModuleBase;

import java.util.*;

/**
 * Created by David Fradin, 2020
 */
public class CudaDeviceInfo implements ModuleBase {

    protected String id;
    protected String name;
    protected String description;
    protected List<AbstractMap.SimpleEntry<String, String>> attributes;

    public CudaDeviceInfo() {
        attributes = new ArrayList<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<AbstractMap.SimpleEntry<String, String>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AbstractMap.SimpleEntry<String, String>> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String key, String value) {
        this.attributes.add(new AbstractMap.SimpleEntry<>(key, value));
    }

}
