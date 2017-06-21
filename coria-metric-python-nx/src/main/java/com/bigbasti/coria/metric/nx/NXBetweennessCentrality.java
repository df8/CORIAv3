package com.bigbasti.coria.metric.nx;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.StorageStatus;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.interop.ThreadedStreamHandler;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

/**
 * Created by Sebastian Gross
 */
@Component
public class NXBetweennessCentrality implements Metric {
    private Logger logger = LoggerFactory.getLogger(NXBetweennessCentrality.class);

    @Autowired
    Environment env;

    @Override
    public String getIdentification() {
        return "java-networkx-betweenness-centrality";
    }

    @Override
    public String getDescription() {
        return "Compute the betweenness centrality of each vertex of a given graph.<br/>" +
                "The betweenness centrality counts how many shortest paths between each pair of nodes of the graph pass by a node. It does it for all nodes of the graph.<br/>" +
                "<img src=\"http://graphstream-project.org/media/img/betweennessCentrality.png\"><br/>" +
                "The above graph shows the betweenness centrality applied to a grid graph, where color indicates centrality, green is lower centrality and red is maximal centrality.";
    }

    @Override
    public String getName() {
        return "Betweenness Centrality";
    }

    @Override
    public String getTechnology() {
        return "Python 3";
    }

    @Override
    public String getShortcut() {
        return "bc";
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
        logger.debug("determining coria working directory...");
        String workingDir = env.getProperty("coria.workingdirectory");
        if(Strings.isNullOrEmpty(workingDir)){
            logger.debug("could not load working directory from properties, trying system variable 'CORIA_HOME'...");
            workingDir = System.getenv("CORIA_HOME");
            if(Strings.isNullOrEmpty(workingDir)){
                logger.error("could not determine working directory, canceling metric execution, make sure coria workspace is setup correctly!");
                throw new RuntimeException("could not determine working directory, canceling metric execution, make sure coria workspace is setup correctly!");
            }
        }
        logger.debug("using following directory as coria home: {}", workingDir);

        logger.debug("creating import file for python script...");
        String filename = getIdentification() + "_" + dataset.getId() + "_" + String.valueOf(System.currentTimeMillis()) + "_import.csv";
        String fullPath = workingDir + FileSystems.getDefault().getSeparator() + filename;
        String responseFileName = fullPath.replace("import.csv", "export.csv");
        logger.debug("Using paths:\nRequest File: {}\nResponse File:{}", fullPath, responseFileName);

        Instant starts = Instant.now();
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(fullPath));
            for(CoriaEdge e : dataset.getEdges()){
                pw.println("XX\t" + e.getSourceNode().getName() + "\t" + e.getDestinationNode().getName());
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
            File python = new File("bc/betweenness_centrality.py");
            System.out.println(">>>>>>>>>>>" + python.exists()); // true

            URL dir_url = NXBetweennessCentrality.class.getResource("resources/bc/betweenness_centrality.py");
            logger.debug("URL: {}", dir_url);

            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("python bc/betweenness_centrality.py -f " + fullPath + " -s " + responseFileName);

            ThreadedStreamHandler stdOut = new ThreadedStreamHandler(pr.getInputStream());
            ThreadedStreamHandler errOut = new ThreadedStreamHandler(pr.getErrorStream());

            stdOut.start();
            errOut.start();

            int exitValue = pr.waitFor();

            stdOut.interrupt();
            errOut.interrupt();
            stdOut.join();
            errOut.join();

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
            try {
                br = new BufferedReader(new FileReader(response));
                for (String line; (line = br.readLine()) != null; ) {
                    String parts[] = line.split(",");
                    CoriaNode cn = dataset.getNodes().stream().filter(coriaNode -> coriaNode.getName().equals(parts[0])).findFirst().get();
                    if(cn == null){
                        logger.warn("could not update node {} - node not found in dataset", parts[0]);
                    }else{
                        cn.setAttribute(getShortcut(), parts[1]);
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
        }else{
            logger.error("the expected python output file ({}) was not found, canceling metric execution", responseFileName);
            throw new RuntimeException("the expected python output file ({}) was not found, canceling metric execution");
        }

        String archivedFileName = responseFileName.replace("export.csv", "archive.csv");
        logger.debug("renaming response file to {}", archivedFileName);
        if(response.renameTo(new File(archivedFileName))){
            logger.debug("succellful renamed file");
        }else{
            logger.error("could not rename file - trying to delete it...");
            if(response.delete()){
                logger.debug("response file was deleted");
            }else{
                logger.error("could not delete response file.\nNOTE: if the response file stays in coria working directory it will be reprocessed at restart of the application");
            }
        }

        return dataset;
    }

    @Override
    public String toString() {
        return "NXBetweennessCentrality{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
