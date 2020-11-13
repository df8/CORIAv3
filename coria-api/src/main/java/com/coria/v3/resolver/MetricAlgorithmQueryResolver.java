package com.coria.v3.resolver;

import com.coria.v3.config.AppContext;
import com.coria.v3.metrics.MetricAlgorithm;
import com.coria.v3.model.ListMetadata;
import com.coria.v3.model.MetricAlgorithmFilter;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for MetricAlgorithm.
 */
@Component
@SuppressWarnings("unused")
public class MetricAlgorithmQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    public List<MetricAlgorithm> allMetricAlgorithms(int page, int perPage, String sortField, String sortOrder, MetricAlgorithmFilter filter) {

        return AppContext.getInstance().getMetricAlgorithms()
                .values()
                .stream()
                .filter(ma -> filter == null || (filter.getName() == null || ma.getName().contains(filter.getName())) && (filter.getType() == null || ma.getType().equals(filter.getType())))
                .sorted(Comparator.comparing(MetricAlgorithm::getName))
                .sorted(Comparator.comparing(MetricAlgorithm::getType))
                .collect(Collectors.toList());
    }

    public ListMetadata _allMetricAlgorithmsMeta(int page, int perPage, String sortField, String sortOrder, MetricAlgorithmFilter filter) {
        return new ListMetadata(AppContext.getInstance().getMetricAlgorithms().values().stream().filter(ma -> (filter.getName() == null || ma.getName().contains(filter.getName())) && (filter.getType() == null || ma.getType().equals(filter.getType()))).count());
    }

    public MetricAlgorithm getMetricAlgorithm(String id) {
        MetricAlgorithm ma = null;
        try {
            ma = AppContext.getInstance().getMetricAlgorithmByName(id);
        } catch (Exception ignored) {
        }
        if (ma == null)
            throw buildException("Metric Algorithm not found");
        return ma;
    }
}
