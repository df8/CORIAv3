package com.coria.v3.resolver;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.model.DatasetFilter;
import com.coria.v3.model.ListMetadata;
import graphql.ErrorType;
import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for DatasetEntity.
 */
@Component
public class DatasetQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    @SuppressWarnings("unused")
    public Page<DatasetEntity> allDatasets(int page, int perPage, String sortField, String sortOrder, DatasetFilter filter) {
        Pageable p = toPageable(page, perPage, sortField, sortOrder);
        if (filter != null) {
            if (filter.getQ() != null)
                return repositoryManager.getDatasetRepository().findAllByNameContaining(filter.getQ(), p);
            if (filter.getIds() != null && filter.getIds().size() > 0)
                return repositoryManager.getDatasetRepository().findAllByIdIn(filter.getIds(), p);
        }
        return repositoryManager.getDatasetRepository().findAll(p);
    }

    @SuppressWarnings("unused")
    public ListMetadata _allDatasetsMeta(int page, int perPage, String sortField, String sortOrder, DatasetFilter filter) {
        if (filter != null) {
            if (filter.getQ() != null)
                return new ListMetadata(repositoryManager.getDatasetRepository().countAllByNameContaining(filter.getQ()));
            if (filter.getIds() != null && filter.getIds().size() > 0)
                return new ListMetadata(repositoryManager.getDatasetRepository().countAllByIdIn(filter.getIds()));
        }
        return new ListMetadata(repositoryManager.getDatasetRepository().count());
    }

    @SuppressWarnings("unused")
    public DatasetEntity Dataset(String id) {
        return repositoryManager.getDatasetRepository().findById(UUID.fromString(id)).orElseThrow(
                GraphqlErrorException
                        .newErrorException()
                        .message("Dataset not found")
                        .errorClassification(ErrorType.DataFetchingException)
                        ::build
        );
    }
}
