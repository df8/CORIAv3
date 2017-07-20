package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by Sebastian Gross
 * Offers convenience methods for working with Graphstream
 */
public class GSHelper {
    public static Graph createGraphFromDataSet(DataSet dataset){
        Logger logger = LoggerFactory.getLogger(GSAllPairShortestPath.class);
        Graph g = new DefaultGraph("temp");
        g.setStrict(false);
        g.setAutoCreate(true); //automatically create nodes based on edges
        for (CoriaEdge edge : dataset.getEdges()) {
            try {
                logger.trace("Edge: " + edge);
                Edge e = g.addEdge(edge.getSourceNode() + "->" + edge.getDestinationNode(), edge.getSourceNode(), edge.getDestinationNode());
                for(Map.Entry<String, String> att : edge.getAttributes().entrySet()){
                    e.addAttribute(att.getKey(), att.getValue());
                }
            } catch (Exception ex) {
                logger.error("failed creating edge for CoriaEdge {}", edge);
                logger.error(ex.getMessage());
                return null;
            }
        }
        return g;
    }
}
