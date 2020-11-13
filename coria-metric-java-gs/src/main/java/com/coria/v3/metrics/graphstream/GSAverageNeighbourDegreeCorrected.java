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
 * Initially implemented by Sebastian Gross, 2017 in CoriaUnifiedRiskScore.java -> correctAverageNeighbourDegree()
 * Rewritten by David Fradin, 2020:
 * - Moved to a separate class
 * - Adopted to changed data structures
 * - Used HashMaps and Java 8 Streams for efficiency
 */
@Component
public class GSAverageNeighbourDegreeCorrected extends GSMetricAlgorithmImplementationBase {

    public GSAverageNeighbourDegreeCorrected() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Average Neighbour Degree").getMetricAlgorithmVariantByName("Corrected"));
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
            n0.addMetricResult(metricEntity, computeAverageNeighbourDegreeCorrected(n0.getNeighbours(), nodeDegreeMap));
        }

        Instant ends = Instant.now();
        logger.debug("finished correction of average neighbour degree ({})", Duration.between(starts, ends));
    }

    public static double computeAverageNeighbourDegreeCorrected(Set<NodeEntity> neighbours, Map<NodeEntity, Double> nodeDegreeMap) {
        double correctedAverageNeighbourDegree = 0;
        if (neighbours.size() > 0) {
            ArrayList<Double> n0_nodeDegrees = neighbours
                    .stream()
                    .map(nodeDegreeMap::get)
                    .collect(Collectors.toCollection(ArrayList::new));
            double mean = n0_nodeDegrees.stream().mapToDouble(x -> x).average().orElse(0);
            if (mean != 0) {
                correctedAverageNeighbourDegree = mean;
                int count = n0_nodeDegrees.size();
                double stddev = Math.sqrt(n0_nodeDegrees.stream().mapToDouble(x -> Math.pow(x - mean, 2)).sum() / count);
                if (stddev != 0) {
                    double median = GSUtility.median(n0_nodeDegrees, Double::compareTo);
                    // Formula taken from Annika Baumann's Master's thesis - Internet Resilience and Connectivity - Risks for Online Businesses, page 94, equation 28.
                    correctedAverageNeighbourDegree = mean + (((median - mean) / stddev) / count) * mean;
                }
            }
        }
        return correctedAverageNeighbourDegree;
    }
}
