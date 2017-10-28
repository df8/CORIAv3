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
public class NXCircularLayout implements MetricModule {
    private Logger logger = LoggerFactory.getLogger(NXCircularLayout.class);

    @Autowired
    FSTools fsTools;

    @Override
    public String getIdentification() {
        return "python-networkx-circular-layout";
    }

    @Override
    public String getDescription() {
        return "Position nodes on a circle";
    }

    @Override
    public String getName() {
        return "Circular Layout (Graph Visualization)";
    }

    @Override
    public String getTechnology() {
        return "Python3";
    }

    @Override
    public String getShortcut() {
        return "pos";
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

        Instant starts = Instant.now();
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(fullPath));
            for(CoriaEdge e : dataset.getEdges()){
                pw.println("XX\t" + e.getSourceNode() + "\t" + e.getDestinationNode());
            }
            pw.close();

            Instant ends = Instant.now();
            logger.debug("python import file created succellful ({})", Duration.between(starts, ends));
        } catch (Exception e) {
            logger.error("could not create python import file: {}", e.getMessage());
            e.printStackTrace();
        }

        starts = Instant.now();
        logger.debug("starting python...");
        try {
            String BC_SCRIPT = "/metric/pos/circular_layout.py";
            URL dir_url = getClass().getResource(BC_SCRIPT);
            String fullPathToScript = Paths.get(dir_url.toURI()).toFile().getAbsolutePath();
            logger.debug("Script URL: {}", fullPathToScript);

            int exitValue = fsTools.startProcessAndWait("python \"" + fullPathToScript + "\" -f " + fullPath + " -s " + responseFileName);

            Instant ends = Instant.now();
            logger.debug("python execution finished ({})", Duration.between(starts, ends));

            if(exitValue != 0){
                //something happened -> do something
                logger.debug("python exit code: {}", exitValue);
            }
        }
        catch(Exception e) {
            System.out.println(e.toString());
        }

        File response = new File(responseFileName);
        if(response.exists()){
            logger.debug("reading response and updating dataset ...");
            starts = Instant.now();

            BufferedReader br = null;
            List<CoriaNode> nodes = new ArrayList<>();
            try {
                br = new BufferedReader(new FileReader(response));
                for (String line; (line = br.readLine()) != null; ) {
                    String parts[] = line.split(",");
                    Optional<CoriaNode> ocn = dataset.getNodes().stream().filter(coriaNode -> coriaNode.getAsid().equals(parts[0])).findFirst();
                    if(!ocn.isPresent()){
                        logger.warn("could not update node {} (value:{}) - node not found in dataset", parts[0], parts[1]);
                    }else{
                        CoriaNode cn = ocn.get();
                        String coordinates = parts[1];
                        coordinates = coordinates.replaceAll("0\\. ", "0.0");
                        coordinates = coordinates.replaceAll("1\\. ", "1.0");
                        coordinates = coordinates.replace("[","").replace("]","").trim().replaceAll("[ ]+", ":");
                        if(coordinates.equals("1.:0.")){coordinates = "1.0:0.0";}
                        cn.setAttribute(getShortcut(), coordinates);
                        nodes.add(cn);
                    }
                }
                br.close();
            } catch (IOException e) {
                logger.error("failed reading response file: {}", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("failed reading response file: " + e.getMessage());
            }

            Instant ends = Instant.now();
            logger.debug("finished reading response file ({})", Duration.between(starts, ends));

            fsTools.cleanupResponseFile(responseFileName, response);
        }else{
            logger.error("the expected python output file ({}) was not found, canceling metric execution", responseFileName);
            throw new RuntimeException("the expected python output file was not found (" + fsTools.getLastError() + ")");
        }

        return dataset;
    }

    @Override
    public String toString() {
        return "NXCircularLayout{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
