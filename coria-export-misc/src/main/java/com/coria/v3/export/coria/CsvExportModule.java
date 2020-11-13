package com.coria.v3.export.coria;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.export.ExportModule;
import com.coria.v3.export.ExportModuleParameter;
import com.coria.v3.export.ExportResult;
import com.coria.v3.utility.Slugify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Sebastian Gross, 2017 (coria-export-misc/src/main/java/com/bigbasti/coria/export/coria/EdgesExportModule.java)
 * Modified by David Fradin, 2020: Adapted to JPA, changed description
 */
@Component
public class CsvExportModule extends ExportModule {
    private final Logger logger = LoggerFactory.getLogger(CsvExportModule.class);

    //TODO /1 proofread the descriptions of all export modules
    public CsvExportModule() {
        super("raw-csv-export-module",
                "Raw CSV Export Module",
                "<p>The <strong>CSV Export Module</strong> exports all either all edges or all nodes from a dataset into a Comma-Separated Values (<code>CSV</code>) text file.</p>" +
                        "<p>You can specify which separator character to use and which entity to export (nodes or edges).</p>" +
                        "<p>Alongside with the name of the entity, the module also exports all attributes and all computed metric results assigned to the entity.</p>",
                new ExportModuleParameter[]{
                        new ExportModuleParameter("CSV Separator", new String[]{"Tab", "Semicolon", "Comma"}),
                        new ExportModuleParameter("Type", new String[]{"Edges", "Nodes"}),
                });
    }

    /**
     * Converts the given dataset to a CSV with [separator] as separator char
     * If no separator (or invalid format) is provided, a comma is used as fallback
     *
     * @param dataset the dataset to be exported
     * @param params  additional parameters for the export adapter
     * @return String with the desired format
     */
    @Override
    public ExportResult exportDataset(DatasetEntity dataset, Map<String, String> params) {
        ExportResult result = new ExportResult();
        result.setContentType("text/csv;charset=utf-8;");
        logger.debug("starting export of dataset");

        String separator;
        if (params == null)
            params = new HashMap<>();
        switch (params.getOrDefault("csv-separator", "Tab")) {
            case "Semicolon":
                separator = ";";
                break;
            case "Comma":
                separator = ",";
                break;
            default:
            case "Tab":
                separator = "\t";
                break;
        }
        logger.debug("using separator: {}", separator);

        switch (params.getOrDefault("type", "Nodes")) {
            case "Edges":
                result.setExportResult(exportEdges(dataset, separator));
                result.setFileName(Slugify.toSlug(dataset.getName()) + "-edges.csv");
                break;
            default:
            case "Nodes":
                result.setExportResult(exportNodes(dataset, separator));
                result.setFileName(Slugify.toSlug(dataset.getName()) + "-nodes.csv");
                break;
        }
        logger.debug("finished CSV export successfully");
        return result;
    }

    private String exportEdges(DatasetEntity dataset, String separator) {
        StringBuilder builder = new StringBuilder();
        //Step 1: Collect all column headers
        HashSet<String> fieldNames = new HashSet<>();
        dataset.getEdges().forEach((edge) -> {
            fieldNames.addAll(edge.getAttributes().keySet());
            edge.getMetricResultsAsMap().forEach((key, value) -> fieldNames.add(key));
        });
        builder.append("node_source").append(separator);
        builder.append("node_target").append(separator);
        fieldNames.forEach(s -> builder.append(s).append(separator));
        builder.append(System.lineSeparator());

        //Step 2: Loop over all edges
        dataset.getEdges().forEach((edge) -> {
            builder.append(edge.getNodeSource().getName()).append(separator);
            builder.append(edge.getNodeTarget().getName()).append(separator);

            //Collect all attributes into a map. This way we guarantee that each row has the same number of columns.
            Map<String, String> attributeMap = new HashMap<>(edge.getAttributes());
            edge.getMetricResultsAsMap().forEach((key, value) -> attributeMap.put(key, String.valueOf(value)));

            //Print all attribute values column by column.
            fieldNames.forEach(attribute -> builder.append(attributeMap.getOrDefault(attribute, "null")).append(separator));
            builder.append(System.lineSeparator());
        });

        return builder.toString();
    }

    private String exportNodes(DatasetEntity dataset, String separator) {
        StringBuilder builder = new StringBuilder();
        //Step 1: Collect all column headers
        HashSet<String> fieldNames = new HashSet<>();
        dataset.getNodes().forEach((node) -> {
            fieldNames.addAll(node.getAttributes().keySet());
            node.getMetricResultsAsMap().forEach((key, value) -> fieldNames.add(key));
        });

        builder.append("name").append(separator);
        fieldNames.forEach(s -> builder.append(s).append(separator));
        builder.append(System.lineSeparator());

        //Step 2: Loop over all nodes
        dataset.getNodes().forEach((node) -> {
            builder.append(node.getName()).append(separator);

            //Collect all attributes into a map. This way we guarantee that each row has the same number of columns.
            Map<String, String> attributeMap = new HashMap<>(node.getAttributes());
            node.getMetricResultsAsMap().forEach((key, value) -> attributeMap.put(key, String.valueOf(value)));

            //Print all attribute values column by column.
            fieldNames.forEach(attribute -> builder.append(attributeMap.getOrDefault(attribute, "null")).append(separator));
            builder.append(System.lineSeparator());
        });

        return builder.toString();
    }


    @Override
    public String toString() {
        return "EdgesExportAdapter{" +
                "id: " + getId() +
                ", name: " + getName() +
                "}";
    }


}
