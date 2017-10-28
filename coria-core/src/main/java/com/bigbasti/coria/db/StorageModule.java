package com.bigbasti.coria.db;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.MetricInfo;

import java.beans.Transient;
import java.util.List;

/**
 * DataStorages offer the possibility to store application data for
 * the purpose of reuse.<br/>
 * How the Data is stored is up to the StorageModule provider
 * Created by Sebastian Gross
 */
public interface StorageModule {
    String getIdentification();

    String getName();

    String getDescription();

    CoriaEdge getEdge(String id);
    @Transient
    List<CoriaEdge> getEdges();
    List<CoriaEdge> getEdges(String orderby, String ordertype);
    List<CoriaEdge> getEdges(Long from, Long to, String orderBy, String orderType);
    void updateEdge(CoriaEdge edge);
    void deleteEdge(CoriaEdge edge);
    void deleteEdge(String id);

    CoriaNode getNode(String id);
    @Transient
    List<CoriaNode> getNodes();
    List<CoriaNode> getNodes(String orderby, String ordertype);
    List<CoriaEdge> getNodes(Long from, Long to, String orderBy, String orderType);
    void updateNode(CoriaNode node);
    void deleteNode(CoriaNode node);
    void deleteNode(String id);

    MetricInfo getMetricInfo(String id);
    @Transient
    List<MetricInfo> getMetricInfos();
    List<MetricInfo> getMetricInfos(String datasetId);
    String addMetricInfo(MetricInfo metric, String datasetId);
    String updateMetricInfo(MetricInfo metricInfo);

    String addDataSet(DataSet dataSet);
    DataSet getDataSet(String id);
    @Transient
    List<DataSet> getDataSets();
    /**
     * returns all datasets without the nodes and edges
     * @return list of all datasets minus nodes & edges
     */
    @Transient
    List<DataSet> getDataSetsShort();
    String updateDataSet(DataSet dataSet);
    void deleteDataSet(DataSet dataSet);
    void deleteDataSet(String id);

    /**
     * returns current storage status (wether it is available) and provides an
     * error message in case its not
     * @return Status of storage
     */
    StorageStatus getStorageStatus();
    void dispose();
}