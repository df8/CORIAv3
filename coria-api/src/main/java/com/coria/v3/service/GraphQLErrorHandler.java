package com.coria.v3.service;

import graphql.ErrorClassification;
import graphql.ExceptionWhileDataFetching;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Catches errors thrown by GraphQL resolvers and reformats them before returning the response to the client.
 * Created by David Fradin, 2020
 * <p>
 * Sources:
 * https://www.pluralsight.com/guides/building-a-graphql-server-with-spring-boot
 * https://github.com/graphql-java-kickstart/graphql-spring-boot/issues/219
 */
@Configuration
@Component
public class GraphQLErrorHandler implements graphql.kickstart.execution.error.GraphQLErrorHandler {
    private final Logger logger = LoggerFactory.getLogger(GraphQLErrorHandler.class);

    @Override
    public List<GraphQLError> processErrors(List<GraphQLError> errors) {
        errors.forEach(error -> {
            Throwable throwable = null;
            if (error instanceof ExceptionWhileDataFetching) {
                throwable = ((ExceptionWhileDataFetching) error).getException();
            } else if (error instanceof Throwable) {
                throwable = (Throwable) error;
            }
            if (throwable != null) {
                StackTraceElement e = Arrays.stream(throwable.getStackTrace())
                        .filter(stackTraceElement -> stackTraceElement.getClassName().startsWith("com.coria.v3"))
                        .findFirst().orElse(null);
                if (e != null)
                    logger.debug("Error caught: {}:{}\t{}", e.getClassName(), e.getLineNumber(), throwable.toString());
            }
        });
        /*return errors.stream()
                .map(err -> {
                            if (err instanceof ExceptionWhileDataFetching && ((ExceptionWhileDataFetching) err).getException() instanceof GraphQLError) {
                                return ((ExceptionWhileDataFetching) err).getException();
                            } else {
                                return (GraphQLError)new GraphQLErrorAdapter(err);
                            }
                        }
                )
                .collect(Collectors.toList());*/

        return errors;
        //return errors;
        /*List<GraphQLError> clientErrors = errors
                .stream()
                .filter(this::isClientError)
                .map(err -> (GraphQLError) ((ExceptionWhileDataFetching) err).getException())
                .collect(Collectors.toList());

        List<GraphQLError> serverErrors = errors
                .stream()
                .filter(e -> !isClientError(e))
                .map(GraphQLErrorAdapter::new)
                .collect(Collectors.toList());

        List<GraphQLError> e = new ArrayList<>();
        e.addAll(clientErrors);
        e.addAll(serverErrors);
        return e;*/
    }
}

/**
 * Created by David Fradin, 2020
 */
class GraphQLErrorAdapter implements GraphQLError {

    private static final long serialVersionUID = 1L;

    public GraphQLErrorAdapter(GraphQLError error) {
        this.error = error;
    }

    private final GraphQLError error;

    @Override
    public Map<String, Object> getExtensions() {
        return error.getExtensions();
    }

    @Override
    public List<SourceLocation> getLocations() {
        return error.getLocations();
    }

    @Override
    public ErrorClassification getErrorType() {
        return error.getErrorType();
    }

    @Override
    public List<Object> getPath() {
        return error.getPath();
    }

    @Override
    public Map<String, Object> toSpecification() {
        return error.toSpecification();
    }

    @Override
    public String getMessage() {
        if (error instanceof ExceptionWhileDataFetching) {
            return ((ExceptionWhileDataFetching) error).getException().getMessage();
        }
        return error.getMessage();
    }
}
