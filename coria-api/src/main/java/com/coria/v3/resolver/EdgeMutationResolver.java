package com.coria.v3.resolver;

import com.coria.v3.dbmodel.EdgeEntity;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL mutation requests for ASLocationEntity.
 */
@Component
public class EdgeMutationResolver extends BaseResolver implements GraphQLMutationResolver {
    private static final Logger logger = LoggerFactory.getLogger(EdgeMutationResolver.class);

    EdgeQueryResolver edgeQueryResolver;

    @Autowired
    public void setEdgeQueryResolver(EdgeQueryResolver edgeQueryResolver) {
        this.edgeQueryResolver = edgeQueryResolver;
    }

    /**
     * Handles the GraphQL request to rename an edge.
     *
     * @param edgeId UUID of the EdgeEntity to be renamed
     * @param name   The new name
     * @return the modified EdgeEntity
     */
    public EdgeEntity updateEdge(String edgeId, String name) {
        EdgeEntity edgeEntity = edgeQueryResolver.getEdge(edgeId);
        edgeEntity.setName(name);
        repositoryManager.getEdgeRepository().save(edgeEntity);
        return edgeEntity;
    }

    /**
     * Handles the GraphQL request to delete an edge.
     * TODO /3 Deleting edges does not work.
     * Error: Cannot convert value of type 'java.util.UUID' to required type 'com.coria.v3.dbmodel.NodeEntity' for property 'nodeSource': no matching editors or conversion strategy found
     * This may help: https://stackoverflow.com/questions/39185977/failed-to-convert-request-element-in-entity-with-idclass
     *
     * @param edgeId UUID of the EdgeEntity to be renamed
     * @return the deleted EdgeEntity
     */
    public EdgeEntity deleteEdge(String edgeId) {
        EdgeEntity edgeEntity = edgeQueryResolver.getEdge(edgeId);
        repositoryManager.getEdgeRepository().delete(edgeEntity);
        return edgeEntity;
    }
}
