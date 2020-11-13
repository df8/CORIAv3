package com.coria.v3.resolver;

import com.coria.v3.dbmodel.DatasetEntity;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Created by David Fradin, 2020
 * GraphQL resolver to add custom properties to DatasetEntity.
 */
@Component
public class DatasetResolver implements GraphQLResolver<DatasetEntity> {
    public Set<Map.Entry<String, String>> attributesList(DatasetEntity datasetEntity) {
        return datasetEntity.getAttributes().entrySet();
    }
}
