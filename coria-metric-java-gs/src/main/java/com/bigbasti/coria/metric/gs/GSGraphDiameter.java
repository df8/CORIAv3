package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Sebastian Gross
 */
@Component
public class GSGraphDiameter implements Metric{
    private Logger logger = LoggerFactory.getLogger(GSGraphDiameter.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-graph-diameter";
    }

    @Override
    public String getDescription() {
        return "computes the diameter of the graph. The diameter of the graph is the largest of all the shortest paths from any node to any other node.<br/>Note that this operation can be quite costly, the algorithm used to compute all shortest paths is the Floyd-Warshall algorithm whose complexity is at worst of O(n^3).";
    }

    @Override
    public String getName() {
        return "Graph Diameter";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "gdia";
    }

    @Override
    public String getProvider() {
        return "GraphStream";
    }

    @Override
    public MetricInfo.MetricType getType() {
        return MetricInfo.MetricType.DATASET;
    }

    @Override
    public DataSet performCalculations(DataSet dataset) {
        logger.debug("calculating {} for dataset {}", getName(), dataset.getId());
        try {
            logger.debug("trying to create a temp graph with provided data...");
            Graph g = GSHelper.createGraphFromDataSet(dataset);
            logger.debug("successful finished graph creation");

            logger.debug("starting metric computation...");
            double andeg = Toolkit.diameter(g);
            dataset.setAttribute(getShortcut(), String.valueOf(andeg));
            logger.debug("finished metric computation - result: {}", String.valueOf(andeg));

            return dataset;
        }catch(Exception ex){
            logger.error("Error while executing calculation: {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "GSGraphDiameter{id: " + getIdentification() + ", name: " + getName() + "}";
    }
}
