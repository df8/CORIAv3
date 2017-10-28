package com.bigbasti.coria.metric.nx;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.interop.FSTools;
import com.bigbasti.coria.metrics.MetricModule;
import com.bigbasti.coria.metrics.MetricInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Sebastian Gross
 */
@Component
public class NXEccentricity implements MetricModule {
    private Logger logger = LoggerFactory.getLogger(NXAverageNeighbourDegree.class);

    @Autowired
    FSTools fsTools;

    @Override
    public String getIdentification() {
        return "python-networkx-eccentricity";
    }

    @Override
    public String getDescription() {
        return "The eccentricity of a node v is the maximum distance from v to all other nodes in G.";
    }

    @Override
    public String getName() {
        return "Eccentricity";
    }

    @Override
    public String getTechnology() {
        return "Python3";
    }

    @Override
    public String getShortcut() {
        return "ecc";
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
        String fullPathToScript = fsTools.getFullPathToResource("/metric/ecc/eccentricity.py");
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
        return "NXEccentricity{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
