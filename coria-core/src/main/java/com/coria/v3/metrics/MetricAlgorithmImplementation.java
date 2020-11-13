package com.coria.v3.metrics;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.Slugify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Sebastian, 2017 (coria-core/src/main/java/com/bigbasti/coria/metrics/MetricModule.java)
 * Modified by David Fradin, 2020: Expanded into an abstract class, reduced redundancy in all subclasses.
 * Added support for:
 * - SpeedIndex for library speed comparison
 * - hierarchy with MetricAlgorithm, MetricAlgorithmVariant and MetricAlgorithmImplementation
 * - disabling an implementation when not available (i.e. we disable GPU-accelerated metrics when the system does not have a CUDA-compatible GPU)
 */
public abstract class MetricAlgorithmImplementation implements ModuleBase {
    protected final String technology, provider, description;
    protected final int speedIndex;
    protected final MetricAlgorithmVariant metricAlgorithmVariant;
    protected final boolean available;
    protected final String unavailableReason;

    public MetricAlgorithmImplementation(String technology, String provider, String description, int speedIndex, MetricAlgorithmVariant metricAlgorithmVariant, boolean available, String unavailableReason) throws Exception {
        if (metricAlgorithmVariant == null) {
            throw new Exception("Unsupported Metric Algorithm Variant provided.");
        }
        this.technology = technology;
        this.provider = provider;
        this.description = description;
        this.speedIndex = speedIndex;
        this.metricAlgorithmVariant = metricAlgorithmVariant;
        this.metricAlgorithmVariant.addImplementation(this);
        this.available = available;
        this.unavailableReason = unavailableReason;
    }

    @Override
    public String getId() {
        String p = Slugify.toSlug(provider);
        if (p.length() > 16)
            p = p.substring(0, 16);
        return this.metricAlgorithmVariant.getId() + "--" + Slugify.toSlug(technology) + "--" + p;
    }

    public String getTechnology() {
        return technology;
    }

    public String getProvider() {
        return provider;
    }

    public int getSpeedIndex() {
        return speedIndex;
    }

    public MetricAlgorithm getMetricAlgorithm() {
        return metricAlgorithmVariant.getMetricAlgorithm();
    }

    /**
     * User-friendly name to be displayed in the frontend.
     *
     * @return name of parser
     */
    @Override
    public String getName() {
        try {
            return this.metricAlgorithmVariant.getMetricAlgorithm().getName() + ": " + this.metricAlgorithmVariant.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * User manual and technical reference in HTML format to be displayed in the frontend.
     *
     * @return HTML Description of the parser
     */
    @Override
    public String getDescription() {
        return description;
    }

    public MetricAlgorithmType getType() {
        return this.metricAlgorithmVariant.getMetricAlgorithm().getType();
    }

    public MetricAlgorithmVariant getMetricAlgorithmVariant() {
        return metricAlgorithmVariant;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getUnavailableReason() {
        return unavailableReason;
    }

    /**
     * Calculates a metric based on a DatasetEntity object and a MetricEntity object.
     *
     * @param repositoryManager   A RepositoryManager instance
     * @param datasetEntity       The DatasetEntity instance to perform the computations on
     * @param metricEntity        The MetricEntity instance representing this metric computation.
     * @param dependencyMetricIds A mapping of Metric Algorithm Variant IDs to previously computed or MetricEntity UUIDs.
     * @throws Exception Exception is thrown when the computation cannot complete due to an error.
     */
    public abstract void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception;
}
