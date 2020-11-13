package com.coria.v3.metrics.graphstream;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.repository.RepositoryManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Sebastian Gross, 2017
 * Modified by David Fradin, 2020:
 * - moved duplicate code fragments to {@link GSMetricAlgorithmImplementationBase }
 * - moved correction methods into separate classes. See GS[...]Corrected.java files.
 * - rewrote performCalculations() code due to changes in storage of metric results
 * - used HashMaps and Java 8 Streams for efficiency
 */
@Component
public class CoriaUnifiedRiskScore extends GSMetricAlgorithmImplementationBase {
    public CoriaUnifiedRiskScore() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Unified Risk Score").getMetricAlgorithmVariantByName("Default"));
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        logger.debug("calculating {} for dataset {}", getName(), datasetEntity.getId());
        // Remapping dependencies into a logical tree of metric->node->metricResultValue
        HashMap<String, Map<NodeEntity, Double>> dependencyMetricResults = new HashMap<>();
        List<String> dependencyVariantIds = List.of("node-degree--normalised", "average-neighbour-degree--corrected-and-normalised", "iterated-average-neighbour-degree--corrected-and-normalised", "betweenness-centrality--normalised", "eccentricity--normalised", "average-shortest-path-length--normalised");
        for (var metricVariantId : dependencyVariantIds) {
            dependencyMetricResults.put(metricVariantId, GSUtility.getNodeMetricResultsMap(repositoryManager, dependencyMetricIds.get(metricVariantId)));
        }

        for (NodeEntity n0 : datasetEntity.getNodes()) {
            double ndeg = dependencyMetricResults.get("node-degree--normalised").get(n0);
            double and = dependencyMetricResults.get("average-neighbour-degree--corrected-and-normalised").get(n0);
            double iand = dependencyMetricResults.get("iterated-average-neighbour-degree--corrected-and-normalised").get(n0);
            double bc = dependencyMetricResults.get("betweenness-centrality--normalised").get(n0);
            double ecc = dependencyMetricResults.get("eccentricity--normalised").get(n0);
            double aspl = dependencyMetricResults.get("average-shortest-path-length--normalised").get(n0);

            n0.addMetricResult(metricEntity,
                    0.25 * ndeg +
                            0.15 * and +
                            0.1 * iand +
                            0.25 * bc +
                            0.125 * ecc +
                            0.125 * aspl
            );
        }
    }
}
