package com.coria.v3.resolver;

import com.coria.v3.dbmodel.EdgeEntity;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Created by David Fradin, 2020
 */
@Component
public class EdgeResolver extends BaseResolver implements GraphQLResolver<EdgeEntity> {
    public Set<Map.Entry<String, String>> attributesList(EdgeEntity edgeEntity) {
        return edgeEntity.getAttributes().entrySet();
    }
}
