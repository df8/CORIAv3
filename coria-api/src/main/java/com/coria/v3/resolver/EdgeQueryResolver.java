package com.coria.v3.resolver;

import com.coria.v3.dbmodel.EdgeEntity;
import com.coria.v3.model.ListMetadata;
import com.coria.v3.model.NodeEdgeFilter;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for EdgeEntity.
 */
@Component
public class EdgeQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    public Page<EdgeEntity> allEdges(int page, int perPage, String sortField, String sortOrder, NodeEdgeFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");
        if (filter.getName() == null || filter.getName().length() == 0) {
            return repositoryManager.getEdgeRepository().findAllByDataset_Id(filter.getDatasetId(), toPageable(page, perPage, sortField, sortOrder));
        } else {
            return repositoryManager.getEdgeRepository().findAllByDataset_IdAndNameContains(filter.getDatasetId(), filter.getName(), toPageable(page, perPage, sortField, sortOrder));
        }
    }

    public ListMetadata _allEdgesMeta(int page, int perPage, String sortField, String sortOrder, NodeEdgeFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");
        if (filter.getName() == null || filter.getName().length() == 0) {
            return new ListMetadata(repositoryManager.getEdgeRepository().countByDataset_Id(filter.getDatasetId()));
        } else {
            return new ListMetadata(repositoryManager.getEdgeRepository().countByDataset_IdAndNameContains(filter.getDatasetId(), filter.getName()));
        }
    }

    public final static String REGEX_UUID = "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}";
    public final static Pattern REGEX_EDGE_ID = Pattern.compile("^(" + REGEX_UUID + ")--(" + REGEX_UUID + ")$");//two UUIDs separated with a double dash "--"

    public EdgeEntity getEdge(String edgeId) {
        Matcher matcher = REGEX_EDGE_ID.matcher(edgeId.toLowerCase());
        if (matcher.matches()) {
            //EdgeEntity edgeEntity = edgeRepository.findById(new EdgeEntityPK(UUID.fromString(matcher.group(1)), UUID.fromString(matcher.group(2)))).orElse(null);
            EdgeEntity edgeEntity = repositoryManager.getEdgeRepository().findByNodeSource_IdAndNodeTarget_Id(UUID.fromString(matcher.group(1)), UUID.fromString(matcher.group(2))).orElse(null);
            if (edgeEntity != null) {
                return edgeEntity;
            }
        }
        throw buildException("Edge not found");
    }
}
