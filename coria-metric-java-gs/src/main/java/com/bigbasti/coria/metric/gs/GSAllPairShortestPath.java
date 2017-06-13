package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Sebastian Gross
 */
@Component
public class GSAllPairShortestPath implements Metric{
    private Logger logger = LoggerFactory.getLogger(GSAllPairShortestPath.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-all-pair-shortest-path";
    }

    @Override
    public String getDescription() {
        return "<p>This class implements the Floyd-Warshall all pair shortest path algorithm where the shortest path from any node to any destination in a given weighted graph (with positive or negative edge weights) is performed.</p><p>The computational complexity is O(n^3), this may seems a very large, however this algorithm may perform better than running several Dijkstra on all node pairs of the graph (that would be of complexity O(n^2 log(n))) when the graph becomes dense.</p>";
    }

    @Override
    public String getName() {
        return "All Pair Shortest Path";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "apsp";
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
        logger.debug("calculating all pair shortest path for dataset {}", dataset.getId());
        try {

            logger.debug("trying to create a temp graph with provided data...");

            Graph g = GSHelper.createGraphFromDataSet(dataset);

            logger.debug("successful finished graph creation");

            logger.debug("starting metric computation...");
            APSP apsp = new APSP();
            apsp.init(g); // registering apsp as a sink for the graph
            apsp.setDirected(false); // undirected graph
            apsp.setWeightAttributeName("weight"); // ensure that the attribute name used is "weight"

            apsp.compute(); // the method that actually computes shortest paths
            logger.debug("finished metric computation");

            logger.debug("updating dataset...");
            for (Node n : g) {
                CoriaNode currentNode = dataset.getNodes()
                        .stream()
                        .filter(coriaNode -> coriaNode.getName().equals(n.getId()))
                        .findFirst()
                        .get();
                currentNode.setAttribute(getShortcut(), "not available yet");
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
        return "GSAllPairShortestPath{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
