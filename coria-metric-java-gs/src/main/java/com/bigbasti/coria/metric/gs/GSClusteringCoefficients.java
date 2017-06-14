package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.Eccentricity;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Sebastian Gross
 */
@Component
public class GSClusteringCoefficients implements Metric {
    private Logger logger = LoggerFactory.getLogger(GSClusteringCoefficients.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-clustering-coefficients";
    }

    @Override
    public String getDescription() {
        return "Computes the clustering coefficient for all nodes in the graph. The complexity if O(d^2) where d is the degree of the node.";
    }

    @Override
    public String getName() {
        return "Clustering Coefficients";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "clco";
    }

    @Override
    public String getProvider() {
        return "GraphStream";
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
            for (Node n : g) {
                CoriaNode currentNode = dataset.getNodes()
                        .stream()
                        .filter(coriaNode -> coriaNode.getName().equals(n.getId()))
                        .findFirst()
                        .get();
                double cc = Toolkit.clusteringCoefficient(n);
                currentNode.setAttribute(getShortcut(), String.valueOf(cc));
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
        return "GSClusteringCoefficients{id: " + getIdentification() + ", name: " + getName() + "}";
    }
}