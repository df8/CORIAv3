package com.coria.v3.metrics.graphstream;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.repository.RepositoryManager;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sebastian Gross, 2017
 * Fixed by David Fradin, 2020:
 * - moved duplicate code fragments to {@link GSMetricAlgorithmImplementationBase }
 * - The implementation by S. Gross was iterating through the two-hop neighbourhood of each node without sanitizing of duplicate neighbours
 * (e.g. with a graph having the paths A->B->E and A->C->E the implementation would count neighbour E twice.)
 * Additionally his implementation was collecting both neighbours with a distance of 1 and a distance of 2.
 * The new implementation collects only distinct neighbours with a distance of 2 and ignores duplicates.
 */
@Component
public class GSIteratedAverageNeighbourDegree extends GSMetricAlgorithmImplementationBase {

    public GSIteratedAverageNeighbourDegree() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Iterated Average Neighbour Degree").getMetricAlgorithmVariantByName("Default"));
    }


    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        logger.debug("starting {}", this.getId());
        Instant starts = Instant.now();
        // Read the results of node degree into a key-value-map
        Map<NodeEntity, Double> nodeDegreeMap = GSUtility.getNodeMetricResultsMap(repositoryManager, dependencyMetricIds.get("node-degree--default"));

        if (nodeDegreeMap.size() == 0) {
            throw new Exception("node-degree--default is not available.");
        }
        for (NodeEntity n0 : datasetEntity.getNodes()) {
            Set<NodeEntity> neighbours_1hop = n0.getNeighbours();
            Set<NodeEntity> neighbours_2hop = new HashSet<>();

            // Collect all 2 hop neighbours
            for (NodeEntity n1 : neighbours_1hop) {
                neighbours_2hop.addAll(n1.getNeighbours());
            }

            // Remove the currently viewed node from the 2-hop neighbourhood list.
            neighbours_2hop.remove(n0);

            // Remove all 2 hop neighbours which are at the same time 1 hop neighbours.
            // This way neighbours_2hop only contains nodes with the shortest distance from n0 of exactly 2.
            for (NodeEntity n1 : neighbours_1hop) {
                neighbours_2hop.remove(n1);
            }

            double iand = 0;
            if (neighbours_2hop.size() > 0) {
                ArrayList<Double> n0_nodeDegrees = neighbours_2hop
                        .stream()
                        .map(nodeDegreeMap::get)
                        .collect(Collectors.toCollection(ArrayList::new));
                iand = n0_nodeDegrees.stream().mapToDouble(x -> x).average().orElse(0);
            }
            n0.addMetricResult(metricEntity, iand);
        }

        Instant ends = Instant.now();
        logger.debug("finished correction of iterated average neighbour degree ({})", Duration.between(starts, ends));
    }

    @Override
    public String toString() {
        return "GSIteratedAverageNeighbourDegree{id: " + getId() + ", name: " + getName() + "}";
    }
}
