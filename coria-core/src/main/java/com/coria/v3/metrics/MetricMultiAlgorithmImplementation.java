package com.coria.v3.metrics;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.repository.RepositoryManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * <p>
 * This interface is implemented by classes that represent algorithms capable to compute multiple CORIA metrics at once.
 * Also see MetricAlgorithmImplementation.java for the single-computation algorithm class.
 */
public interface MetricMultiAlgorithmImplementation {

    String getId();

    /**
     * Defines the API to calculate a number of metrics based on a DatasetEntity object and a list of MetricEntity objects.
     */
    void performComputations(RepositoryManager repositoryManager, DatasetEntity datasetEntity, List<MetricEntity> metricEntityList, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception;
}
