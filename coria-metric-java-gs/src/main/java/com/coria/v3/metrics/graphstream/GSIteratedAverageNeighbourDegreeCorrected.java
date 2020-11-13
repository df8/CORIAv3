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

/**
 * Initially implemented by Sebastian Gross, 2017 in coria-metric-internal/src/main/java/com/bigbasti/coria/metric/internal/CoriaUnifiedRiskScore.java -> correctIteratedAverageNeighbourDegree()
 * Rewritten by David Fradin, 2020:
 * - Moved to a separate class
 * - Adopted to changed data structures
 * - Used HashMaps and Java 8 Streams for efficiency
 */
@Component
public class GSIteratedAverageNeighbourDegreeCorrected extends GSMetricAlgorithmImplementationBase {

    public GSIteratedAverageNeighbourDegreeCorrected() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Iterated Average Neighbour Degree").getMetricAlgorithmVariantByName("Corrected"));
    }

    /**
     * @param repositoryManager
     * @param datasetEntity
     * @param metricEntity
     * @param dependencyMetricIds
     * @throws Exception
     */
    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        logger.debug("starting {}", this.getId());
        Instant starts = Instant.now();
        // Read the results of node degree into a key-value-map
        Map<NodeEntity, Double> nodeDegreeMap = GSUtility.getNodeMetricResultsMap(repositoryManager, dependencyMetricIds.get("node-degree--default"));

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

            n0.addMetricResult(metricEntity, GSAverageNeighbourDegreeCorrected.computeAverageNeighbourDegreeCorrected(neighbours_2hop, nodeDegreeMap));
        }

        Instant ends = Instant.now();
        logger.debug("finished correction of iterated average neighbour degree ({})", Duration.between(starts, ends));
    }
}
