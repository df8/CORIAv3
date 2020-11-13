package com.coria.v3.resolver;

import com.coria.v3.config.AppContext;
import com.coria.v3.repository.RepositoryManager;
import graphql.ErrorType;
import graphql.GraphqlErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by David Fradin, 2020
 * Base class for GraphQL resolvers to encapsulate common functions such as pagination and error handling in one place.
 */
@Component
public abstract class BaseResolver {

    protected AppContext appContext;
    protected RepositoryManager repositoryManager;

    @Autowired
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    @Autowired
    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    protected static Pageable toPageable(int page, int perPage, String sortField, String sortOrder) {
        if (sortField == null) {
            throw buildException("sortField parameter cannot be null", ErrorType.ValidationError);
        }
        if (!sortOrder.equals("DESC"))
            sortOrder = "ASC";
        if (perPage == 0)
            perPage = 1000;
        return PageRequest.of(page, perPage, Sort.by(Sort.Direction.fromString(sortOrder), sortField));
    }

    protected static Pageable toPageable(int page, int perPage, List<Sort.Order> sortFields) {
        if (perPage == 0)
            perPage = 1000;
        return PageRequest.of(page, perPage, Sort.by(sortFields));
    }

    protected static GraphqlErrorException buildException(String message) {
        return buildException(message, ErrorType.DataFetchingException);
    }

    protected static GraphqlErrorException buildException(String message, ErrorType errorType) {
        return GraphqlErrorException
                .newErrorException()
                .message(message)
                .errorClassification(errorType)
                .build();
    }
}
