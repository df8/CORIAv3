package com.coria.v3.metrics.graphstream;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.*;
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
 */
@Component
public class GSEccentricity extends GSMetricAlgorithmImplementationBase {

    public GSEccentricity() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Eccentricity").getMetricAlgorithmVariantByName("Default"));
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) {
        List<ShortestPathLengthEntity> shortestPathLengths = repositoryManager.getShortestPathLengthRepository().findAllByMetric_Id(dependencyMetricIds.get("shortest-path-lengths--default"));
        HashMap<NodeEntity, NodeMetricResultEntity> eccentricity = new HashMap<>();
        for (ShortestPathLengthEntity s : shortestPathLengths) {
            if (!eccentricity.containsKey(s.getNodeSource())) {
                eccentricity.put(s.getNodeSource(), new NodeMetricResultEntity(s.getDistance(), metricEntity, s.getNodeSource()));
            } else {
                NodeMetricResultEntity nmr1 = eccentricity.get(s.getNodeSource());
                if (nmr1.getValue() < s.getDistance()) {
                    nmr1.setValue(s.getDistance());
                }
            }

            if (!eccentricity.containsKey(s.getNodeTarget())) {
                eccentricity.put(s.getNodeTarget(), new NodeMetricResultEntity(s.getDistance(), metricEntity, s.getNodeTarget()));
            } else {
                NodeMetricResultEntity nmr2 = eccentricity.get(s.getNodeTarget());
                if (nmr2.getValue() < s.getDistance()) {
                    nmr2.setValue(s.getDistance());
                }
            }
        }
        metricEntity.getNodeMetricResults().addAll(eccentricity.values());
    }

    @Override
    public String toString() {
        return "GSEccentricity{id: " + getId() + ", name: " + getName() + "}";
    }
}
