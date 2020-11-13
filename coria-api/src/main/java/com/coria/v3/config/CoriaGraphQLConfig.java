package com.coria.v3.config;

import com.coria.v3.service.GraphQLErrorHandler;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import graphql.kickstart.execution.GraphQLObjectMapper;
import graphql.kickstart.execution.config.GraphQLServletObjectMapperConfigurer;
import graphql.kickstart.execution.config.ObjectMapperProvider;
import graphql.kickstart.servlet.apollo.ApolloScalars;
import graphql.kickstart.spring.error.ErrorHandlerSupplier;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

import javax.servlet.Filter;

import static graphql.kickstart.execution.GraphQLObjectMapper.newBuilder;

/**
 * Created by David Fradin, 2020
 * Based on source: https://github.com/graphql-java-kickstart/graphql-spring-boot/blob/master/example-graphql-tools/src/main/java/com/graphql/sample/boot/GraphQLToolsSampleApplication.java
 */
@Configuration
public class CoriaGraphQLConfig {

    /**
     * @return
     */
    @Bean
    public GraphQLServletObjectMapperConfigurer objectMapperConfigurer() {
        return (mapper -> mapper.registerModule(new JavaTimeModule()));
    }

    /**
     * Source: https://github.com/graphql-java-kickstart/graphql-spring-boot/issues/379
     *
     * @param objectMapperProviderObjectProvider
     * @param graphQLErrorHandler
     * @param objectMapperConfigurer
     * @return
     */
    @Bean
    public GraphQLObjectMapper graphQLObjectMapper(ObjectProvider<ObjectMapperProvider> objectMapperProviderObjectProvider, GraphQLErrorHandler graphQLErrorHandler, @Autowired(required = false) GraphQLServletObjectMapperConfigurer objectMapperConfigurer) {
        GraphQLObjectMapper.Builder builder = newBuilder();

        builder.withGraphQLErrorHandler(new ErrorHandlerSupplier(graphQLErrorHandler));

        ObjectMapperProvider objectMapperProvider = objectMapperProviderObjectProvider.getIfAvailable();

        if (objectMapperProvider != null) {
            builder.withObjectMapperProvider(objectMapperProvider);
        } else if (objectMapperConfigurer != null) {
            builder.withObjectMapperConfigurer(objectMapperConfigurer);
        }
        return builder.build();
    }

    @Bean
    public Filter OpenFilter() {
        return new OpenEntityManagerInViewFilter();
    }

    @SuppressWarnings("SameReturnValue")
    @Bean
    public GraphQLScalarType uploadScalarDefine() {
        return ApolloScalars.Upload;
    }

    /**
     * The GraphQL type "JSON" is required, because GraphQL does not natively implement conversion from java.util.Map<?,?> to JSON.
     * Source: https://stackoverflow.com/questions/46034801/custom-scalar-in-graphql-java/46074133#46074133
     */
    @SuppressWarnings("SameReturnValue")
    @Bean
    public GraphQLScalarType extendedScalarsConfig() {
        return ExtendedScalars.Json;
    }
}
