package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Calculates the Node Degree for a given DataSet
 * using GraphStream Java Library
 * Created by Sebastian Gross
 */
@Component
public class GSNodeDegree implements Metric {

    private Logger logger = LoggerFactory.getLogger(GSNodeDegree.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-node-degree";
    }

    @Override
    public String getDescription() {
        return "Calculates the degree for each Node in a DataSet";
    }

    @Override
    public String getName() {
        return "Node Degree";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "ndeg";
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
        logger.debug("calculating node degree for dataset {}", dataset.getId());
        try {
            Graph g = GSHelper.createGraphFromDataSet(dataset);
            logger.debug("successful finished graph creation");

            logger.debug("updating dataset...");
            int maxDegree = 0;
            float counter = 0;
            for (Node n : g) {
                int deg = n.getDegree();
                CoriaNode currentNode = dataset.getNodes()
                        .stream()
                        .filter(coriaNode -> coriaNode.getName().equals(n.getId()))
                        .findFirst()
                        .get();
                currentNode.setAttribute(getShortcut(), String.valueOf(deg));
                if(deg > maxDegree){
                    maxDegree = deg;
                }
                counter++;
                if(counter % 1000 == 0){
                    logger.debug("{} Progress: {}/{}", getName(), counter, dataset.getNodesCount());
                }
            }

            logger.debug("updating relative node degree");

            for(CoriaNode n : dataset.getNodes()){
                Double relNdeg = (Double.valueOf(n.getAttribute(getShortcut())) / maxDegree) * 100;
                n.setAttribute(getShortcut() + "_relative", relNdeg.toString());
            }

            logger.debug("updating dataset finished");

            return dataset;
        }catch(Exception ex){
            logger.error("Error while executing calculation: {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "GSNodeDegree{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
