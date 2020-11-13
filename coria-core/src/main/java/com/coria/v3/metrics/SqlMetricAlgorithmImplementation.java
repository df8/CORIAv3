package com.coria.v3.metrics;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.repository.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
public class SqlMetricAlgorithmImplementation extends MetricAlgorithmImplementation {
    protected final static Logger logger = LoggerFactory.getLogger(SqlMetricAlgorithmImplementation.class);

    public SqlMetricAlgorithmImplementation(MetricAlgorithmVariant metricAlgorithmVariant) throws Exception {
        super(
                "SQL Query",
                "D. Fradin / MariaDB",
                "This metric is implemented in SQL code and is executed directly on a MariaDB server instance. " +
                        "<h4>Advantages</h4><ul><li>Since the computations are executed relatively close to the data storage, we minimize the amount of comparably slow I/O operations.</li>" +
                        "<li>Additionally we benefit from algorithmic optimizations that are readily available within the relational database management system (RDBMS).[1]</li>" +
                        "</ul>" +
                        "<h4>Disadvantages</h4>" +
                        "<ul>" +
                        "<li>As multi-threading is not supported in MariaDB, the computations are utilizing only a single CPU core.</li>" +
                        "<li>While SQL is a universal query language supported by a range of RDBMS, the implementation provided by D. Fradin supports only MariaDB. " +
                        "Other RDBMS such as MySQL or Microsoft SQL Server have not yet implemented newer window functions such as <a href=\"https://mariadb.com/kb/en/median/\"><code>MEDIAN(column)</code></a>.</li>" +
                        "</ul>" +
                        "[1] For a list of optimization methods available in the current MariaDB release, see https://mariadb.com/kb/en/query-optimizations/.",
                30,
                metricAlgorithmVariant,
                true,
                null);
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity dataset, MetricEntity metric, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        performCalculations(this.metricAlgorithmVariant, repositoryManager, dataset, metric, dependencyMetricIds);
    }

