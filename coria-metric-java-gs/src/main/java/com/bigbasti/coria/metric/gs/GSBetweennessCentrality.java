package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Sebastian Gross on 02.06.2017.
 */
@Component
public class GSBetweennessCentrality implements Metric {
    private Logger logger = LoggerFactory.getLogger(GSBetweennessCentrality.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-betweenness-centrality";
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
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "bc";
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
        logger.debug("beginning calculation of " + getName());
        logger.debug("creating graph");

        Graph g = new DefaultGraph("BC Graph");

        g.setStrict(false);
        g.setAutoCreate(true);

        for(CoriaEdge e : dataset.getEdges()){
            try {
                g.addEdge(e.getName(), e.getSourceNode().getName(), e.getDestinationNode().getName());
            }catch(Exception ex){
                logger.error("Error creating edge: {} because {}", e, ex.getMessage());
            }
        }
        logger.debug("graph created, starting calculation");

        BetweennessCentrality bc = new BetweennessCentrality();
//        bc.setWeightAttributeName("weight");
        bc.init(g);
        bc.compute();

        logger.debug("calculation completed, updating dataset");

        double maxCb = 0.0;
        for(Node n: g){
            double cb = n.getAttribute("Cb");
            CoriaNode cn = dataset.getNodes().stream().filter(coriaNode -> coriaNode.getName().equals(n.getId())).findFirst().get();
            if(cn == null){
                logger.warn("could not update node {} - node not found in dataset", n.getId());
            }else{
                cn.setAttribute(getShortcut(), String.valueOf(cb));
            }
            if (cb > maxCb) {
                maxCb = cb;
            }
        }

        logger.debug("updating relative betweenness centrality");

        for(CoriaNode n : dataset.getNodes()){
            Double relBc = (Double.valueOf(n.getAttribute(getShortcut())) / maxCb) * 100;
            logger.trace("BC: {} / {} * 100 = {}", Double.valueOf(n.getAttribute(getShortcut())), maxCb, relBc);
            n.setAttribute(getShortcut()+"_relative", relBc.toString());
        }

        logger.debug("calculation for {} finished successfully", getName());

        return dataset;
    }

    @Override
    public String toString() {
        return "GSBetweennessCentrality{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
