package com.coria.v3.resolver;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import com.coria.v3.metrics.MetricAlgorithmVariant;
import com.coria.v3.repository.RepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * Created by David Fradin, 2020
 */
@Service
public class MetricComputationBean {
    private final Logger logger = LoggerFactory.getLogger(MetricComputationBean.class);
    private RepositoryManager repositoryManager;

    @Autowired
    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    /**
     * scheduleDependentMetrics is a recursive function that schedules all required metrics based on dependencies.
     *
     * @return
     * @throws Exception
     */
    @Transactional
    public MetricEntity scheduleDependentMetrics(
            DatasetEntity datasetEntity,
            MetricAlgorithmVariant metricAlgorithmVariantObj,
            MetricAlgorithmImplementation metricAlgorithmImplementationObj,
            HashMap<String, MetricEntity> dependencyMetricIds,
            Queue<MetricEntity> scheduledMetrics) throws Exception {
        if (metricAlgorithmVariantObj.getDependencies() != null) {
            logger.debug("[Metric Resolver] Searching and computing all dependencies of {}", metricAlgorithmVariantObj.getId());
            for (MetricAlgorithmVariant mav1 : metricAlgorithmVariantObj.getDependencies()) {
                scheduleDependentMetrics(datasetEntity, mav1, null, dependencyMetricIds, scheduledMetrics);
            }
        }
        MetricEntity metricEntity = null;

        //First check whether this metric variant has already been scheduled
        if (metricAlgorithmImplementationObj == null) {
            if (dependencyMetricIds.containsKey(metricAlgorithmVariantObj.getId())) {
                return dependencyMetricIds.get(metricAlgorithmVariantObj.getId());
            }
            //Second check if this metric variant has already been computed
            metricEntity = repositoryManager
                    .getMetricRepository()
                    .findAllByDataset_IdAndStatus(datasetEntity.getId(), MetricEntity.MetricStatus.FINISHED)
                    .stream()
                    .filter(metricEntity1 ->
                            metricEntity1
                                    .getMetricAlgorithm()
                                    .getName()
                                    .equals(metricAlgorithmVariantObj.getMetricAlgorithm().getName()) &&
                                    metricEntity1
                                            .getMetricAlgorithmImplementation()
                                            .getMetricAlgorithmVariant()
                                            .getName()
                                            .equals(metricAlgorithmVariantObj.getName())
                    )
                    .findFirst()
                    .orElse(null);

            if (metricEntity == null) {
                //If we need to compute this dependency now, search for the fastest implementation of this MAV:
                metricAlgorithmImplementationObj = metricAlgorithmVariantObj
                        .getImplementations()
                        .stream()
                        .filter(MetricAlgorithmImplementation::isAvailable)
                        .max(Comparator.comparing(MetricAlgorithmImplementation::getSpeedIndex))
                        .orElseThrow(() -> new Exception("No available implementations for " + metricAlgorithmVariantObj.getId()));
            }
        }
        if (metricEntity == null) {
            metricEntity = new MetricEntity(
                    metricAlgorithmVariantObj,
                    metricAlgorithmImplementationObj,
                    Timestamp.from(Instant.now()),
                    MetricEntity.MetricStatus.SCHEDULED,
                    datasetEntity);
            System.out.println("\tSAVING " + metricAlgorithmImplementationObj.getId());
            repositoryManager.getMetricRepository().save(metricEntity);
            repositoryManager.getDatasetRepository().save(datasetEntity);
            repositoryManager.getEntityManager().merge(metricEntity);
            repositoryManager.getEntityManager().merge(datasetEntity);
            repositoryManager.getEntityManager().flush();
            scheduledMetrics.add(metricEntity);
        }
        dependencyMetricIds.put(metricAlgorithmVariantObj.getId(), metricEntity);
        return metricEntity;
    }

