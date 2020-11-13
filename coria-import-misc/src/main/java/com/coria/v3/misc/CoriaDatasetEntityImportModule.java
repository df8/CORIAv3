package com.coria.v3.misc;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 */
@Component
public class CoriaDatasetEntityImportModule extends CoriaDatasetEntityImportModuleBase {

    private final static Logger logger = LoggerFactory.getLogger(CoriaDatasetEntityImportModule.class);


    public CoriaDatasetEntityImportModule() {
        super("coria-xml-json-import-module",
                "CORIA XML/JSON Import Module",
                "<p>This Importer can be used to import a DataSet, which was previously exported by the <code>CORIA DataSet Export Adapter</code></p>\n" +
                        "<p>The content of the file must be either in <code>XML</code> or <code>JSON</code> format.</p>" +
                        "<p><strong>NOTE:</strong> Even if the DatSet file contains a name for the Set, it will be overwritten with the name you specify here in the Name textfield!</p>");
    }

    @Override
    public DatasetEntity parseInformation(String datasetName, List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception {
        logger.debug("starting import of data");
        if (files.size() == 0)
            throw new Exception("You have not provided any file to import.");
        if (files.size() > 1)
            throw new Exception("Please provide only one single file to import.");
        logger.debug("data format accepted - begin parsing");

        parseInformationBase(datasetName);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(files.get(0).getInputStream(), StandardCharsets.UTF_8))) {
            String strData = br.lines().collect(Collectors.joining("\n"));

            if (strData.startsWith("<")) {
                logger.debug("parsing file as XML");
                try {
                    XmlMapper xmlMapper = new XmlMapper();
                    ImportedDataset importedDataset = xmlMapper.readValue(strData, ImportedDataset.class);
                    logger.debug("{}", importedDataset.toString());

                    for (ImportedNode node : importedDataset.nodes) {
                        createNode(node.name);
                    }

                    for (ImportedEdge edge : importedDataset.edges) {
                        createEdge(nodeDict.get(edge.nodeSource), nodeDict.get(edge.nodeTarget));
                    }

                    importedDataset.attributes.forEach((k, v) -> {
                        if (!k.equals("count_nodes") && !k.equals("count_edges"))
                            datasetEntity.addAttribute(k, v);
                    });

                    finalizeImport(repositoryManager.getDatasetRepository());
                } catch (IOException e) {
                    logger.error("error while deserializing xml: {}", e.getMessage());
                    e.printStackTrace();
                    throw new Exception(e.getMessage());
                }
            } else {
                logger.debug("parsing file as JSON");
                if (strData.startsWith("\"")) {
                    //Undo JSON escaping: leading and tailing quotes like "
                    strData = strData.substring(1, strData.length() - 1);
                    strData = strData.replaceAll("\\\\", "");
                }
                datasetEntity = new ObjectMapper().readValue(strData, DatasetEntity.class);
            }

            if (datasetEntity.getAttributes() == null) {
                datasetEntity.setAttributes(new HashMap<>());
            }

            logger.debug("parsing finished, parsed " + datasetEntity.getNodes().size() + " nodes, " + datasetEntity.getEdges().size() + " edges");
        }
        return datasetEntity;
    }


    @Override
    public String toString() {
        return "CoriaDataSetImporter{" + "id: " + getId() + ", name: " + getName() + "}";
    }


    static class ImportedEdge {
        public String nodeSource;
        public String nodeTarget;

        public Map<String, String> attributes;
        public Map<String, Double> metricResults;
    }

    static class ImportedNode {
        public String name;
        public Map<String, String> attributes;
        public Map<String, Double> metricResults;
    }

    static class ImportedMetric {
        public String moduleId;
        public Timestamp started;
        public Timestamp finished;
        public MetricEntity.MetricStatus status;
        public String message;
    }


    static class ImportedDataset {
        public List<ImportedEdge> edges;
        public List<ImportedNode> nodes;
        public Map<String, String> attributes;
        public Map<String, Double> metricResults;
        public String name;
        public Timestamp created;
    }

}
