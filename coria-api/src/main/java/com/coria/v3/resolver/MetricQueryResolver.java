package com.coria.v3.resolver;

import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.model.ListMetadata;
import com.coria.v3.model.MetricFilter;
import graphql.ErrorType;
import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for MetricEntity.
 */
@Component
public class MetricQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    public Page<MetricEntity> allMetrics(int page, int perPage, String sortField, String sortOrder, MetricFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");
        if (sortField.equals("metricAlgorithmImplementation.id"))
            sortField = "metricAlgorithmImplementationId";
        return repositoryManager.getMetricRepository().findAllByDataset_Id(filter.getDatasetId(), toPageable(page, perPage, sortField, sortOrder));
    }

    public ListMetadata _allMetricsMeta(int page, int perPage, String sortField, String sortOrder, MetricFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");
        return new ListMetadata(repositoryManager.getMetricRepository().countByDataset_Id(filter.getDatasetId()));
    }

    public MetricEntity getMetric(String id) {
        return repositoryManager.getMetricRepository().findById(UUID.fromString(id)).orElseThrow(
                GraphqlErrorException
                        .newErrorException()
                        .message("Metric not found")
                        .errorClassification(ErrorType.DataFetchingException)
                        ::build
        );
    }
}
