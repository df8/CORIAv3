package com.coria.v3.resolver;

import com.coria.v3.dbmodel.NodeEntity;
import graphql.ErrorType;
import graphql.kickstart.tools.GraphQLMutationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * This resolver handles the GraphQL mutation requests for NodeEntity.
 */
@Component
public class NodeMutationResolver extends BaseResolver implements GraphQLMutationResolver {
    private static final Logger logger = LoggerFactory.getLogger(NodeMutationResolver.class);

    /**
     * Handles the GraphQL request to rename a node.
     *
     * @param nodeId UUID of the NodeEntity to be renamed
     * @param name   The new name
     * @return the modified NodeEntity
     */
    public NodeEntity updateNode(UUID nodeId, String name) {
        NodeEntity nodeEntity = repositoryManager.getNodeRepository().findById(nodeId).orElse(null);
        if (nodeEntity == null)
            throw buildException("Node not found.", ErrorType.ValidationError);
        nodeEntity.setName(name);
        repositoryManager.getNodeRepository().save(nodeEntity);
        return nodeEntity;
    }

    /**
     * Handles the GraphQL request to delete a node.
     *
     * @param nodeId UUID of the NodeEntity to be deleted
     * @return the deleted NodeEntity
     */
    public NodeEntity deleteNode(UUID nodeId) {
        NodeEntity nodeEntity = repositoryManager.getNodeRepository().findById(nodeId).orElseThrow();
        try {
            repositoryManager.getNodeRepository().delete(nodeEntity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodeEntity;
    }
}
