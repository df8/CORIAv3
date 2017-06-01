package com.bigbasti.coria.data;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.db.StorageStatus;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
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
        return null;
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

    @Override
    public void updateDataSet(DataSet dataSet) {

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