    protected static void performCalculations(MetricAlgorithmVariant metricAlgorithmVariant, RepositoryManager repositoryManager, DatasetEntity dataset, MetricEntity metric, Map<String, UUID> dependencyMetricIds) throws Exception {
        boolean implementationFound = true;

        switch (metricAlgorithmVariant.getMetricAlgorithm().getName()) {
            case "Average Node Degree":
                if (metricAlgorithmVariant.getName().equals("Default")) {
                    repositoryManager.getDatasetRepository().calculateAverageNodeDegree(dataset.getId(), metric.getId());
                } else {
                    implementationFound = false;
                }
                break;
            case "Average Neighbour Degree":
                switch (metricAlgorithmVariant.getName()) {
                    case "Default":
                        repositoryManager.getDatasetRepository().calculateAverageNeighbourDegree(dataset.getId(), metric.getId(), dependencyMetricIds.get("node-degree--default"));
                        break;
                    case "Corrected":
                        repositoryManager.getDatasetRepository().calculateAverageNeighbourDegreeCorrected(dataset.getId(), metric.getId(),
                                dependencyMetricIds.get("node-degree--default"));
                        break;
                    case "Corrected and Normalised":
                        repositoryManager.getDatasetRepository().calculateNodeMetricMinMaxNormalised(metric.getId(),
                                dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Corrected").getId()));
                        break;
                    default:
                        implementationFound = false;
                        break;
                }
                break;
            case "Average Shortest Path Length":
                switch (metricAlgorithmVariant.getName()) {
                    case "Default":
                        if (repositoryManager.getShortestPathLengthRepository().countByMetric_Dataset_Id(dataset.getId()) == 0) {
                            throw new Exception("Metric \"AverageShortestPathLength\" depends on the results of the metric \"ShortestPathLength\". Please run the metric \"ShortestPathLength\" first before you launch metric \"AverageShortestPathLength\".");
                        }
                        repositoryManager.getDatasetRepository().calculateAverageShortestPathLength(metric.getId(), getShortestPathLengthMetric(repositoryManager, dataset).getId());
                        break;
                    case "Normalised":
                        repositoryManager.getDatasetRepository().calculateNodeMetricMaxMinNormalised(metric.getId(), dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Default").getId()));
                        break;
                    default:
                        implementationFound = false;
                        break;
                }
                break;
            case "Betweenness Centrality":
                if (metricAlgorithmVariant.getName().equals("Normalised")) {
                    repositoryManager.getDatasetRepository().calculateNodeMetricMinMaxNormalised(metric.getId(), dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Default").getId()));
                } else {
                    implementationFound = false;
                }
                break;
            case "Eccentricity":
                switch (metricAlgorithmVariant.getName()) {
                    case "Default":
                        if (repositoryManager.getShortestPathLengthRepository().countByMetric_Dataset_Id(dataset.getId()) == 0) {
                            throw new Exception("Metric \"Eccentricity\" depends on the results of the metric \"ShortestPathLength\". Please run metric \"ShortestPathLength\" first before you launch metric \"Eccentricity\".");
                        }
                        repositoryManager.getDatasetRepository().calculateEccentricity(metric.getId(), getShortestPathLengthMetric(repositoryManager, dataset).getId());
                        break;
                    case "Normalised":
                        repositoryManager.getDatasetRepository().calculateNodeMetricMaxMinNormalised(metric.getId(), dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Default").getId()));
                        break;
                    default:
                        implementationFound = false;
                        break;
                }
                break;
            case "Iterated Average Neighbour Degree":
                switch (metricAlgorithmVariant.getName()) {
                    case "Default":
                        // Node Degree must be calculated before calculating Iterated Average Neighbour Degree.
                        repositoryManager.getDatasetRepository().calculateIteratedAverageNeighbourDegree(dataset.getId(), metric.getId(), dependencyMetricIds.get("node-degree--default"));
                        break;
                    case "Corrected":
                        repositoryManager.getDatasetRepository().calculateIteratedAverageNeighbourDegreeCorrected(dataset.getId(), metric.getId(), dependencyMetricIds.get("node-degree--default"));
                        break;
                    case "Corrected and Normalised":
                        repositoryManager.getDatasetRepository().calculateNodeMetricMinMaxNormalised(metric.getId(), dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Corrected").getId()));
                        break;
                    default:
                        implementationFound = false;
                        break;
                }
                break;
            case "Local Clustering Coefficients":
                switch (metricAlgorithmVariant.getName()) {
                    case "Default":
                        //TODO /3 broken metric
                        throw new Exception("Not implemented yet");
                        //repositoryManager.getDatasetRepository().calculateLocalClusteringCoefficients(dataset.getId(), metric.getId(), dependencyMetricIds.get("node-degree--default"));
                        //break;
                    case "Corrected":
                        repositoryManager.getDatasetRepository().calculateLocalClusteringCoefficientsCorrected(metric.getId(),
                                dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Default").getId()),
                                dependencyMetricIds.get("node-degree--default"));
                        break;
                    case "Corrected and Normalised":
                        repositoryManager.getDatasetRepository().calculateNodeMetricMinMaxNormalised(metric.getId(),
                                dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Corrected").getId()));
                        break;
                    default:
                        implementationFound = false;
                        break;
                }
                break;
            case "Node Degree":
                switch (metricAlgorithmVariant.getName()) {
                    case "Default":
                        repositoryManager.getDatasetRepository().calculateNodeDegree(dataset.getId(), metric.getId());
                        break;
                    case "Normalised":
                        repositoryManager.getDatasetRepository().calculateNodeMetricMinMaxNormalised(metric.getId(), dependencyMetricIds.get("node-degree--default"));
                        break;
                    default:
                        implementationFound = false;
                        break;
                }
                break;
            case "Unified Risk Score":
                switch (metricAlgorithmVariant.getName()) {
                    case "Default":
                        repositoryManager.getDatasetRepository().calculateUnifiedRiskScore(dataset.getId(), metric.getId());
                        break;
                    case "Normalised":
                        repositoryManager.getDatasetRepository().calculateNodeMetricMinMaxNormalised(metric.getId(),
                                dependencyMetricIds.get(metricAlgorithmVariant.getMetricAlgorithm().getMetricAlgorithmVariantByName("Default").getId()));
                        break;
                    default:
                        implementationFound = false;
                        break;
                }
                break;
            default:
                implementationFound = false;
        }
        if (!implementationFound) {
            throw new Exception("No SQL Query implementation found for algorithm: " + metricAlgorithmVariant.getMetricAlgorithm().getName() + ", variant: " + metricAlgorithmVariant.getName());
        }
    }

    //TODO /2 run through performCalculations process automatically
    protected static MetricEntity getShortestPathLengthMetric(RepositoryManager repositoryManager, DatasetEntity dataset) {
        //Check if there are shortest path length results in DB
        //return repositoryManager.getMetricRepository().findFirstByDataset_IdAndMetricAlgorithmImplementation_MetricAlgorithm_NameAndMetricAlgorithmImplementation_MetricAlgorithmVariant_NameAndStatus(dataset.getId(), "Shortest Path Lengths", "Default", MetricEntity.MetricStatus.FINISHED).orElseThrow();
        return repositoryManager.getMetricRepository().findAllByDataset_IdAndStatus(dataset.getId(), MetricEntity.MetricStatus.FINISHED)
                .stream().filter(metricEntity1 ->
                        metricEntity1.getMetricAlgorithm().getName().equals("Shortest Path Lengths") &&
                                metricEntity1.getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getName().equals("Default")
                ).findFirst().orElseThrow();
    }
}
