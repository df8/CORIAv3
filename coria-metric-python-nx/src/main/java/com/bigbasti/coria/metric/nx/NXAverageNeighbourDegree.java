package com.bigbasti.coria.metric.nx;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.interop.FSTools;
import com.bigbasti.coria.metrics.MetricModule;
import com.bigbasti.coria.metrics.MetricInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.FileSystems;

/**
 * Created by Sebastian Gross
 */
@Component
public class NXAverageNeighbourDegree implements MetricModule {
    private Logger logger = LoggerFactory.getLogger(NXAverageNeighbourDegree.class);

    @Autowired
    FSTools fsTools;

    @Override
    public String getIdentification() {
        return "python-networkx-average-neighbour-degree";
    }

    @Override
    public String getDescription() {
        return "Returns the average degree of the neighborhood of each node. <br/> The average degree of a node i is <br/>" +
                "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/ef382473d588681f1efa5156fdf589c8cc3d7fb0.png\"><br/>" +
                "where N(i) are the neighbors of node i and k_j is the degree of node j which belongs to N(i). For weighted graphs, an analogous measure can be defined<br/>" +
                "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/20848505733e71688e5bfbe310322bac4b4ac407.png\"><br/>" +
                "where s_i is the weighted degree of node i, w_{ij} is the weight of the edge that links i and j and N(i) are the neighbors of node i.";
    }

    @Override
    public String getName() {
        return "Average Neighbour Degree";
    }

    @Override
    public String getTechnology() {
        return "Python3";
    }

    @Override
    public String getShortcut() {
        return "and";
    }

    @Override
    public String getProvider() {
        return "NetworkX";
    }

    @Override
    public MetricInfo.MetricType getType() {
        return MetricInfo.MetricType.NODE;
    }

    @Override
    public DataSet performCalculations(DataSet dataset) {
        String workingDir = null;
        try {
            workingDir = fsTools.getWorkingDirectory();
        } catch (Exception e) {
            logger.error("could not determine working directory, canceling metric execution, make sure coria workspace is setup correctly!");
            throw new RuntimeException("could not determine working directory, canceling metric execution, make sure coria workspace is setup correctly!");
        }

        logger.debug("creating import file for python script...");
        String filename = getIdentification() + "_" + dataset.getId() + "_" + String.valueOf(System.currentTimeMillis()) + "_import.csv";
        String fullPath = workingDir + FileSystems.getDefault().getSeparator() + filename;
        String responseFileName = fullPath.replace("import", "export");
        logger.debug("Using paths:\nRequest File: {}\nResponse File:{}", fullPath, responseFileName);

        boolean result = fsTools.writeEdgesFileToWorkingDir(dataset.getEdges(), fullPath, "\t", "XX\t");

        logger.debug("starting python...");
        String fullPathToScript = fsTools.getFullPathToResource("/metric/and/average_neighbour_degree.py");
        boolean scriptStart = fsTools.startSyncSystemProcess("python \"" + fullPathToScript + "\" -f " + fullPath + " -s " + responseFileName);

        File response = new File(responseFileName);
        if(response.exists()){
            fsTools.readNetworkXMetricResponseAndUpdateDataset(dataset, response, ",", getShortcut(), getName());
            fsTools.cleanupResponseFile(responseFileName, response);
        }else{
            logger.error("the expected python output file ({}) was not found, canceling metric execution", responseFileName);
            throw new RuntimeException("the expected python output file was not found. (" + fsTools.getLastError() + ")");
        }

        return dataset;
    }

    @Override
    public String toString() {
        return "NXAverageNeighbourDegree{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
