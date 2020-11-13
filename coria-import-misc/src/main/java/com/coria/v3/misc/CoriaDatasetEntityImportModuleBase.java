package com.coria.v3.misc;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.EdgeEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.parser.DatasetEntityImportModule;
import com.coria.v3.repository.DatasetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 */
public abstract class CoriaDatasetEntityImportModuleBase extends DatasetEntityImportModule {

    private final static Logger logger = LoggerFactory.getLogger(CoriaDatasetEntityImportModuleBase.class);

    protected DatasetEntity datasetEntity;
    protected HashMap<String, NodeEntity> nodeDict;
    protected HashMap<String, EdgeEntity> edgeDict;


    protected CoriaDatasetEntityImportModuleBase(String id, String name, String description) {
        super(id, name, description);
    }

    protected void parseInformationBase(String datasetName) {
        nodeDict = new HashMap<>();
        edgeDict = new HashMap<>();
        datasetEntity = new DatasetEntity();
        datasetEntity.setId(UUID.randomUUID());
        datasetEntity.setCreated(Timestamp.from(Instant.now()));
        datasetEntity.setName(datasetName);
    }

    protected void finalizeImport(DatasetRepository datasetRepository) {
        datasetEntity.setNodes(nodeDict.values());
        datasetRepository.save(datasetEntity);
        datasetEntity.setEdges(edgeDict.values());
        datasetRepository.save(datasetEntity);
        datasetEntity.addAttribute("count_nodes", String.valueOf(nodeDict.size()));
        datasetEntity.addAttribute("count_edges", String.valueOf(edgeDict.size()));
        datasetRepository.save(datasetEntity);
        logger.debug("parsing finished, parsed {} nodes, {} edges", nodeDict.size(), edgeDict.size());
    }

    protected NodeEntity createNode(String nodeName) {
        NodeEntity node = nodeDict.get(nodeName);
        if (node == null) {
            node = new NodeEntity();
            node.setId(UUID.randomUUID());
            node.setName(nodeName);
            node.setDataset(datasetEntity);
            nodeDict.put(nodeName, node);
        }
        return node;
    }

    protected void createEdge(NodeEntity fNode, NodeEntity tNode) {
        String name = String.format("%s <-> %s", fNode.getName(), tNode.getName());
        EdgeEntity edge = edgeDict.get(name);
        if (edge == null) {
            edge = new EdgeEntity();
            edge.setName(name);
            edge.setDataset(datasetEntity);
            edge.setNodeSource(fNode);
            edge.setNodeTarget(tNode);
            edgeDict.put(name, edge);
        }
    }
}
