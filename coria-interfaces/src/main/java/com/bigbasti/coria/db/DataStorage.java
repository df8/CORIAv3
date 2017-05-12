package com.bigbasti.coria.db;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;

import java.util.List;

/**
 * DataStorages offer the possibility to store application data for
 * the purpose of reuse.<br/>
 * How the Data is stored is up to the DataStorage provider
 * Created by Sebastian Gross
 */
public interface DataStorage {
    String getIdentification();

    String getName();

    String getDeecription();

    CoriaEdge getEdge(String id);
    List<CoriaEdge> getEdges();
    List<CoriaEdge> getEdges(String orderby, String ordertype);
    List<CoriaEdge> getEdges(Long from, Long to, String orderBy, String orderType);
    void updateEdge(CoriaEdge edge);
    void deleteEdge(CoriaEdge edge);
    void deleteEdge(String id);

    CoriaNode getNode(String id);
    List<CoriaNode> getNodes();
    List<CoriaNode> getNodes(String orderby, String ordertype);
    List<CoriaEdge> getNodes(Long from, Long to, String orderBy, String orderType);
    void updateNode(CoriaNode node);
    void deleteNode(CoriaNode node);
    void deleteNode(String id);

    DataSet getDataSet(String id);
    List<DataSet> getDataSets();
    void updateDataSet(DataSet dataSet);
    void deleteDataSet(DataSet dataSet);
    void deleteDataSet(String id);

}
