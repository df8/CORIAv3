package com.coria.v3.metrics;


import com.coria.v3.utility.Slugify;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 */
public class MetricAlgorithm {

    protected String name;
    protected String description;
    protected MetricAlgorithmType type;
    protected final HashMap<String, MetricAlgorithmVariant> metricAlgorithmVariantsMap;

    public MetricAlgorithm(String name, String description, MetricAlgorithmType type, List<MetricAlgorithmVariant> metricAlgorithmVariantsMap) throws Exception {
        this.name = name;
        this.description = description;
        this.type = type;
        this.metricAlgorithmVariantsMap = new HashMap<>();
        if (metricAlgorithmVariantsMap != null) {
            for (MetricAlgorithmVariant mav : metricAlgorithmVariantsMap) {
                mav.setMetricAlgorithm(this);//Sets the reference to parent (MetricAlgorithm) for each child (MetricAlgorithmVariant)
                if (this.metricAlgorithmVariantsMap.containsKey(mav.getName())) {
                    throw new Exception("Duplicate Metric Algorithm Variant key " + mav.getName() + " for Metric Algorithm " + getName());
                }
                this.metricAlgorithmVariantsMap.put(mav.getName(), mav);
            }
        }
    }

    public String getId() {
        return Slugify.toSlug(this.name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MetricAlgorithmType getType() {
        return type;
    }

    public void setType(MetricAlgorithmType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, MetricAlgorithmVariant> getMetricAlgorithmVariantsMap() {
        return metricAlgorithmVariantsMap;
    }

    public Collection<MetricAlgorithmVariant> getMetricAlgorithmVariants() {
        return this.metricAlgorithmVariantsMap
                .values()
                .stream()
                //This custom sorting method puts the "Default" variant first and sorts the all other values in lexicographical order.
                .sorted((mav1, mav2) -> (mav1.getName().equals("Default")) ? -1 : Comparator.comparing(MetricAlgorithmVariant::getName).compare(mav1, mav2))
                .collect(Collectors.toList());
    }

    public MetricAlgorithmVariant getMetricAlgorithmVariantByName(String name) {
        return metricAlgorithmVariantsMap.get(name);
    }

    @Override
    public String toString() {
        return "MetricAlgorithm{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", metricAlgorithmVariants=(" + metricAlgorithmVariantsMap.size() + ")" +
                '}';
    }

 /*
    /
     * Transforms "CamelCaseNames" to "Friendly Names With Spaces"
     *
     * @param metricAlgorithm The metric algorithm to retrieve the name for.
     * @return
     /
    public static String getFriendlyName(MetricAlgorithm metricAlgorithm) {
        return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(metricAlgorithm.toString()), ' ');
    }

    */
}
