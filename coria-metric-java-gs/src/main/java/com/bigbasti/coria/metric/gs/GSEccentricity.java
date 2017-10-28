package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.MetricInfo;
import com.bigbasti.coria.metrics.MetricModule;
import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.Eccentricity;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Sebastian Gross
 */
@Component
public class GSEccentricity implements MetricModule{
    private Logger logger = LoggerFactory.getLogger(GSEccentricity.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-eccentricity";
    }

    @Override
    public String getDescription() {
        return "<p>Compute the eccentricity of a connected graph.</p><p>In a graph G, if d(u,v) is the shortest length between two nodes u and v (ie the number of edges of the shortest path) let e(u) be the d(u,v) such that v is the farthest of u. Eccentricity of a graph G is a subgraph induced by vertices u with minimum e(u).</p>";
    }

    @Override
    public String getName() {
        return "Eccentricity";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "ecc";
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
        logger.debug("calculating eccentricity for dataset {}", dataset.getId());
        try {

            logger.debug("trying to create a temp graph with provided data...");

            Graph g = GSHelper.createGraphFromDataSet(dataset);

            logger.debug("successful finished graph creation");

            logger.debug("starting metric computation...");
            logger.debug("\t--->APSP");
            APSP apsp = new APSP();
            apsp.init(g); // registering apsp as a sink for the graph
            apsp.setDirected(false); // undirected graph
            apsp.setWeightAttributeName("weight"); // ensure that the attribute name used is "weight"

            apsp.compute(); // the method that actually computes shortest paths
            logger.debug("finished metric computation");

            logger.debug("\t--->Eccentricity");
            Eccentricity eccentricity = new Eccentricity();
            eccentricity.init(g);
            eccentricity.compute();

            logger.debug("updating dataset...");
            for (Node n : g) {
                CoriaNode currentNode = dataset.getNodes()
                        .stream()
                        .filter(coriaNode -> coriaNode.getAsid().equals(n.getId()))
                        .findFirst()
                        .get();
                currentNode.setAttribute(getShortcut(), n.getAttribute("eccentricity")?"1":"0");
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
        return "GSEccentricity{id: " + getIdentification() + ", name: " + getName() +"}";
    }
}
