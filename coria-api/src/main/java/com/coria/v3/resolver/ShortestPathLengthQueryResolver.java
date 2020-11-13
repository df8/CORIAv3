package com.coria.v3.resolver;

import com.coria.v3.dbmodel.ShortestPathLengthEntity;
import com.coria.v3.model.ListMetadata;
import com.coria.v3.model.ShortestPathLengthFilter;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for ShortestPathLengthEntity.
 */
@Component
public class ShortestPathLengthQueryResolver extends BaseResolver implements GraphQLQueryResolver {

    @SuppressWarnings("unused")
    public Page<ShortestPathLengthEntity> allShortestPathLengths(int page, int perPage, String sortField, String sortOrder, ShortestPathLengthFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");

        if (sortField.equals("nodeSourceName")) sortField = "nodeSource.name";
        else if (sortField.equals("nodeTargetName")) sortField = "nodeTarget.name";

        List<Sort.Order> sortOrderList;
        switch (sortField) {
            case "nodeTarget.name":
                sortOrderList = List.of(
                        sortOrder.equals("DESC") ? Sort.Order.desc(sortField) : Sort.Order.asc(sortField),
                        sortOrder.equals("DESC") ? Sort.Order.desc("nodeSource.name") : Sort.Order.asc("nodeSource.name")
                );
                break;
            case "distance":
                sortOrderList = List.of(
                        sortOrder.equals("DESC") ? Sort.Order.desc(sortField) : Sort.Order.asc(sortField),
                        Sort.Order.asc("nodeSource.name"),
                        Sort.Order.asc("nodeTarget.name")
                );
                break;
            default:
            case "nodeSource.name":
                sortOrderList = List.of(
                        sortOrder.equals("DESC") ? Sort.Order.desc(sortField) : Sort.Order.asc(sortField),
                        sortOrder.equals("DESC") ? Sort.Order.desc("nodeTarget.name") : Sort.Order.asc("nodeTarget.name")
                );
                break;
        }

        if (filter.getMetricAlgorithmImplementationId() == null || filter.getMetricAlgorithmImplementationId().length() == 0) {
            return repositoryManager.getShortestPathLengthRepository().findAllByMetric_Dataset_Id(filter.getDatasetId(), toPageable(page, perPage, sortOrderList));
        } else {
            return repositoryManager.getShortestPathLengthRepository().findAllByMetric_Dataset_IdAndMetric_MetricAlgorithmImplementationIdContains(filter.getDatasetId(), filter.getMetricAlgorithmImplementationId(), toPageable(page, perPage, sortOrderList));
        }
    }

    @SuppressWarnings("unused")
    public ListMetadata _allShortestPathLengthsMeta(int page, int perPage, String sortField, String sortOrder, ShortestPathLengthFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");
        if (filter.getMetricAlgorithmImplementationId() == null) {
            return new ListMetadata(repositoryManager.getShortestPathLengthRepository().countByMetric_Dataset_Id(filter.getDatasetId()));
        } else {
            return new ListMetadata(repositoryManager.getShortestPathLengthRepository().countByMetric_Dataset_IdAndMetric_MetricAlgorithmImplementationIdContains(filter.getDatasetId(), filter.getMetricAlgorithmImplementationId()));
        }
    }

    public final static String REGEX_UUID = "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}";
    public final static Pattern REGEX_ShortestPathLength_ID = Pattern.compile("^(" + REGEX_UUID + ")--(" + REGEX_UUID + ")--(" + REGEX_UUID + ")$");//three UUIDs separated with a double dash "--"

    public ShortestPathLengthEntity getShortestPathLength(String shortestPathLengthId) {
        Matcher matcher = REGEX_ShortestPathLength_ID.matcher(shortestPathLengthId.toLowerCase());
        if (matcher.matches()) {
            ShortestPathLengthEntity shortestPathLengthEntity = repositoryManager.getShortestPathLengthRepository().findByMetric_IdAndNodeSource_IdAndNodeTarget_Id(UUID.fromString(matcher.group(1)), UUID.fromString(matcher.group(2)), UUID.fromString(matcher.group(3))).orElse(null);
            if (shortestPathLengthEntity != null) {
                return shortestPathLengthEntity;
            }
        }
        throw buildException("ShortestPathLength not found");
    }
}
