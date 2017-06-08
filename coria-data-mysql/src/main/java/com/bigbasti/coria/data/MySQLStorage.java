package com.bigbasti.coria.data;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.db.StorageStatus;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import com.google.common.base.Strings;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Sebastian Gross
 */
@Component
public class MySQLStorage implements DataStorage {

    private Logger logger = LoggerFactory.getLogger(MySQLStorage.class);

    @Autowired
    Environment env;
    private String dbUrl;
    private String dbUser;
    private String dbPass;
    private String dbSchema;
    private String dbDriver;

    private final int BATCH_SIZE = 5000;

    @PostConstruct
    public void checkDatabaseSetup(){
        logger.debug("checking database schema");

        dbUrl = env.getProperty("coria.db.mysql.jdbcurl");
        dbUser = env.getProperty("coria.db.mysql.username");
        dbPass = env.getProperty("coria.db.mysql.password");
        dbSchema = env.getProperty("coria.db.mysql.schema");
        dbDriver = env.getProperty("coria.db.mysql.driver");

        logger.debug("trying to connect to database " + dbUrl + " (" + dbSchema + ") using " + dbUser + "/" + dbPass.replaceAll(".*", "*") + " with driver {}", dbDriver);

        if(getStorageStatus().isReadyToUse()){
            migrateDatabaseIfNeeded(dbUrl, dbUser, dbPass, dbSchema);
        }else{
            logger.error("database seems not to be ready to use");
        }
        logger.debug("database check completed");
    }

    private void migrateDatabaseIfNeeded(String url, String user, String pass, String schema){
        logger.info("automatic db migration is active - checking for outstanding migration");
        try {
            Flyway migration = new Flyway();
            migration.setDataSource(url, user, pass);
            migration.setSchemas(schema);
            migration.migrate();
        } catch (Exception e) {
            logger.error("automatic migration failed because {}", e.getMessage());
        }
        logger.info("automatic migration finished");
    }

    @Override
    public String getIdentification() {
        return "coria-data-mysql";
    }

    @Override
    public String getName() {
        return "MySQL Datenbank Adapter";
    }

