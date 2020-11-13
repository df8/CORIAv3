package com.coria.v3.metrics.graphstream;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.repository.RepositoryManager;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Sebastian Gross, 2017
 * Modified by David Fradin, 2020:
 * - moved duplicate code fragments to {@link GSMetricAlgorithmImplementationBase }
 */
@Component
public class GSAverageClusteringCoefficient extends GSMetricAlgorithmImplementationBase {

    public GSAverageClusteringCoefficient() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Average Clustering Coefficient").getMetricAlgorithmVariantByName("Default"));
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) {
        logger.debug("starting {}", this.getId());
        Graph g = createGraphFromDataSet(datasetEntity);
        double result = Toolkit.averageClusteringCoefficient(g);
        datasetEntity.addMetricResult(metricEntity, result);
        logger.debug("calculation for {} finished successfully - result: {}", this.getId(), result);
    }

    @Override
    public String toString() {
        return "GSAverageClusteringCoefficient{id: " + getId() + ", name: " + getName() + "}";
    }
}
