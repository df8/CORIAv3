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
 * Implemented by David Fradin, 2020
 */
@Component
public class GSAverageNeighbourDegreeDefault extends GSMetricAlgorithmImplementationBase {

    public GSAverageNeighbourDegreeDefault() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Average Neighbour Degree").getMetricAlgorithmVariantByName("Default"));
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        logger.debug("starting {}", this.getId());
        Instant starts = Instant.now();
        // Read the results of node degree into a key-value-map
        Map<NodeEntity, Double> nodeDegreeMap = GSUtility.getNodeMetricResultsMap(repositoryManager, dependencyMetricIds.get("node-degree--default"));

        for (NodeEntity n0 : datasetEntity.getNodes()) {
            double and = 0;
            Set<NodeEntity> neighbours = n0.getNeighbours();
            if (neighbours.size() > 0) {
                ArrayList<Double> n0_nodeDegrees = neighbours
                        .stream()
                        .map(nodeDegreeMap::get)
                        .collect(Collectors.toCollection(ArrayList::new));
                and = n0_nodeDegrees.stream().mapToDouble(x -> x).average().orElse(0);
            }
            n0.addMetricResult(metricEntity, and);
        }

        Instant ends = Instant.now();
        logger.debug("finished {} ({})", this.getId(), Duration.between(starts, ends));
    }
}
