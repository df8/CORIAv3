package com.coria.v3.resolver;

import com.coria.v3.dbmodel.ASLocationEntity;
import com.coria.v3.model.BaseFilter_Q_StringIds;
import com.coria.v3.model.ListMetadata;
import graphql.ErrorType;
import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL query requests for ASLocationEntity.
 */
@Component
public class ASLocationQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    @SuppressWarnings("unused")
    public Page<ASLocationEntity> allASLocations(int page, int perPage, String sortField, String sortOrder, BaseFilter_Q_StringIds filter) {
        Pageable p = toPageable(page, perPage, sortField, sortOrder);
        if (filter.getQ() != null)
            return repositoryManager.getAsLocationRepository().findAllByIdContaining(filter.getQ(), p);
        if (filter.getIds() != null && filter.getIds().size() > 0)
            return repositoryManager.getAsLocationRepository().findAllByIdIn(filter.getIds(), p);
        return repositoryManager.getAsLocationRepository().findAll(p);
    }

    @SuppressWarnings("unused")
    public ListMetadata _allASLocationsMeta(int page, int perPage, String sortField, String sortOrder, BaseFilter_Q_StringIds filter) {
        if (filter.getQ() != null)
            return new ListMetadata(repositoryManager.getAsLocationRepository().countAllByIdContaining(filter.getQ()));
        if (filter.getIds() != null && filter.getIds().size() > 0)
            return new ListMetadata(repositoryManager.getAsLocationRepository().countAllByIdIn(filter.getIds()));
        return new ListMetadata(repositoryManager.getAsLocationRepository().count());
    }

    @SuppressWarnings("unused")
    public ASLocationEntity getASLocation(String id) {
        return repositoryManager.getAsLocationRepository().findById(id).orElseThrow(
                GraphqlErrorException
                        .newErrorException()
                        .message("ASLocation not found")
                        .errorClassification(ErrorType.DataFetchingException)
                        ::build
        );
    }
}
