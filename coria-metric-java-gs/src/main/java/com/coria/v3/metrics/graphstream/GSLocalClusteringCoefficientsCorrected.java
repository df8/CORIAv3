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
 * Initially implemented by Sebastian Gross, 2017 in CoriaUnifiedRiskScore.java -> correctClusteringCoefficient()
 * Rewritten by David Fradin, 2020:
 * - Moved to a separate class
 * - Adopted to changed data structures
 * - Used HashMaps and Java 8 Streams for efficiency
 * Mind that there was a typo in the formula in Annika Baumann's Master's thesis - Internet Resilience and Connectivity - Risks for Online Businesses on  page 95, equation 29:
 * Incorrect: cc * (degree * cc) / 4
 * Correct:   cc + (degree * cc) / 4
 */
@Component
public class GSLocalClusteringCoefficientsCorrected extends GSMetricAlgorithmImplementationBase {

    public GSLocalClusteringCoefficientsCorrected() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Local Clustering Coefficients").getMetricAlgorithmVariantByName("Corrected"));
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
        Map<NodeEntity, Double> localCCMap = GSUtility.getNodeMetricResultsMap(repositoryManager, dependencyMetricIds.get("local-clustering-coefficients--default"));

        if (nodeDegreeMap.size() == 0) {
            throw new Exception("node-degree--default is not available.");
        }
        if (localCCMap.size() == 0) {
            throw new Exception("local-clustering-coefficients--default is not available.");
        }

        for (NodeEntity n0 : datasetEntity.getNodes()) {
            double ndeg = nodeDegreeMap.get(n0);
            double lcc = localCCMap.get(n0);
            // Formula taken from Annika Baumann's Master's thesis - Internet Resilience and Connectivity - Risks for Online Businesses, page 95, equation 29.
            n0.addMetricResult(metricEntity, lcc + lcc * ndeg / 4);
        }

        Instant ends = Instant.now();
        logger.debug("finished correction of iterated average neighbour degree ({})", Duration.between(starts, ends));
    }
}
