package com.coria.v3.resolver;

import com.coria.v3.dbmodel.ASOrganizationEntity;
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
 * This resolver handles the GraphQL query requests for ASOrganizationEntity.
 */
@Component
public class ASOrganizationQueryResolver extends BaseResolver implements GraphQLQueryResolver {
    @SuppressWarnings("unused")
    public Page<ASOrganizationEntity> allASOrganizations(int page, int perPage, String sortField, String sortOrder, BaseFilter_Q_StringIds filter) {
        Pageable p = toPageable(page, perPage, sortField, sortOrder);
        if (filter.getQ() != null)
            return repositoryManager.getAsOrganizationRepository().findAllByIdContainingOrNameContaining(filter.getQ(), filter.getQ(), p);
        if (filter.getIds() != null && filter.getIds().size() > 0)
            return repositoryManager.getAsOrganizationRepository().findAllByIdIn(filter.getIds(), p);
        return repositoryManager.getAsOrganizationRepository().findAll(p);
    }

    @SuppressWarnings("unused")
    public ListMetadata _allASOrganizationsMeta(int page, int perPage, String sortField, String sortOrder, BaseFilter_Q_StringIds filter) {
        if (filter.getQ() != null)
            return new ListMetadata(repositoryManager.getAsOrganizationRepository().countAllByIdContaining(filter.getQ()));
        if (filter.getIds() != null && filter.getIds().size() > 0)
            return new ListMetadata(repositoryManager.getAsOrganizationRepository().countAllByIdIn(filter.getIds()));
        return new ListMetadata(repositoryManager.getAsOrganizationRepository().count());
    }

    @SuppressWarnings("unused")
    public ASOrganizationEntity getASOrganization(String id) {
        return repositoryManager.getAsOrganizationRepository().findById(id).orElseThrow(
                GraphqlErrorException
                        .newErrorException()
                        .message("ASOrganization not found")
                        .errorClassification(ErrorType.DataFetchingException)
                        ::build
        );
    }
}
