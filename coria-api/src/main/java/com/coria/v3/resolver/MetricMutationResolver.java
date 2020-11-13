package com.coria.v3.resolver;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import graphql.ErrorType;
import graphql.kickstart.tools.GraphQLMutationResolver;
import graphql.schema.DataFetchingEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileSystemNotFoundException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL mutation requests for MetricEntity.
 */
@Component
public class MetricMutationResolver extends BaseResolver implements GraphQLMutationResolver {

    private final Logger logger = LoggerFactory.getLogger(MetricMutationResolver.class);

    ThreadPoolTaskExecutor taskExecutor;

    private MetricComputationBean metricComputationBean;

    @Qualifier("getThreadPoolTaskExecutor")
    @Autowired
    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Autowired
    public void setMetricComputationBean(MetricComputationBean metricComputationBean) {
        this.metricComputationBean = metricComputationBean;
    }

    final boolean asyncExecution = true;

    /*
     * How to stream MySQL query rows one by one in Java:
     * http://knes1.github.io/blog/2015/2015-10-19-streaming-mysql-results-using-java8-streams-and-spring-data.html
     */

    /**
     * Handles the GraphQL request to execute a metric.
     * This function creates multiple metrics and schedules a queue if there are open dependencies.
     * The function returns right after scheduling the metrics and does not wait until all computations are completed.
     *
     * @param datasetId                     The dataset UUID to compute the metric on.
     * @param metricAlgorithm               MetricAlgorithm ID to compute
     * @param metricAlgorithmVariant        MetricAlgorithmVariant ID to compute
     * @param metricAlgorithmImplementation MetricAlgorithmImplementation ID to compute
     * @param environment                   Request object
     * @return returns the scheduled MetricEntity object
     * @throws Exception thrown when the computation fails for various reasons.
     */
    @Modifying
    @Transactional
    public MetricEntity createMetric(UUID datasetId, String metricAlgorithm, String metricAlgorithmVariant, String metricAlgorithmImplementation, List<HashMap<String, String>> parameters, DataFetchingEnvironment environment) throws Exception {
        logger.debug("starting metricAlgorithmImplementation {} on dataset {}", metricAlgorithmImplementation, datasetId);
        DatasetEntity datasetEntity = repositoryManager.getDatasetRepository().findById(datasetId).orElseThrow();
        MetricAlgorithmImplementation metricAlgorithmImplementationObj = null;

        try {
            Thread.currentThread().setName(metricAlgorithmImplementation);
            logger.debug("begin metricAlgorithmImplementation execution for {}", metricAlgorithmImplementation);

            metricAlgorithmImplementationObj = appContext.getMetricAlgorithmImplementation(metricAlgorithmImplementation);
            if (metricAlgorithmImplementationObj == null)
                throw buildException("Selected metric module not found.");
            HashMap<String, MetricEntity> dependencyMetricIds = new HashMap<>();
            LinkedList<MetricEntity> scheduledMetrics = new LinkedList<>();
            MetricEntity metricEntity = metricComputationBean.scheduleDependentMetrics(
                    datasetEntity,
                    metricAlgorithmImplementationObj.getMetricAlgorithmVariant(),
                    metricAlgorithmImplementationObj,
                    dependencyMetricIds,
                    scheduledMetrics
            );
            System.out.println("*** EXECUTION PLAN ***");
            int i = 0;
            for (MetricEntity me : scheduledMetrics) {
                System.out.println(++i + "\t" + me.getId() + "\t" + me.getMetricAlgorithmImplementationId());
            }
            System.out.println();

            runMetrics(datasetId, dependencyMetricIds, scheduledMetrics, parameters);
            return metricEntity;
        } catch (Exception e) {
            if (metricAlgorithmImplementationObj != null) {

                MetricEntity metricEntity = new MetricEntity(
                        metricAlgorithmImplementationObj.getMetricAlgorithmVariant(),
                        metricAlgorithmImplementationObj,
                        Timestamp.from(Instant.now()),
                        MetricEntity.MetricStatus.FAILED,
                        datasetEntity);
                metricEntity.setFinished(Timestamp.from(Instant.now()));
                metricEntity.setMessage(e.getMessage());
                datasetEntity.addMetric(metricEntity);
                repositoryManager.getMetricRepository().saveAndFlush(metricEntity);
                repositoryManager.getDatasetRepository().saveAndFlush(datasetEntity);

                //something went wrong while executing the metricAlgorithmImplementation
                if (e instanceof FileSystemNotFoundException) {
                    //this usually happens when the module resources were not copied into coria-api resources
                    //if this is the case please perform a maven install on the root project
                    metricEntity.setMessage("Could not find required file resource for metric execution. Please contact your system administrator and check if all resources are in place.");
                } else {
                    metricEntity.setMessage(e.getMessage());
                }
                repositoryManager.getMetricRepository().save(metricEntity);
                return metricEntity;
            } else {
                e.printStackTrace();
                throw buildException("Error while calculating metric: " + e.getMessage(), ErrorType.ValidationError);
            }
        }
    }

