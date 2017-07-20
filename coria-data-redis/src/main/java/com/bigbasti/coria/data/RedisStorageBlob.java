package com.bigbasti.coria.data;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.db.StorageStatus;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.MetricInfo;
import com.google.common.base.Strings;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//@Component
public class RedisStorageBlob implements DataStorage {
    private Logger logger = LoggerFactory.getLogger(RedisStorageBlob.class);

    @Autowired
    Environment env;

    private String dbUrl;
    private String dbUser;
    private String dbPass;

    Config config = null;
    RedissonClient client = null;

    @PostConstruct
    public void checkDatabaseSetup(){
        dbUrl = env.getProperty("coria.db.redis.url");
        dbUser = env.getProperty("coria.db.redis.clientname");
        dbPass = env.getProperty("coria.db.redis.password");


        config = new Config();
        SingleServerConfig ssc = config.useSingleServer();
        ssc.setAddress(dbUrl);
        if(!Strings.isNullOrEmpty(dbUser)){ssc.setClientName(dbUser);}
        if(!Strings.isNullOrEmpty(dbPass)){ssc.setPassword(dbPass);}

        client = Redisson.create(config);
    }

    @Override
    public String getIdentification() {
        return "coria-data-redis";
    }

    @Override
    public String getName() {
        return "Redis Database Adapter";
    }

    @Override
    public String getDescription() {
        return "Connects CORIA to the Redis Storage, the access configuration needs to be setup in the application.properties of this module";
    }

    @Override
    public CoriaEdge getEdge(String id) {
        return null;
    }

    @Override
    public List<CoriaEdge> getEdges() {
        return null;
    }

    @Override
    public List<CoriaEdge> getEdges(String orderby, String ordertype) {
        return null;
    }

    @Override
    public List<CoriaEdge> getEdges(Long from, Long to, String orderBy, String orderType) {
        return null;
    }

    @Override
    public void updateEdge(CoriaEdge edge) {

    }

    @Override
    public void deleteEdge(CoriaEdge edge) {

    }

    @Override
    public void deleteEdge(String id) {

    }

    @Override
    public CoriaNode getNode(String id) {
        return null;
    }

    @Override
    public List<CoriaNode> getNodes() {
        return null;
    }

    @Override
    public List<CoriaNode> getNodes(String orderby, String ordertype) {
        return null;
    }

    @Override
    public List<CoriaEdge> getNodes(Long from, Long to, String orderBy, String orderType) {
        return null;
    }

    @Override
    public void updateNode(CoriaNode node) {

    }

    @Override
    public void deleteNode(CoriaNode node) {

    }

    @Override
    public void deleteNode(String id) {

    }

    @Override
    public MetricInfo getMetricInfo(String id) {
        return null;
    }

    @Override
    public List<MetricInfo> getMetricInfos() {
        logger.debug("loading metric infos");
        Instant starts = Instant.now();

        List<MetricInfo> retVal = new ArrayList<>();
        List<Object> dataSets = getClient().getList("datasets").readAll();
        for(Object o : dataSets){
            DataSet ds = (DataSet) o;
            retVal.addAll(ds.getMetricInfos());
        }

        Instant ends = Instant.now();
        logger.debug("loading metric infos finished ({})", Duration.between(starts, ends));
        return retVal;
    }

    @Override
    public List<MetricInfo> getMetricInfos(String datasetId) {
        logger.debug("loading metric infos for {}", datasetId);
        Instant starts = Instant.now();

        List<MetricInfo> retVal = null;
        List<Object> dataSets = getClient().getList("datasets").readAll();
        for(Object o : dataSets){
            DataSet ds = (DataSet) o;
            if(ds.getId().equals(datasetId)){
                retVal = ds.getMetricInfos();
            }
        }

        Instant ends = Instant.now();
        logger.debug("loading metric infos finished ({})", Duration.between(starts, ends));
        return retVal;
    }

