package com.coria.v3.resolver;

import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.model.ListMetadata;
import com.coria.v3.model.NodeEdgeFilter;
import graphql.ErrorType;
import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for NodeEntity.
 */
@Component
public class NodeQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    public Page<NodeEntity> allNodes(int page, int perPage, String sortField, String sortOrder, NodeEdgeFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");
        if (filter.getName() == null || filter.getName().length() == 0) {
            return repositoryManager.getNodeRepository().findAllByDataset_Id(filter.getDatasetId(), toPageable(page, perPage, sortField, sortOrder));
        } else {
            return repositoryManager.getNodeRepository().findAllByDataset_IdAndNameContains(filter.getDatasetId(), filter.getName(), toPageable(page, perPage, sortField, sortOrder));
        }
    }

    public ListMetadata _allNodesMeta(int page, int perPage, String sortField, String sortOrder, NodeEdgeFilter filter) {
        if (filter == null || filter.getDatasetId() == null)
            throw buildException("Missing parameter datasetId");
        if (filter.getName() == null || filter.getName().length() == 0) {
            return new ListMetadata(repositoryManager.getNodeRepository().countByDataset_Id(filter.getDatasetId()));
        } else {
            return new ListMetadata(repositoryManager.getNodeRepository().countByDataset_IdAndNameContains(filter.getDatasetId(), filter.getName()));
        }
    }

    public NodeEntity getNode(String id) {
        return repositoryManager.getNodeRepository().findById(UUID.fromString(id)).orElseThrow(
                GraphqlErrorException
                        .newErrorException()
                        .message("Node not found")
                        .errorClassification(ErrorType.DataFetchingException)
                        ::build
        );
    }
}
