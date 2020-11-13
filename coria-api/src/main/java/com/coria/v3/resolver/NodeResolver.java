package com.coria.v3.resolver;

import com.coria.v3.dbmodel.NodeEntity;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Created by David Fradin, 2020
 */
@Component
public class NodeResolver extends BaseResolver implements GraphQLResolver<NodeEntity> {
    public Set<Map.Entry<String, String>> attributesList(NodeEntity nodeEntity) {
        return nodeEntity.getAttributes().entrySet();
    }
}
