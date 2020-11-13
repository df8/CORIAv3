package com.coria.v3.metrics.graphstream;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.metrics.MetricAlgorithmVariant;
import com.coria.v3.repository.RepositoryManager;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * David Fradin, 2020:
 * A single class to calculate the min-max 'normalised' [and max-min] variant of any metric
 */

public class GSGenericMetricAlgorithmImplementationNormalised extends GSMetricAlgorithmImplementationBase {

    private final String inputMetricAlgorithmVariantId;
    private final boolean minMaxNormalisation;

    public GSGenericMetricAlgorithmImplementationNormalised(MetricAlgorithmVariant metricAlgorithmVariant, String inputMetricAlgorithmVariantId, boolean minMaxNormalisation) throws Exception {
        super(metricAlgorithmVariant);
        this.inputMetricAlgorithmVariantId = inputMetricAlgorithmVariantId;
        this.minMaxNormalisation = minMaxNormalisation;
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
        // Read the results of inputMetricAlgorithmVariantId into a key-value-map
        Map<NodeEntity, Double> inputMetricValuesMap = GSUtility.getNodeMetricResultsMap(repositoryManager, dependencyMetricIds.get(this.inputMetricAlgorithmVariantId));

        if (inputMetricValuesMap.size() == 0) {
            throw new Exception(this.inputMetricAlgorithmVariantId + " is not available.");
        }
        if (inputMetricValuesMap.size() != datasetEntity.getNodes().size()) {
            throw new Exception("Dependency metric results are incorrect for " + this.getId() + ".");
        }

        double min = Collections.min(inputMetricValuesMap.values());
        double max = Collections.max(inputMetricValuesMap.values());

        for (NodeEntity n0 : datasetEntity.getNodes()) {
            if (this.minMaxNormalisation) {
                if (max - min < 1e-6) {
                    n0.addMetricResult(metricEntity, 0);
                } else {
                    n0.addMetricResult(metricEntity, (inputMetricValuesMap.get(n0) - min) / (max - min));
                }
            } else {
                //Max-Min Normalisation
                if (max - min < 1e-6) {
                    n0.addMetricResult(metricEntity, 1);
                } else {
                    n0.addMetricResult(metricEntity, (max - inputMetricValuesMap.get(n0)) / (max - min));
                }
            }
        }

        Instant ends = Instant.now();
        logger.debug("finished correction of {} ({})", inputMetricAlgorithmVariantId, Duration.between(starts, ends));
    }
}
