package com.bigbasti.coria.metric.nx;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metric.tools.FSTools;
import com.bigbasti.coria.metrics.Metric;
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
public class NXClusteringCoefficient implements Metric {
    private Logger logger = LoggerFactory.getLogger(NXAverageNeighbourDegree.class);

    @Autowired
    FSTools fsTools;

    @Override
    public String getIdentification() {
        return "python-networkx-clustering-coefficient";
    }

    @Override
    public String getDescription() {
        return "Compute the clustering coefficient for nodes. <br/> For unweighted graphs, the clustering of a node u is the fraction of possible triangles through that node that exist,<br/>" +
                "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/e02119031edbc373d28811944663c191c9e53d1c.png\"><br/>" +
                "where T(u) is the number of triangles through node u and deg(u) is the degree of u. For weighted graphs, the clustering is defined as the geometric average of the subgraph edge weights<br/>" +
                "<img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/ea71c34aaf758ad8c19f0129bc003db96c8ca69b.png\"><br/>" +
                "The edge weights \\hat{w}_{uv} are normalized by the maximum weight in the network <img src=\"https://networkx.github.io/documentation/networkx-1.10/_images/math/fd424b86ab2590d0323aae53278c3fa4ca6cabf7.png\"><br/>" +
                "The value of c_u is assigned to 0 if deg(u) < 2.";
    }

    @Override
    public String getName() {
        return "Clustering Coefficients";
    }

    @Override
    public String getTechnology() {
        return "Python3";
    }

    @Override
    public String getShortcut() {
        return "clco";
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
            String BC_SCRIPT = "/metric/clco/clustering_coefficients.py";
            URL dir_url = NXBetweennessCentrality.class.getResource(BC_SCRIPT);
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
            double maxBc = 0.0;
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
                        cn.setAttribute(getShortcut(), parts[1]);
                        nodes.add(cn);
                        try{
                            Double bc = Double.valueOf(parts[1]);
                            if(bc > maxBc){
                                maxBc = bc;
                            }
                        }catch(Exception ex){logger.warn("could not parse bc value {} from node {}", parts[1], parts[0]);}
                    }
                }
                br.close();
            } catch (IOException e) {
                logger.error("failed reading response file: {}", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("failed reading response file: " + e.getMessage());
            }

            logger.debug("updating relative clustering coefficients");

            for(CoriaNode n : nodes){
                Double relBc = (Double.valueOf(n.getAttribute(getShortcut())) / maxBc) * 100;
                logger.trace("BC: {} / {} * 100 = {}", Double.valueOf(n.getAttribute(getShortcut())), maxBc, relBc);
                n.setAttribute(getShortcut()+"_relative", relBc.toString());
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
        return "NXClusteringCoefficient{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