    @Override
    public String getDescription() {
        return "Connects CORIA to the MySQL Storage, the access configuration needs to be setup in the application.properties of this module";
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
    public String addMetricInfo(MetricInfo metric, String datasetId) {
        final String INSERT_METRIC = "INSERT INTO " + dbSchema + ".`metrics` (`dataset_id`, `name`, `shortcut`, `provider`, `technology`, `started`, `status`, `type`, `value`, `message`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int retVal = 0;

        logger.debug("inserting metric for dataset {}", datasetId);
        Instant starts = Instant.now();
        try (Connection con = getConnection()){
            try(PreparedStatement stmt = con.prepareStatement(INSERT_METRIC, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, datasetId);
                stmt.setString(2, metric.getName());
                stmt.setString(3, metric.getShortcut());
                stmt.setString(4, metric.getProvider());
                stmt.setString(5, metric.getTechnology());
                stmt.setTimestamp(6, new Timestamp(metric.getExecutionStarted().getTime()));
                stmt.setString(7, metric.getStatus().name());
                stmt.setString(8, metric.getType().name());
                stmt.setString(9, metric.getValue());
                stmt.setString(10, metric.getMessage());

                stmt.executeUpdate();
                con.commit();
                //get the autoincremented dataset id
                ResultSet rs = stmt.getGeneratedKeys();
                rs.next();
                retVal = rs.getInt(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Inserting metric failed: {}", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
        Instant ends = Instant.now();
        logger.debug("inserting metric finished ({})", Duration.between(starts, ends));

        return String.valueOf(retVal);
    }

    @Override
    public String updateMetricInfo(MetricInfo metricInfo) {
        final String UPDATE_METRIC = "UPDATE " + dbSchema + ".`metrics` SET `name`=?, `shortcut`=?, `provider`=?, `technology`=?, `started`=?, `finished`=?, `status`=?, `type`=?, `value`=?, `message`=? WHERE `id`=?;";

        logger.debug("updating {}", metricInfo);
        if(Strings.isNullOrEmpty(metricInfo.getId())){
            logger.warn("cannot update metricinfo without primary key present");
            return "error: no primary key present";
        }

        Instant starts = Instant.now();
        try (Connection con = getConnection()){
            try(PreparedStatement stmt = con.prepareStatement(UPDATE_METRIC, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, metricInfo.getName());
                stmt.setString(2, metricInfo.getShortcut());
                stmt.setString(3, metricInfo.getProvider());
                stmt.setString(4, metricInfo.getTechnology());
                stmt.setTimestamp(5, new Timestamp(metricInfo.getExecutionStarted().getTime()));
                stmt.setTimestamp(6, new Timestamp(metricInfo.getExecutionFinished().getTime()));
                stmt.setString(7, metricInfo.getStatus().name());
                stmt.setString(8, metricInfo.getType().name());
                stmt.setString(9, metricInfo.getValue());
                stmt.setString(10, metricInfo.getMessage());
                stmt.setInt(11, Integer.valueOf(metricInfo.getId()));

                stmt.executeUpdate();
                con.commit();
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("updating metric failed: {}", e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
        Instant ends = Instant.now();
        logger.debug("updating metric finished ({})", Duration.between(starts, ends));

        return null;
    }

    //TODO: also add attributes
    @Override
    public String addDataSet(DataSet dataSet) {
        final String INSERT_DATASET = "INSERT INTO " + dbSchema + ".`datasets` (`name`, `created`) VALUES (?, ?)";
        final String INSERT_EDGE = "INSERT INTO " + dbSchema + ".`edges` (`dataset_id`, `name`, `source`, `destination`) VALUES (?, ?, ?, ?);";
        final String INSERT_NODE = "INSERT INTO " + dbSchema + ".`nodes` (`dataset_id`, `name`) VALUES (?, ?);";

        int dataSetKey = 0;
        logger.debug("inserting dataset...");
        Instant starts = Instant.now();
        try (Connection con = getConnection()){
            try(PreparedStatement stmt = con.prepareStatement(INSERT_DATASET, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, dataSet.getName());
                stmt.setTimestamp(2, new Timestamp(dataSet.getCreated().getTime()));

                stmt.executeUpdate();
                con.commit();
                //get the autoincremented dataset id
                ResultSet rs = stmt.getGeneratedKeys();
                rs.next();
                dataSetKey = rs.getInt(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("Inserting new DataSet failed: {}", e.getMessage());
            return e.getMessage();
        }
        Instant ends = Instant.now();
        logger.debug("inserting dataset finished ({})", Duration.between(starts, ends));

        starts = Instant.now();
        if(dataSet.getEdges() != null && dataSet.getEdges().size() > 0) {
            logger.debug("inserting nodes...");
            //first we must insert the nodes into the db
            try (Connection con = getConnection()){
                con.setAutoCommit(false);
                try(PreparedStatement stmt = con.prepareStatement(INSERT_NODE, Statement.RETURN_GENERATED_KEYS)) {
                    int batchCounter = 0;
                    for (CoriaNode node : dataSet.getNodes()) {
                        stmt.clearParameters();
                        stmt.setInt(1, dataSetKey);
                        stmt.setString(2, node.getName());
                        stmt.addBatch();

                        if (batchCounter++ > BATCH_SIZE) {
                            stmt.executeBatch();
                            batchCounter = 0;
                        }
                    }
                    stmt.executeBatch();
                    con.commit();
                }
            } catch (SQLException | ClassNotFoundException e) {
                logger.error("Inserting Nodes failed: {}", e.getMessage());
                return e.getMessage();
            }
            ends = Instant.now();
            logger.debug("inserting nodes finished ({})", Duration.between(starts, ends));

            starts = Instant.now();
            logger.debug("inserting edges...");
            //then we add the edges
            try (Connection con = getConnection()){
                con.setAutoCommit(false);
                try(PreparedStatement stmt = con.prepareStatement(INSERT_EDGE, Statement.RETURN_GENERATED_KEYS)) {
                    int batchCounter = 0;
                    for (CoriaEdge edge : dataSet.getEdges()) {
                        stmt.clearParameters();
                        stmt.setInt(1, dataSetKey);
                        stmt.setString(2, edge.getName());
                        stmt.setString(3, edge.getSourceNode().getName());
                        stmt.setString(4, edge.getDestinationNode().getName());
                        stmt.addBatch();
                        if (batchCounter++ > BATCH_SIZE) {
                            stmt.executeBatch();
                            batchCounter = 0;
                        }
                    }
                    stmt.executeBatch();
                    con.commit();
                }
            } catch (SQLException | ClassNotFoundException e) {
                logger.error("Inserting new Edges failed: {}", e.getMessage());
                return e.getMessage();
            }
            ends = Instant.now();
            logger.debug("inserting edges finished ({})", Duration.between(starts, ends));
        }
        return null;
    }

    @Override
    public DataSet getDataSet(String id) {
        return loadDataSet(id, true);
    }

    private DataSet loadDataSet(String id, boolean toShort) {
        logger.debug("loading dataset {}", id);
        final String SELECT_DATASET = "SELECT * FROM " + dbSchema + ".datasets where id = ?";
        final String SELECT_METRIC_FOR_DATASET = "SELECT * FROM " + dbSchema + ".metrics where dataset_id = ?";
        final String SELECT_ATTRIBUTE_FOR_DATASET = "SELECT * FROM " + dbSchema + ".attributes where dataset_id = ?";
        final String SELECT_NODES_FOR_DATASET = "SELECT * FROM " + dbSchema + ".nodes where dataset_id = ?";
        final String SELECT_EDGES_FOR_DATASET = "SELECT * FROM " + dbSchema + ".edges where dataset_id = ?";

        Connection con = null;
        try {
            con = getConnection();
            if(con == null){
                logger.error("could not establish db connection!");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        DataSet dataset = null;
        Instant starts = Instant.now();
        try {
            try (PreparedStatement stmt = con.prepareStatement(SELECT_DATASET)) {
                stmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    dataset = toDataSet(rs);
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded dataset ({})", Duration.between(starts, ends));
        } catch (SQLException e) {
            logger.error("could not load dataset: {}", e.getMessage());
            e.printStackTrace();
        }

        //use the dataset information to load the rest of the information
        // ======== METRICS

        logger.debug("loading metrics for dataset...");
        starts = Instant.now();
        try {
            try (PreparedStatement stmt = con.prepareStatement(SELECT_METRIC_FOR_DATASET)) {
                stmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    dataset.getMetricInfos().add(toMetric(rs));
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded metrics for dataset ({})", Duration.between(starts, ends));
        } catch (SQLException e) {
            logger.error("could not load metrics: {}", e.getMessage());
            e.printStackTrace();
        }

        //======== ATTRIBUTES

        logger.debug("loading attributes for dataset...");
        starts = Instant.now();
        try {
            try (PreparedStatement stmt = con.prepareStatement(SELECT_ATTRIBUTE_FOR_DATASET)) {
                stmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    dataset.setAttribute(rs.getString("key"), rs.getString("value"));
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded attributes for dataset ({})", Duration.between(starts, ends));
        } catch (SQLException e) {
            logger.error("could not load attributes: {}", e.getMessage());
            e.printStackTrace();
        }

        //======== NODES

        logger.debug("loading nodes for dataset...");
        starts = Instant.now();
        try {
            try (PreparedStatement stmt = con.prepareStatement(SELECT_NODES_FOR_DATASET)) {
                stmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    dataset.getNodes().add(toNode(rs, con));
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded nodes for dataset ({})", Duration.between(starts, ends));
        } catch (SQLException e) {
            logger.error("could not load nodes: {}", e.getMessage());
            e.printStackTrace();
        }

        //======== EDGES

        logger.debug("loading edges for dataset...");
        starts = Instant.now();
        try {
            try (PreparedStatement stmt = con.prepareStatement(SELECT_EDGES_FOR_DATASET)) {
                stmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    dataset.getEdges().add(toEdge(rs, dataset.getNodes(), con, toShort));
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded edges for dataset ({})", Duration.between(starts, ends));
        } catch (SQLException e) {
            logger.error("could not load edges: {}", e.getMessage());
            e.printStackTrace();
        }

        logger.debug("dataset loaded successful");

        return dataset;
    }

    @Override
    public DataSet getDataSetShort(String id) {
        return loadDataSet(id, false);
    }

    @Override
    public List<DataSet> getDataSets() {
        return null;
    }

    @Override
    public List<DataSet> getDataSetsShort() {
        logger.debug("loading short datasets...");
        final String SELECT_ALL_DATASETS_SHORT = "SELECT * FROM " + dbSchema + ".datasets";
        final String SELECT_METRIC_FOR_DATASET = "SELECT * FROM " + dbSchema + ".metrics where dataset_id = ?";
        final String SELECT_ATTRIBUTE_FOR_DATASET = "SELECT * FROM " + dbSchema + ".attributes where dataset_id = ?";

        List<DataSet> datasets = new ArrayList<>();
        Instant starts = Instant.now();
        try {
            try (Connection con = getConnection()) {
                try (PreparedStatement stmt = con.prepareStatement(SELECT_ALL_DATASETS_SHORT)) {
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()){
                        datasets.add(toDataSet(rs));
                    }
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded {} datasets ({})", datasets.size(), Duration.between(starts, ends));
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("could not load datasets: {}", e.getMessage());
            e.printStackTrace();
        }

        //use the dataset information to load the rest of the information
        // ======== METRICS

        logger.debug("loading metrics for datasets...");
        starts = Instant.now();
        try {
            try (Connection con = getConnection()) {
                for(DataSet ds : datasets){
                    try (PreparedStatement stmt = con.prepareStatement(SELECT_METRIC_FOR_DATASET)) {
                        stmt.setInt(1, Integer.parseInt(ds.getId()));
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()){
                            ds.getMetricInfos().add(toMetric(rs));
                        }
                    }
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded metrics for all datasets ({})", Duration.between(starts, ends));
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("could not load metrics: {}", e.getMessage());
            e.printStackTrace();
        }

        //======== ATTRIBUTES

        logger.debug("loading attributes for datasets...");
        starts = Instant.now();
        try {
            try (Connection con = getConnection()) {
                for(DataSet ds : datasets){
                    try (PreparedStatement stmt = con.prepareStatement(SELECT_ATTRIBUTE_FOR_DATASET)) {
                        stmt.setInt(1, Integer.parseInt(ds.getId()));
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()){
                            ds.setAttribute(rs.getString("key"), rs.getString("value"));
                        }
                    }
                }
            }
            Instant ends = Instant.now();
            logger.debug("loaded attributes for all datasets ({})", Duration.between(starts, ends));
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("could not load attributes: {}", e.getMessage());
            e.printStackTrace();
        }

        return datasets;
    }

    private MetricInfo toMetric(ResultSet rs) throws SQLException {
        MetricInfo m = new MetricInfo();
        m.setId(String.valueOf(rs.getInt("id")));
        m.setName(rs.getString("name"));
        m.setShortcut(rs.getString("shortcut"));
        m.setProvider(rs.getString("provider"));
        m.setTechnology(rs.getString("technology"));
        m.setExecutionStarted(rs.getDate("started"));
        m.setExecutionFinished(rs.getDate("finished"));
        m.setStatus(MetricInfo.MetricStatus.valueOf(rs.getString("status")));
        return m;
    }

    private DataSet toDataSet(ResultSet rs) throws SQLException {
        DataSet ds = new DataSet();
        ds.setId(String.valueOf(rs.getInt("id")));
        ds.setName(rs.getString("name"));
        ds.setCreated(rs.getDate("created"));
        if(!Strings.isNullOrEmpty(rs.getString("emails"))) {
            ds.setNotificationEmails(Arrays.stream(rs.getString("emails").split(",")).collect(Collectors.toList()));
        }
        return ds;
    }

    private CoriaNode toNode(ResultSet rs, Connection con) throws SQLException {
        CoriaNode node = new CoriaNode(rs.getString("name"));
        node.setId(String.valueOf(rs.getInt("id")));

        //TODO: Risc score lesen
        //======== ATTRIBUTES

        logger.trace("\tloading attributes for node {}...", node.getId());
        final String SELECT_ATTRIBUTE_FOR_NODE = "SELECT * FROM " + dbSchema + ".attributes where node_id = ?";
        Instant starts = Instant.now();
        try {
            try (PreparedStatement stmt = con.prepareStatement(SELECT_ATTRIBUTE_FOR_NODE)) {
                stmt.setInt(1, Integer.parseInt(node.getId()));
                ResultSet res = stmt.executeQuery();
                while (res.next()){
                    node.setAttribute(rs.getString("key"), res.getString("value"));
                }
            }
            Instant ends = Instant.now();
            logger.trace("\tloaded attributes for node ({})", Duration.between(starts, ends));
        } catch (SQLException e) {
            logger.error("\tcould not load attributes: {}", e.getMessage());
            e.printStackTrace();
        }

        return node;
    }

    private CoriaEdge toEdge(ResultSet rs, List<CoriaNode> nodes, Connection con, boolean connectedNodes) throws SQLException {
        CoriaEdge edge = new CoriaEdge(String.valueOf(rs.getString("name")));
        edge.setId(String.valueOf(rs.getInt("id")));

        String sourceid = rs.getString("source");
        String destid = rs.getString("destination");

        //======== CONNECTED NODES

        if(connectedNodes) {
            CoriaNode source = nodes.stream()
                    .filter(coriaNode -> coriaNode.getName().equals(sourceid))
                    .findFirst()
                    .get();
            CoriaNode dest = nodes.stream()
                    .filter(coriaNode -> coriaNode.getName().equals(destid))
                    .findFirst()
                    .get();
            edge.setSourceNode(source);
            edge.setDestinationNode(dest);
        }

        //======== ATTRIBUTES

        logger.trace("\tloading attributes for edge {}...", edge.getId());
        final String SELECT_ATTRIBUTE_FOR_NODE = "SELECT * FROM " + dbSchema + ".attributes where node_id = ?";
        Instant starts = Instant.now();
        try {
            try (PreparedStatement stmt = con.prepareStatement(SELECT_ATTRIBUTE_FOR_NODE)) {
                stmt.setInt(1, Integer.parseInt(edge.getId()));
                ResultSet res = stmt.executeQuery();
                while (res.next()){
                    edge.setAttribute(rs.getString("key"), res.getString("value"));
                }
            }
            Instant ends = Instant.now();
            logger.trace("\tloaded attributes for edge ({})", Duration.between(starts, ends));
        } catch (SQLException  e) {
            logger.error("\tcould not load attributes: {}", e.getMessage());
            e.printStackTrace();
        }

        return edge;
    }

    @Override
    public String updateDataSet(DataSet dataSet) {
        final String UPDATE_DATASET = "UPDATE " + dbSchema + ".`datasets` SET `name`=?, `created`=?, `emails`=? WHERE `id`=?;";

        logger.debug("updating dataset...");
        Instant starts = Instant.now();
        try (Connection con = getConnection()){
            try(PreparedStatement stmt = con.prepareStatement(UPDATE_DATASET)) {
                stmt.setString(1, dataSet.getName());
                stmt.setTimestamp(2, new Timestamp(dataSet.getCreated().getTime()));
                stmt.setString(3, String.join(",", dataSet.getNotificationEmails()));
                stmt.setLong(4, Long.valueOf(dataSet.getId()));

                stmt.executeUpdate();
                con.commit();
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("updating DataSet failed: {}", e.getMessage());
            return e.getMessage();
        }
        Instant ends = Instant.now();
        logger.debug("updating dataset finished ({})", Duration.between(starts, ends));

        //======= NODES

        starts = Instant.now();
        logger.debug("updating nodes...");
        try (Connection con = getConnection()){
            updateNodeList(dataSet.getNodes(), con);
        } catch (SQLException | ClassNotFoundException e) {
            logger.error("updating nodes failed: {}", e.getMessage());
            return e.getMessage();
        }
        ends = Instant.now();
        logger.debug("updating nodes finished ({})", Duration.between(starts, ends));

        //======= EDGES


        return null;
    }

    /**
     * updates a node in the database
     * 1. update node data
     * 2. update node attributes
     * @param nodes
     * @param con
     * @return
     * @throws SQLException
     */
    private String updateNodeList(List<CoriaNode> nodes, Connection con) throws SQLException {
        final String UPDATE_NODE = "UPDATE " + dbSchema + ".`nodes` SET `name`=?, `risc_score`=? WHERE `id`=?";
        final String UPDATE_ATTRIBUTE = "UPDATE " + dbSchema + ".`attributes` SET `key`=?, `value`=? WHERE `node_id`=? and `key`=?";
        final String INSERT_ATTRIBUTE_NODE = "INSERT INTO " + dbSchema + ".`attributes` (`node_id`, `key`, `value`) VALUES (?, ?, ?)";

        try(PreparedStatement stmtUpdateNode = con.prepareStatement(UPDATE_NODE)) {
            int batchCounterNodes = 0;
            for (CoriaNode node : nodes) {
                stmtUpdateNode.clearParameters();
                stmtUpdateNode.setString(1, node.getName());
                stmtUpdateNode.setString(2, node.getRiscScore());
                stmtUpdateNode.setLong(3, Long.valueOf(node.getId()));
                stmtUpdateNode.addBatch();
                if (batchCounterNodes++ > BATCH_SIZE) {
                    stmtUpdateNode.executeBatch();
                    batchCounterNodes = 0;
                }

                if(node.getAttributes() != null && node.getAttributes().size() > 0){
                    for (Map.Entry<String, String> attr : node.getAttributes().entrySet()) {
                        try (PreparedStatement stmtUpdateAttribute = con.prepareStatement(UPDATE_ATTRIBUTE)) {
                            stmtUpdateAttribute.setString(1, attr.getKey());
                            stmtUpdateAttribute.setString(2, attr.getValue());
                            stmtUpdateAttribute.setLong(3, Long.valueOf(node.getId()));
                            stmtUpdateAttribute.setString(4, attr.getKey());
                            int restult = stmtUpdateAttribute.executeUpdate();
                            if(restult == 0){
                                //attribute must be created
                                try (PreparedStatement stmtCreateAttribute = con.prepareStatement(INSERT_ATTRIBUTE_NODE)) {
                                    stmtCreateAttribute.setLong(1, Long.valueOf(node.getId()));
                                    stmtCreateAttribute.setString(2, attr.getKey());
                                    stmtCreateAttribute.setString(3, attr.getValue());
                                    stmtCreateAttribute.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
            stmtUpdateNode.executeBatch();
            con.commit();
        }

        return null;
    }

    @Override
    public void deleteDataSet(DataSet dataSet) {

    }

    @Override
    public void deleteDataSet(String id) {

    }

    @Override
    public StorageStatus getStorageStatus() {
        StorageStatus status = new StorageStatus(true, null);
        try {
            getConnection();
        } catch (ClassNotFoundException e) {
            logger.error("error while connecting to database: {}", e.getMessage());
            status.setReadyToUse(false);
            status.setMessage(e.getMessage());
        } catch (SQLException e) {
            logger.error("error while checking storage status: {}", e.getMessage());
            status.setReadyToUse(false);
            status.setMessage(e.getMessage());
        }
        return status;
    }

    private Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(dbDriver);
        return DriverManager.getConnection(dbUrl, dbUser, dbPass);
    }

    @Override
    public String toString() {
        return "MySQLStorage{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
