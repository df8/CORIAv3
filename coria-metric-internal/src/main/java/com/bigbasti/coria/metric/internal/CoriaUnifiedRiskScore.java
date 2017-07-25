package com.bigbasti.coria.metric.internal;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metric.gs.GSHelper;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;

/**
 * Created by Sebastian Gross
 */
@Component
public class CoriaUnifiedRiskScore implements Metric{
    private Logger logger = LoggerFactory.getLogger(CoriaUnifiedRiskScore.class);

    @Override
    public String getIdentification() {
        return "java-coria-unified-risk-score";
    }

    @Override
    public String getDescription() {
        return "<p>1. Executes correction of <code>Clustering Coefficients, Average Neighbour Degree</code> and <code>Iterated Average Neighbour Degree</code> algorithms</p>" +
                "<p>2. Executes normalization of all metrics listed below</p>" +
                "<p>3. Calculates Risk Score based on the following weighting: <br/>" +
                "<ul><li><code>Node Degree</code>: 0.25</li>" +
                "<li><code>Average Neighbour Degree</code>: 0.15</li>" +
                "<li><code>Iterated Neighbour Degree</code>: 0.1</li>" +
                "<li><code>Betweenness Centrality</code>: 0.25</li>" +
                "<li><code>Average Shortest Path Length</code>: 0.25</li></ul>" +
                "<strong>NOTE:</strong> All the metrics above must be executed before this metric!</p>" +
                "<p>This Metric is based on the Master Thesis 'Internet Resilience and Connectivity Risks for Online Businesses' by Annika Baumann</p>";
    }

    @Override
    public String getName() {
        return "Unified Risk Score";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "urs";
    }

    @Override
    public String getProvider() {
        return "CORIA";
    }

    @Override
    public MetricInfo.MetricType getType() {
        return MetricInfo.MetricType.NODE;
    }

    @Override
    public DataSet performCalculations(DataSet dataset) {
        logger.debug("calculating {} for dataset {}", getName(), dataset.getId());
        try {

            logger.debug("trying to create a temp graph with provided data...");
            Graph g = GSHelper.createGraphFromDataSet(dataset);
            logger.debug("successful finished graph creation");

            logger.debug("starting metric computation...");
            double maxIand = 0;
            float counter = 0;
            for (Node n : g) {
                long degreeSum = 0;
                int nodesCount = 0;
                Iterator<Node> neighborNodes = g.getNode(n.getId()).getNeighborNodeIterator();
                while(neighborNodes.hasNext()){
                    Node firstLevelNode = neighborNodes.next();
                    Iterator<Node> secodLevelNodes = firstLevelNode.getNeighborNodeIterator();
                    while(secodLevelNodes.hasNext()){
                        Node secondLevelNode = secodLevelNodes.next();
                        degreeSum += secondLevelNode.getDegree();
                        nodesCount++;
                    }
                }
                double iand = 0d;
                if(nodesCount > 0){
                    iand = degreeSum / nodesCount;
                }
                CoriaNode currentNode = dataset.getNodes()
                        .stream()
                        .filter(coriaNode -> coriaNode.getName().equals(n.getId()))
                        .findFirst()
                        .get();
                currentNode.setAttribute(getShortcut(), String.valueOf(iand));
                if(iand > maxIand){
                    maxIand = iand;
                }
                counter++;
                if(counter % 1000 == 0){
                    logger.debug("{} Progress: {}/{}", getName(), counter, dataset.getNodesCount());
                }
            }

            logger.debug("updating relative node degree");

            for(CoriaNode n : dataset.getNodes()){
                Double relIand = (Double.valueOf(n.getAttribute(getShortcut())) / maxIand) * 100;
                n.setAttribute(getShortcut() + "_relative", relIand.toString());
            }

            logger.debug("finished metric computation");

            return dataset;
        }catch(Exception ex){
            logger.error("Error while executing calculation: {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "CoriaUnifiedRiskScore{id: " + getIdentification() + ", name: " + getName() + "}";
    }
}
