package com.coria.v3.metrics.graphstream;

import com.coria.v3.config.AppContext;
import com.coria.v3.metrics.MetricAlgorithm;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import com.coria.v3.metrics.MetricAlgorithmImplementationFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Fradin, 2020
 * TODO description
 */
@Component
public class GSMetricAlgorithmImplementationFactory implements MetricAlgorithmImplementationFactory {

    @Override
    public List<? extends MetricAlgorithmImplementation> getList() throws Exception {
        ArrayList<MetricAlgorithmImplementation> result = new ArrayList<>();

        MetricAlgorithm nodeDegree = AppContext.getInstance().getMetricAlgorithmByName("Node Degree");
        result.add(new GSGenericMetricAlgorithmImplementationNormalised(
                nodeDegree.getMetricAlgorithmVariantByName("Normalised"), nodeDegree.getMetricAlgorithmVariantByName("Default").getId(), true));

        MetricAlgorithm averageShortestPathLength = AppContext.getInstance().getMetricAlgorithmByName("Average Shortest Path Length");
        result.add(new GSGenericMetricAlgorithmImplementationNormalised(
                averageShortestPathLength.getMetricAlgorithmVariantByName("Normalised"), averageShortestPathLength.getMetricAlgorithmVariantByName("Default").getId(), false));

        MetricAlgorithm eccentricity = AppContext.getInstance().getMetricAlgorithmByName("Eccentricity");
        result.add(new GSGenericMetricAlgorithmImplementationNormalised(
                eccentricity.getMetricAlgorithmVariantByName("Normalised"), eccentricity.getMetricAlgorithmVariantByName("Default").getId(), false));

        MetricAlgorithm betweennessCentrality = AppContext.getInstance().getMetricAlgorithmByName("Betweenness Centrality");
        result.add(new GSGenericMetricAlgorithmImplementationNormalised(
                betweennessCentrality.getMetricAlgorithmVariantByName("Normalised"), betweennessCentrality.getMetricAlgorithmVariantByName("Default").getId(), true));

        MetricAlgorithm averageNeighbourDegree = AppContext.getInstance().getMetricAlgorithmByName("Average Neighbour Degree");
        result.add(new GSGenericMetricAlgorithmImplementationNormalised(
                averageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected and Normalised"), averageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected").getId(), true));

        MetricAlgorithm iteratedAverageNeighbourDegree = AppContext.getInstance().getMetricAlgorithmByName("Iterated Average Neighbour Degree");
        result.add(new GSGenericMetricAlgorithmImplementationNormalised(
                iteratedAverageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected and Normalised"), iteratedAverageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected").getId(), true));

        MetricAlgorithm localClusteringCoefficients = AppContext.getInstance().getMetricAlgorithmByName("Local Clustering Coefficients");
        result.add(new GSGenericMetricAlgorithmImplementationNormalised(
                localClusteringCoefficients.getMetricAlgorithmVariantByName("Corrected and Normalised"), localClusteringCoefficients.getMetricAlgorithmVariantByName("Corrected").getId(), true));

        return result;
    }
}
