package com.coria.v3.metrics.graphstream;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.EdgeEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import com.coria.v3.metrics.MetricAlgorithmVariant;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class GSMetricAlgorithmImplementationBase extends MetricAlgorithmImplementation {
    protected final static Logger logger = LoggerFactory.getLogger(GSMetricAlgorithmImplementationBase.class);

    public GSMetricAlgorithmImplementationBase(MetricAlgorithmVariant metricAlgorithmVariant) throws Exception {
        //TODO description about GraphStream
        super("Java", "S. Gross / D. Fradin / GraphStream",
                "",
                10,
                metricAlgorithmVariant, true, null);
    }

    protected Map<String, NodeEntity> nodeDictByName;
    protected Map<String, EdgeEntity> edgeDictByName;

    /**
     * Creates a new GraphStream Graph Object from a given DataSet
     * Written by: Sebastian Gross, 2017
     *
     * @param dataset DatasetEntity to convert to a Graph
     * @return Graph containing the attributes, nodes and edges from the DatasetEntity
     */
    public Graph createGraphFromDataSet(DatasetEntity dataset) {
        nodeDictByName = dataset.getNodes().stream().collect(Collectors.toMap(NodeEntity::getName, nodeEntity -> nodeEntity));
        edgeDictByName = new HashMap<>(dataset.getEdgesAsMap());
        Logger logger = LoggerFactory.getLogger(GSMetricAlgorithmImplementationBase.class);
        logger.debug("trying to create a temp graph with provided data...");
        Graph g = new DefaultGraph("temp");
        g.setStrict(false);
        g.setAutoCreate(true); //automatically create nodes based on edges
        dataset.getEdges().forEach((edge) -> {
            try {
                logger.trace("Edge: " + edge);
                Edge e = g.addEdge(edge.getName(), edge.getNodeSourceName(), edge.getNodeTargetName());
                edge.getAttributes().forEach(e::addAttribute);
            } catch (Exception ex) {
                logger.error("failed creating edge for CoriaEdge {}", edge);
                logger.error(ex.getMessage());
            }
        });

        logger.debug("successful finished graph creation");
        return g;
    }
}