    @Transactional(propagation = Propagation.NESTED)
    public void executeMetric(
            UUID datasetId,
            UUID metricEntityId,
            Map<String, UUID> dependencyMetricIds,
            List<HashMap<String, String>> parameters) throws Exception {
        DatasetEntity datasetEntity = repositoryManager.getDatasetRepository().findById(datasetId).orElseThrow();
        Optional<MetricEntity> x = repositoryManager.getMetricRepository().findById(metricEntityId);
        if (x.isEmpty()) {
            throw new Exception("Metric with ID " + metricEntityId + " not found in DB.");
        }
        MetricEntity metricEntity = x.get();
        try {
            //System.out.println(TransactionSynchronizationManager.isActualTransactionActive());

            //logger.debug("[METRIC QUEUE]\t{}", metricEntity.getMetricAlgorithmImplementation().getId());
            metricEntity.setStarted(Timestamp.from(Instant.now()));
            metricEntity.setStatus(MetricEntity.MetricStatus.RUNNING);
            repositoryManager.getMetricRepository().save(metricEntity);

            metricEntity.setStarted(Timestamp.from(Instant.now()));
            metricEntity.getMetricAlgorithmImplementation().performComputation(repositoryManager, datasetEntity, metricEntity, dependencyMetricIds, parameters);
            metricEntity.setFinished(Timestamp.from(Instant.now()));
            metricEntity.setStatus(MetricEntity.MetricStatus.FINISHED);
            repositoryManager.getMetricRepository().save(metricEntity);
            logger.debug("Finished execution {}  for {}", metricEntity.getMetricAlgorithmImplementation().getId(), datasetEntity.getName());

        } catch (Exception e) {
            logger.error("Calculation error: {} in {}", e.getMessage(), e.getStackTrace()[0].toString());
            metricEntity.setFinished(Timestamp.from(Instant.now()));
            metricEntity.setStatus(MetricEntity.MetricStatus.FAILED);
            metricEntity.setMessage("Metric calculation error: " + e.getMessage());
            repositoryManager.getMetricRepository().saveAndFlush(metricEntity);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.NESTED)
    public void executeMultipleMetrics(
            UUID datasetId,
            String metricMultiAlgorithmImplementationId,
            List<UUID> metricEntityIds,
            Map<String, UUID> dependencyMetricIds,
            List<HashMap<String, String>> parameters) {
        DatasetEntity datasetEntity = repositoryManager.getDatasetRepository().findById(datasetId).orElseThrow();
        List<MetricEntity> metricList = repositoryManager.getMetricRepository().findAllById(metricEntityIds);
        metricList.sort(Comparator.comparingInt(m -> metricEntityIds.indexOf(m.getId())));//Restore the initial order where metrics with "less" dependencies come first.

        try {
            //logger.debug("[METRIC QUEUE]\t{}", metricEntity.getMetricAlgorithmImplementation().getId());
            for (var metricEntity : metricList) {
                metricEntity.setStarted(Timestamp.from(Instant.now()));
                metricEntity.setStatus(MetricEntity.MetricStatus.RUNNING);
                repositoryManager.getMetricRepository().save(metricEntity);
                metricEntity.setStarted(Timestamp.from(Instant.now()));
            }
            AppContext.getInstance().getMetricMultiAlgorithmImplementation(metricMultiAlgorithmImplementationId).performComputations(repositoryManager, datasetEntity, metricList, dependencyMetricIds, parameters);
            for (var metricEntity : metricList) {
                if (metricEntity.getFinished() == null) {
                    metricEntity.setFinished(Timestamp.from(Instant.now()));
                }
                metricEntity.setStatus(MetricEntity.MetricStatus.FINISHED);
            }
            for (var metricEntity : metricList) {
                repositoryManager.getMetricRepository().save(metricEntity);
                logger.debug("Finished execution {}  for {}", metricEntity.getMetricAlgorithmImplementation().getId(), datasetEntity.getName());
            }


        } catch (Exception e) {
            logger.error("Calculation error: {} in {}", e.getMessage(), e.getStackTrace()[0].toString());
            for (var metricEntity : metricList) {
                metricEntity.setFinished(Timestamp.from(Instant.now()));
                metricEntity.setStatus(MetricEntity.MetricStatus.FAILED);
                metricEntity.setMessage("Metric calculation error: " + e.getMessage());
            }
            for (var metricEntity : metricList) {
                repositoryManager.getMetricRepository().saveAndFlush(metricEntity);
            }
        }
    }
}
