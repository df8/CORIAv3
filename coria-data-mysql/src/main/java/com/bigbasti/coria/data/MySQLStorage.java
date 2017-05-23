package com.bigbasti.coria.data;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.db.StorageStatus;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

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
    public void addDataSet(DataSet dataSet) {

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
        return null;
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
            Class.forName(dbDriver);
            Connection con= DriverManager.getConnection(dbUrl, dbUser, dbPass);
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

    @Override
    public String toString() {
        return "MySQLStorage{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
