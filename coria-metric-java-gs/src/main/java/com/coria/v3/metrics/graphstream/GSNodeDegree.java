package com.coria.v3.metrics.graphstream;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.repository.RepositoryManager;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Calculates the Node Degree for a given DatasetEntity
 * using GraphStream Java Library
 * Created by Sebastian Gross, 2017
 * Modified by David Fradin, 2020:
 * - moved duplicate code fragments to {@link GSMetricAlgorithmImplementationBase }
 */
@Component
public class GSNodeDegree extends GSMetricAlgorithmImplementationBase {

    public GSNodeDegree() throws Exception {
        super(AppContext.getInstance().getMetricAlgorithmByName("Node Degree").getMetricAlgorithmVariantByName("Default"));
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        logger.debug("starting {}", this.getId());
        Graph g = createGraphFromDataSet(datasetEntity);
        for (Node n : g) {
            int result = n.getDegree();
            NodeEntity nodeEntity = nodeDictByName.get(n.getId());
            if (nodeEntity == null)
                throw new Exception(String.format("Node with ID %s not found in dataset", n.getId()));
            nodeEntity.addMetricResult(metricEntity, result);
        }
        logger.debug("calculation for {} finished successfully", getName());
    }

    @Override
    public String toString() {
        return "GSNodeDegree{id: " + getId() + ", name: " + getName() + "}";
    }
}
