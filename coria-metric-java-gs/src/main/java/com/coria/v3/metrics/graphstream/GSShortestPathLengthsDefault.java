package com.coria.v3.metrics.graphstream;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.dbmodel.ShortestPathLengthEntity;
import com.coria.v3.repository.RepositoryManager;
import org.graphstream.algorithm.APSP;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <p>This class implements the Floyd-Warshall all pair shortest path algorithm where the shortest path from any node to any destination in a given weighted graph (with positive or negative edge weights) is performed.</p>
 * <p>The computational complexity is O(n^3), this may seems a very large, however this algorithm may perform better than running several Dijkstra on all node pairs of the graph (that would be of complexity O(n^2 log(n))) when the graph becomes dense.</p>
 * <p>
 * Created by Sebastian Gross, 2017
 * <p>
 * Modified by David Fradin, 2020:
 * - moved duplicate code fragments to {@link GSMetricAlgorithmImplementationBase }
 * - renamed GSAllPairShortestPath to GSShortestPathLengthsDefault
 * Note: All Pairs Shortest Path is not a real metric, but rather an auxiliary algorithm.
 * It does not provide a number for each Node, but a list shortest paths from any node to any other node.
 */
@Component
public class GSShortestPathLengthsDefault extends GSMetricAlgorithmImplementationBase {

    public GSShortestPathLengthsDefault() throws Exception {
        //super(MetricAlgorithm.Unknown); //"All Pair Shortest Path"
        super(AppContext.getInstance().getMetricAlgorithmByName("Shortest Path Lengths").getMetricAlgorithmVariantByName("Default"));
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) {
        logger.debug("starting {}", this.getId());

        Graph g = createGraphFromDataSet(datasetEntity);
        APSP apsp = new APSP();
        apsp.init(g); // registering apsp as a sink for the graph
        apsp.setDirected(false); // undirected graph
        apsp.setWeightAttributeName("weight"); // ensure that the attribute name used is "weight"
        apsp.compute(); // the method that actually computes shortest paths
        logger.debug("calculation completed, updating dataset");
        ArrayList<ShortestPathLengthEntity> splList = new ArrayList<>();
        for (Node n0 : g) {
            NodeEntity nodeEntity0 = nodeDictByName.get(n0.getId());
            APSP.APSPInfo splFromN = n0.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
            for (Node n1 : g) {
                if (n0.getId().compareTo(n1.getId()) < 0) {
                    NodeEntity nodeEntity1 = nodeDictByName.get(n1.getId());
                    splList.add(new ShortestPathLengthEntity(metricEntity, nodeEntity0, nodeEntity1, (int) Math.round(splFromN.getLengthTo(n1.getId()))));
                }
            }

            //nodeEntity.addMetricResult(metricEntity, result);
        }
        metricEntity.getShortestPathLengths().addAll(splList);
        logger.debug("calculation for {} finished successfully", getName());
    }

    @Override
    public String toString() {
        return "GSAllPairShortestPath{id: " + getId() + ", name: " + getName() + "}";
    }
}
