package com.coria.v3.metrics;

import com.coria.v3.config.AppContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David Fradin, 2020
 */
@Component
public class SqlMetricAlgorithmImplementationFactory implements MetricAlgorithmImplementationFactory {
    @Override
    public List<? extends MetricAlgorithmImplementation> getList() throws Exception {
        ArrayList<MetricAlgorithmImplementation> result = new ArrayList<>();

        result.add(new SqlMetricAlgorithmImplementation(AppContext.getInstance().getMetricAlgorithmByName("Betweenness Centrality").getMetricAlgorithmVariantByName("Normalised")));

        result.add(new SqlMetricAlgorithmImplementation(AppContext.getInstance().getMetricAlgorithmByName("Average Node Degree").getMetricAlgorithmVariantByName("Default")));

        MetricAlgorithm maAverageNeighbourDegree = AppContext.getInstance().getMetricAlgorithmByName("Average Neighbour Degree");
        result.add(new SqlMetricAlgorithmImplementation(maAverageNeighbourDegree.getMetricAlgorithmVariantByName("Default")));
        result.add(new SqlMetricAlgorithmImplementation(maAverageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected")));
        result.add(new SqlMetricAlgorithmImplementation(maAverageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected and Normalised")));

        MetricAlgorithm maLocalClusteringCoefficients = AppContext.getInstance().getMetricAlgorithmByName("Local Clustering Coefficients");
        // TODO /3 broken code
        // result.add(new SqlMetricAlgorithmImplementation(maLocalClusteringCoefficients.getMetricAlgorithmVariantByName("Default")));
        result.add(new SqlMetricAlgorithmImplementation(maLocalClusteringCoefficients.getMetricAlgorithmVariantByName("Corrected")));
        result.add(new SqlMetricAlgorithmImplementation(maLocalClusteringCoefficients.getMetricAlgorithmVariantByName("Corrected and Normalised")));

        MetricAlgorithm maIteratedAverageNeighbourDegree = AppContext.getInstance().getMetricAlgorithmByName("Iterated Average Neighbour Degree");
        result.add(new SqlMetricAlgorithmImplementation(maIteratedAverageNeighbourDegree.getMetricAlgorithmVariantByName("Default")));
        result.add(new SqlMetricAlgorithmImplementation(maIteratedAverageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected")));
        result.add(new SqlMetricAlgorithmImplementation(maIteratedAverageNeighbourDegree.getMetricAlgorithmVariantByName("Corrected and Normalised")));

        MetricAlgorithm maAverageShortestPathLength = AppContext.getInstance().getMetricAlgorithmByName("Average Shortest Path Length");
        result.add(new SqlMetricAlgorithmImplementation(maAverageShortestPathLength.getMetricAlgorithmVariantByName("Default")));
        result.add(new SqlMetricAlgorithmImplementation(maAverageShortestPathLength.getMetricAlgorithmVariantByName("Normalised")));

        MetricAlgorithm maEccentricity = AppContext.getInstance().getMetricAlgorithmByName("Eccentricity");
        result.add(new SqlMetricAlgorithmImplementation(maEccentricity.getMetricAlgorithmVariantByName("Default")));
        result.add(new SqlMetricAlgorithmImplementation(maEccentricity.getMetricAlgorithmVariantByName("Normalised")));

        MetricAlgorithm maNodeDegree = AppContext.getInstance().getMetricAlgorithmByName("Node Degree");
        result.add(new SqlMetricAlgorithmImplementation(maNodeDegree.getMetricAlgorithmVariantByName("Default")));
        result.add(new SqlMetricAlgorithmImplementation(maNodeDegree.getMetricAlgorithmVariantByName("Normalised")));

        MetricAlgorithm maUnifiedRiskScore = AppContext.getInstance().getMetricAlgorithmByName("Unified Risk Score");
        result.add(new SqlMetricAlgorithmImplementation(maUnifiedRiskScore.getMetricAlgorithmVariantByName("Default")));
        //result.add(new SqlMetricAlgorithmImplementation(maUnifiedRiskScore.getMetricAlgorithmVariantByName("Normalised")));
        //TODO test URS
        return result;
    }
}