    @Override
    public String addMetricInfo(MetricInfo metric, String datasetId) {
        logger.debug("inserting new metric info {}", datasetId);
        Instant starts = Instant.now();

        metric.setId(UUID.randomUUID().toString());
        List<Object> dataSets = getClient().getList("datasets").readAll();
        for(Object o : dataSets){
            DataSet ds = (DataSet) o;
            if(ds.getId().equals(datasetId)) {
                ds.getMetricInfos().add(metric);
                updateDataSet(ds);
                break;
            }
        }

        Instant ends = Instant.now();
        logger.debug("inserting new metric info finished ({})", Duration.between(starts, ends));
        return metric.getId();
    }

    @Override
    public String updateMetricInfo(MetricInfo metricInfo) {
        logger.debug("updating metric info {}", metricInfo);
        Instant starts = Instant.now();

        List<Object> dataSets = getClient().getList("datasets").readAll();
        for(Object o : dataSets){
            DataSet ds = (DataSet) o;
            for(MetricInfo mi : ds.getMetricInfos()){
                if(mi.getId().equals(metricInfo.getId())){
                    mi = metricInfo;
                    updateDataSet(ds);
                    break;
                }
            }
        }

        Instant ends = Instant.now();
        logger.debug("inserting new metric info finished ({})", Duration.between(starts, ends));
        return null;
    }

    @Override
    public String addDataSet(DataSet dataSet) {
        logger.debug("inserting dataset...");
        Instant starts = Instant.now();

        dataSet.setId(UUID.randomUUID().toString());
        RList<DataSet> dataSets = getClient().getList("datasets");
        dataSets.add(dataSet);

        Instant ends = Instant.now();
        logger.debug("inserting dataset finished ({})", Duration.between(starts, ends));
        return null;
    }

    @Override
    public DataSet getDataSet(String id) {
        logger.debug("loading dataset {}", id);
        Instant starts = Instant.now();

        DataSet retVal = null;
        List<Object> dataSets = getClient().getList("datasets").readAll();
        for(Object o : dataSets){
            DataSet ds = (DataSet) o;
            if(ds.getId().equals(id)){
                retVal = ds;
            }
        }

        Instant ends = Instant.now();
        logger.debug("inserting dataset finished ({})", Duration.between(starts, ends));
        return retVal;
    }

    @Override
    public List<DataSet> getDataSets() {
        return getDataSetsShort();
    }

    @Override
    public List<DataSet> getDataSetsShort() {
//        RKeys keys = getClient().getKeys();
//        Iterable<String> dataSets = keys.getKeysByPattern("dataset*");

        RList<DataSet> dataSets = getClient().getList("datasets");
        return dataSets.readAll();

    }

    @Override
    public String updateDataSet(DataSet dataSet) {
        RList<DataSet> dataSets = getClient().getList("datasets");
        for(DataSet ds : dataSets){
            if(ds.getId().equals(dataSet.getId())){
                dataSets.remove(ds);
                dataSets.add(dataSet);
            }
        }
        return null;
    }

    @Override
    public void deleteDataSet(DataSet dataSet) {
        deleteDataSet(dataSet.getId());
    }

    @Override
    public void deleteDataSet(String id) {
        logger.debug("deleting dataset {}", id);
        Instant starts = Instant.now();

        List<Object> dataSets = getClient().getList("datasets").readAll();
        for(Object o : dataSets){
            DataSet ds = (DataSet) o;
            if(ds.getId().equals(id)){
                dataSets.remove(o);
            }
        }

        Instant ends = Instant.now();
        logger.debug("deleting dataset finished ({})", Duration.between(starts, ends));
    }

    @Override
    public StorageStatus getStorageStatus() {
        StorageStatus status = new StorageStatus(true, null);

        RedissonClient client = Redisson.create(config);
        if(client == null){
            status.setReadyToUse(false);
            status.setMessage("Connection was not successful");
        }

        return status;
    }

    private RedissonClient getClient(){
        return client;
    }

    @Override
    public String toString() {
        return "RedisStorage{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
