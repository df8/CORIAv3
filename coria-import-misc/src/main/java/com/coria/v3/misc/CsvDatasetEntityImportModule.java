package com.coria.v3.misc;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Parser for CSV files with two columns
 * Created by Sebastian Gross, 2017
 * Modified by David Fradin, 2020:
 * - Adopted to JPA
 * - refactored methods to reduce code redundancy
 * - Changed the description
 */
@Component
public class CsvDatasetEntityImportModule extends CoriaDatasetEntityImportModuleBase {
    private final Logger logger = LoggerFactory.getLogger(CsvDatasetEntityImportModule.class);

    public CsvDatasetEntityImportModule() {
        super("raw-csv-import-module",
                "Raw CSV Import Module",
                "<p>The Raw CSV Import Module is designed to import one or multiple text files containing at least two columns of data separated by a <code>TAB</code> character.</p>\n" +
                        "<p>Each line represents an edge: The first column contains the name of the source node and the second column the name of the target node.</p>\n" +
                        "<p>Example line: <code>A   B</code> is interpreted as: There is bidirectional edge (link) between node <code>A</code> and <code>B</code>.</p>\n" +
                        "<p>Each column may also contain multiple nodes separated by a comma:</p>\n" +
                        "<p>Example line: <code>X,Y   F,G,K</code> is interpreted as: There are bidirectional edges between each source node and each target node which results in the following six edges: " +
                        "<code>X&lt;-&gt;F</code>, <code>X&lt;-&gt;G</code>, <code>X&lt;-&gt;K</code>, <code>Y&lt;-&gt;F</code>, <code>Y&lt;-&gt;G</code> amd <code>Y&lt;-&gt;K</code>.</p>\n" +
                        "<p>The expected file format is <code>.txt</code>.</p>");
    }

    @Override
    public DatasetEntity parseInformation(String datasetName, List<UploadedFile> files, RepositoryManager repositoryManager) throws Exception {
        parseInformationBase(datasetName);

        logger.debug("starting import of data");

        for (UploadedFile file : files) {
            try (
                    InputStream is = file.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    parseLine(line);
                }
            }
        }

        finalizeImport(repositoryManager.getDatasetRepository());
        return datasetEntity;
    }


    protected void parseLine(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 2) {                              //ignore lines with not enough data
            //no whitespaces allowed in names
            String[] fromParts = parts[0].split(",");
            String[] toParts = parts[1].split(",");
            for (String fPart : fromParts) {
                NodeEntity fNode = createNode(fPart);
                for (String tPart : toParts) {
                    NodeEntity tNode = createNode(tPart);
                    createEdge(fNode, tNode);
                }
            }
        } else {
            logger.trace("ignoring line because it contains not enough values: " + line);
        }
    }

    @Override
    public String toString() {
        return "CsvImportModule{" + "id: " + getId() + ", name: " + getName() + "}";
    }
}