    void runMetrics(UUID datasetId, HashMap<String, MetricEntity> dependencyMetrics, Queue<MetricEntity> scheduledMetrics, List<HashMap<String, String>> parameters) throws Exception {
        Map<String, UUID> dependencyMetricIds = dependencyMetrics.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getId()));
        if (asyncExecution) {
            taskExecutor.submit(() -> {
                MetricEntity metricEntity1 = null;
                try {
                    Thread.sleep(500);
                    // Special case optimization:
                    // For large datasets, if multiple metrics are required to be computed in Python together in one execution plan,
                    // it is faster to compute all metrics in one Python run and also to completely skip writing SPL-distances into DB.
                    ArrayList<UUID> pythonMetrics = new ArrayList<>();
                    for (MetricEntity m : scheduledMetrics) {
                        if (m.getMetricAlgorithmImplementationId().contains("--python3")) {
                            pythonMetrics.add(m.getId());
                        } else
                            break;
                    }

                    boolean pythonMetricsAlreadyExecuted = false;
                    while (!scheduledMetrics.isEmpty()) {
                        metricEntity1 = scheduledMetrics.remove();
                        String maiId = metricEntity1.getMetricAlgorithmImplementation().getId();
                        if (maiId.contains("--python3") && !pythonMetricsAlreadyExecuted) {
                            pythonMetricsAlreadyExecuted = true;
                            metricComputationBean.executeMultipleMetrics(datasetId,
                                    maiId.contains("cugraph") ?
                                            "multi--python3-c-cuda--rapids-cugraph" :
                                            "multi--python3--networkx",
                                    pythonMetrics, dependencyMetricIds, parameters);
                        } else if (!pythonMetrics.contains(metricEntity1.getId())) {
                            //System.out.println("QUEUE: " + metricEntity1.getId() + " " + metricEntity1.getMetricAlgorithmImplementation().getId());
                            metricComputationBean.executeMetric(datasetId, metricEntity1.getId(), dependencyMetricIds, parameters);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (metricEntity1 != null) {
                        metricEntity1 = repositoryManager.getMetricRepository().findById(metricEntity1.getId()).orElse(null);
                        if (metricEntity1 != null) {
                            metricEntity1.setStatus(MetricEntity.MetricStatus.FAILED);
                            metricEntity1.setFinished(Timestamp.from(Instant.now()));
                            metricEntity1.setMessage(e.getMessage());
                        }
                    }
                    while (!scheduledMetrics.isEmpty()) {
                        metricEntity1 = scheduledMetrics.remove();
                        metricEntity1 = repositoryManager.getMetricRepository().findById(metricEntity1.getId()).orElse(null);
                        if (metricEntity1 != null) {
                            metricEntity1.setStatus(MetricEntity.MetricStatus.FAILED);
                            metricEntity1.setFinished(Timestamp.from(Instant.now()));
                            metricEntity1.setMessage("A preceding metric computation failed.");
                        }
                    }
                }
            });
        } else {
            while (!scheduledMetrics.isEmpty()) {
                MetricEntity metricEntity1 = scheduledMetrics.remove();
                //System.out.println("QUEUE: " + metricEntity1.getId() + " " + metricEntity1.getMetricAlgorithmImplementation().getId());
                metricComputationBean.executeMetric(datasetId, metricEntity1.getId(), dependencyMetricIds, parameters);
            }
        }
    }

    /**
     * Handles the GraphQL request to delete a MetricEntity including all related metric results.
     *
     * @param metricId MetricEntity UUID
     * @return the removed DatasetEntity
     */
    public MetricEntity deleteMetric(UUID metricId) {
        MetricEntity metricEntity = repositoryManager.getMetricRepository().findById(metricId).orElseThrow();
        repositoryManager.getMetricRepository().delete(metricEntity);
        return metricEntity;
    }
}
