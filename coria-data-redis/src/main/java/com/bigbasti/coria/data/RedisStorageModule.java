package com.bigbasti.coria.data;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.StorageModule;
import com.bigbasti.coria.db.StorageStatus;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.MetricInfo;
import com.google.common.base.Strings;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
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
import java.util.*;

@Component
public class RedisStorageModule implements StorageModule {
    private Logger logger = LoggerFactory.getLogger(RedisStorageModule.class);

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


        try {
            config = new Config();
            SingleServerConfig ssc = config.useSingleServer();
            ssc.setAddress(dbUrl);
            if (!Strings.isNullOrEmpty(dbUser)) {
                ssc.setClientName(dbUser);
            }
            if (!Strings.isNullOrEmpty(dbPass)) {
                ssc.setPassword(dbPass);
            }

            client = Redisson.create(config);
        }catch(Exception ex){
            logger.error("could not establish database connection");
            logger.error("db seems not to be ready to use");
        }
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
        return "Connects CORIA to the Redis Storage, the access configuration needs to be setup in the application.properties";
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


        Instant ends = Instant.now();
        logger.debug("loading metric infos finished ({})", Duration.between(starts, ends));
        return null;
    }

    @Override
    public List<MetricInfo> getMetricInfos(String datasetId) {
        logger.trace("loading metric infos for {}", datasetId);
        Instant starts = Instant.now();

        List<MetricInfo> mInfos = null;
        mInfos = getClient().getList("minfos#" + datasetId);

        Instant ends = Instant.now();
        logger.trace("loading metric infos finished ({})", Duration.between(starts, ends));
        return mInfos;
    }

    @Override
    public String addMetricInfo(MetricInfo metric, String datasetId) {
        logger.debug("inserting new metric info {}", datasetId);
        Instant starts = Instant.now();

        metric.setId(UUID.randomUUID().toString());
        RList<MetricInfo> dbMInfos = getClient().getList("minfos#" + datasetId);
        dbMInfos.add(metric);

        Instant ends = Instant.now();
        logger.debug("inserting new metric info finished ({})", Duration.between(starts, ends));
        return metric.getId();
    }

    @Override
    public String updateMetricInfo(MetricInfo metricInfo) {
        logger.debug("updating metric info {}", metricInfo);
        Instant starts = Instant.now();

        RList<DataSet> dataSets = getClient().getList("datasets");
        for(DataSet ds : dataSets){
            RList<MetricInfo> dbMInfos = getClient().getList("minfos#" + ds.getId());
            for(MetricInfo mi : dbMInfos){
                if(mi.getId().equals(metricInfo.getId())){
//                    MetricInfo updated = new MetricInfo();
//                    updated.setId(metricInfo.getId());
//                    updated.setExecutionStarted(metricInfo.getExecutionStarted());
//                    updated.setExecutionFinished(metricInfo.getExecutionFinished());
//                    updated.setMessage(metricInfo.getMessage());
//                    updated.setName(metricInfo.getName());
//                    updated.setProvider(metricInfo.getProvider());
//                    updated.setShortcut(metricInfo.getShortcut());
//                    updated.setTechnology(metricInfo.getTechnology());
//                    updated.setType(metricInfo.getType());
//                    updated.setValue(metricInfo.getValue());
//                    updated.setStatus(metricInfo.getStatus());
                    dbMInfos.remove(mi);
                    dbMInfos.add(metricInfo);
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
        List<CoriaNode> nodes = dataSet.getNodes();
        List<CoriaEdge> edges = dataSet.getEdges();
        List<MetricInfo> minfos = dataSet.getMetricInfos();
        Map<String, String> attrs = dataSet.getAttributes();

        dataSet.setNodes(null);
        dataSet.setEdges(null);
        dataSet.setMetricInfos(null);
        dataSet.setAttributes(null);

        // 1. Store dataset itself (no nested objects)
        RList<DataSet> dataSets = getClient().getList("datasets");
        dataSets.add(dataSet);

        // 2. Store nodes
        RList<CoriaNode> dbNodes = getClient().getList("nodes#" + dataSet.getId());
        nodes.forEach(coriaNode -> {
            coriaNode.setId(UUID.randomUUID().toString());
            dbNodes.add(coriaNode);
        });

        // 3. Store Edges
        RList<CoriaEdge> dbEdges = getClient().getList("edges#" + dataSet.getId());
        edges.forEach(coriaEdge -> {
            coriaEdge.setId(UUID.randomUUID().toString());
            dbEdges.add(coriaEdge);
        });

        // 4. Store metric infos
        RList<MetricInfo> dbMInfos = getClient().getList("minfos#" + dataSet.getId());
        minfos.forEach(metricInfo -> {
            metricInfo.setId(UUID.randomUUID().toString());
            dbMInfos.add(metricInfo);
        });

        // 5. Store attributes
        RMap<String, String> map = client.getMap("attrs#" + dataSet.getId());
        for(String key : attrs.keySet()){
            map.put(key, attrs.get(key));
        }


        Instant ends = Instant.now();
        logger.debug("inserting dataset finished ({})", Duration.between(starts, ends));
        return null;
    }

    @Override
    public DataSet getDataSet(String id) {
        logger.debug("loading dataset {}", id);
        Instant starts = Instant.now();

        DataSet dataSet = null;
        List<Object> dataSets = getClient().getList("datasets").readAll();
        for(Object o : dataSets){
            DataSet ds = (DataSet) o;
            if(ds.getId().equals(id)){
                dataSet = ds;
            }
        }
        if(dataSet == null){return null;}

        List nodes = getClient().getList("nodes#" + id).readAll();
        for(Object n : nodes){
            dataSet.getNodes().add((CoriaNode)n);
        }
        List edges = getClient().getList("edges#" + id);
        for(Object e : edges){
            dataSet.getEdges().add((CoriaEdge)e);
        }
        List minfos = getClient().getList("minfos#" + id);
        for(Object mi : minfos){
            dataSet.getMetricInfos().add((MetricInfo)mi);
        }
        Map<String, String> attrs = getClient().getMap("attrs#" + id);
        for(String key : attrs.keySet()){
            dataSet.getAttributes().put(key, attrs.get(key));
        }

        Instant ends = Instant.now();
        logger.debug("loading dataset finished ({})", Duration.between(starts, ends));
        return dataSet;
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
    synchronized public String updateDataSet(DataSet dataSet) {
        logger.debug("updating dataset {}", dataSet.getId());
        Instant starts = Instant.now();

        DataSet dbDataSet = null;
        List<Object> foundDataSets = getClient().getList("datasets").readAll();
        for(Object o : foundDataSets){
            DataSet ds = (DataSet) o;
            if(ds.getId().equals(dataSet.getId())){
                dbDataSet = getDataSet(ds.getId());
            }
        }

        if(dbDataSet == null){return null;}

        Instant check = Instant.now();
        logger.debug("  syncing nodes...");
        List<CoriaNode> syncedNodes = syncNodes(dbDataSet.getNodes(), dataSet.getNodes());
        dataSet.setNodes(null);
        Instant endCheck = Instant.now();
        logger.debug("  syncing nodes finished ({})", Duration.between(check, endCheck));

        check = Instant.now();
        logger.debug("  syncing edges...");
        List<CoriaEdge> syncedEdges = syncEdges(dbDataSet.getEdges(), dataSet.getEdges());
        dataSet.setEdges(null);
        endCheck = Instant.now();
        logger.debug("  syncing edges finished ({})", Duration.between(check, endCheck));

        dataSet.setMetricInfos(null);       //todo

        check = Instant.now();
        logger.debug("  syncing attrs...");
        Map<String, String> syncedAttrs = syncAttributes(dbDataSet.getAttributes(), dataSet.getAttributes());
        dataSet.setAttributes(null);
        endCheck = Instant.now();
        logger.debug("  syncing attrs finished ({})", Duration.between(check, endCheck));

        // 1. Store dataset itself (no nested objects)
        RList<DataSet> dataSets = getClient().getList("datasets");
        for(DataSet ds : dataSets){
            if(ds.getId().equals(dataSet.getId())){
                ds.setEdgesCount(syncedEdges.size());
                ds.setNodesCount(syncedNodes.size());
                ds.setName(dataSet.getName());
                ds.setNotificationEmails(dataSet.getNotificationEmails());
                break;
            }
        }

        getClient().getList("nodes#" + dataSet.getId()).clear();
        getClient().getList("nodes#" + dataSet.getId()).addAll(syncedNodes);

        getClient().getList("edges#" + dataSet.getId()).clear();
        getClient().getList("edges#" + dataSet.getId()).addAll(syncedEdges);

        getClient().getMap("attrs#" + dataSet.getId()).clear();
        getClient().getMap("attrs#" + dataSet.getId()).putAll(syncedAttrs);

        Instant ends = Instant.now();
        logger.debug("updating dataset finished ({})", Duration.between(starts, ends));

        return null;
    }

    private void updateDataSetAndAttributes(DataSet updatedDataSet, DataSet dbDataSet){
        dbDataSet.setEdgesCount(updatedDataSet.getEdges().size());
        dbDataSet.setNodesCount(updatedDataSet.getNodes().size());
        dbDataSet.setName(updatedDataSet.getName());
        dbDataSet.setNotificationEmails(updatedDataSet.getNotificationEmails());

        RMap<String, String> dbAttrs = getClient().getMap("attrs#" + dbDataSet.getId());

        for(String key : updatedDataSet.getAttributes().keySet()){
            dbAttrs.put(key, updatedDataSet.getAttribute(key));
        }
    }

    private void updateNodes(List<CoriaNode> updatedNodes, String dataSetId){
        RList<CoriaNode> dbNodes = getClient().getList("nodes#" + dataSetId);
        for(CoriaNode n : updatedNodes){
            Optional<CoriaNode> optNode = dbNodes.stream().filter(coriaNode -> coriaNode.getId().equals(n.getId())).findFirst();
            if(optNode.isPresent()){
                CoriaNode dbNode = optNode.get();
                dbNode.setName(n.getName());
                dbNode.setAsid(n.getAsid());
                dbNode.setRiscScore(n.getRiscScore());
                for(String key : n.getAttributes().keySet()){
                    dbNode.setAttribute(key, n.getAttribute(key));
                }
            }else{
                getClient().getList("nodes#" + dataSetId).add(n);
            }
        }
    }

    private void updateEdges(List<CoriaEdge> updatedEdges, String dataSetId){
        RList<CoriaEdge> dbEdges = getClient().getList("edges#" + dataSetId);
        for(CoriaEdge e : updatedEdges){
            Optional<CoriaEdge> optEdge = dbEdges.stream().filter(coriaEdge -> coriaEdge.getId().equals(e.getId())).findFirst();
            if(optEdge.isPresent()){
                CoriaEdge dbEdge = optEdge.get();
                dbEdge.setName(e.getName());
                dbEdge.setSourceNode(e.getSourceNode());
                dbEdge.setDestinationNode(e.getDestinationNode());
                for(String key : e.getAttributes().keySet()){
                    dbEdge.setAttribute(key, e.getAttribute(key));
                }
            }else{
                getClient().getList("edges#" + dataSetId).add(e);
            }
        }
    }

    private Map<String, String> syncAttributes(Map<String, String> old, Map<String, String> updated){
        Map<String, String> synced = new HashMap<>();

        for(String uk : updated.keySet()){
            boolean found = false;
            for(String ok : old.keySet()){
                if (uk.equals(ok)) {
                    found = true;
                    synced.put(ok, updated.get(uk));
                }
            }
            if(!found){
                synced.put(uk, updated.get(uk));
            }
        }

        return synced;
    }

    private List<CoriaEdge> syncEdges(List<CoriaEdge> old, List<CoriaEdge> updated){
        ArrayList<CoriaEdge> synced = new ArrayList<>();

        for(CoriaEdge ue : updated){
            boolean found = false;
            if(ue.getAttributes().size() == 0){
                //if there are no attributes we skip the comparison
                //since it takes quite a long time to process
                synced.add(ue);
                continue;
            }
            for(CoriaEdge oe : old){
                if(ue.getId().equals(oe.getId())){
                    found = true;
                    oe.setName(ue.getName());
                    oe.setSourceNode(ue.getSourceNode());
                    oe.setDestinationNode(ue.getDestinationNode());
                    for(String key : ue.getAttributes().keySet()){
                        oe.setAttribute(key, ue.getAttribute(key));
                    }
                    synced.add(oe);
                }
            }
            if(!found){
                synced.add(ue);
            }
        }

        return synced;
    }

    private List<CoriaNode> syncNodes(List<CoriaNode> old, List<CoriaNode> updated){
        ArrayList<CoriaNode> synced = new ArrayList<>();

        for(CoriaNode un : updated){
            boolean found = false;
            for(CoriaNode on : old){
                if(un.getId().equals(on.getId())){
                    found = true;
                    on.setName(un.getName());
                    on.setAsid(un.getAsid());
                    on.setRiscScore(un.getRiscScore());
                    logger.trace("### updating node {}, old attribute count: {}", on.getName(), on.getAttributes().size());
                    int oldSize = on.getAttributes().size();
                    logger.trace("###\tnew attribute count: {}", un.getAttributes().keySet().size());
                    for(String key : un.getAttributes().keySet()){
                        on.setAttribute(key, un.getAttribute(key));
                    }
                    synced.add(on);
                }
            }
            if(!found){
                synced.add(un);
            }
        }

        return synced;
    }

    @Override
    public void deleteDataSet(DataSet dataSet) {
        deleteDataSet(dataSet.getId());
    }

    @Override
    public void deleteDataSet(String id) {
        logger.debug("deleting dataset {}", id);
        Instant starts = Instant.now();

        RList<DataSet> dataSets = getClient().getList("datasets");
        int index = 0;
        for(DataSet ds : dataSets){
            if(ds.getId().equals(id)){
                dataSets.remove(index);
                break;
            }
            index++;
        }

        Instant ends = Instant.now();
        logger.debug("deleting dataset finished ({})", Duration.between(starts, ends));
    }

    @Override
    public StorageStatus getStorageStatus() {
        StorageStatus status = new StorageStatus(true, null);

        try {
            RedissonClient client = Redisson.create(config);

        } catch (Exception e) {

        }
        if(client == null){
            status.setReadyToUse(false);
            status.setMessage("Connection was not successful");
        }

        return status;
    }

    @Override
    public void dispose() {
        logger.debug("closing database connection for {}", getName());
        if(this.client != null){
            client.shutdown();
            config = null;
        }
    }

    private RedissonClient getClient(){
        return client;
    }

    @Override
    public String toString() {
        return "RedisStorageModule{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
