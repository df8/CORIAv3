package com.coria.v3.metrics.python;

import com.coria.v3.dbmodel.*;
import com.coria.v3.metrics.MetricAlgorithmType;
import com.coria.v3.repository.RepositoryManager;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 */
public class FileSystemUtility {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemUtility.class);

    final HashMap<NodeEntity, String> nodeRenumbering1;
    final HashMap<String, NodeEntity> nodeRenumbering2;
    final HashMap<String, EdgeEntity> edgeRenumbering;

    public FileSystemUtility() {
        this.nodeRenumbering1 = new HashMap<>();
        this.nodeRenumbering2 = new HashMap<>();
        this.edgeRenumbering = new HashMap<>();
    }

    private AtomicInteger id;

    private String renumberNode(NodeEntity nodeEntity, boolean prepareForNodeMetricResults) {
        String nodeRenumberedId = nodeRenumbering1.get(nodeEntity);
        if (nodeRenumberedId == null) {
            nodeRenumberedId = String.valueOf(id.getAndIncrement());
            nodeRenumbering1.put(nodeEntity, nodeRenumberedId);
            if (prepareForNodeMetricResults)
                nodeRenumbering2.put(nodeRenumberedId, nodeEntity);
        }
        return nodeRenumberedId;
    }

    public void copyAllEdgesToTextFile(DatasetEntity datasetEntity, String requestFilePath, String separator, boolean prepareForNodeMetricResults, boolean prepareForEdgeMetricResults) throws Exception {
        id = new AtomicInteger(0);
        try (PrintWriter pw = new PrintWriter(new FileWriter(requestFilePath))) {
            datasetEntity
                    .getEdges()
                    .stream()
                    .sorted(Comparator.comparing(EdgeEntity::getName))
                    .forEach(edgeEntity -> {
                        String sourceNodeRenumberedId = renumberNode(edgeEntity.getNodeSource(), prepareForNodeMetricResults);
                        String targetNodeRenumberedId = renumberNode(edgeEntity.getNodeTarget(), prepareForNodeMetricResults);

                        if (prepareForEdgeMetricResults) {
                            edgeRenumbering.put(sourceNodeRenumberedId + "," + targetNodeRenumberedId, edgeEntity);
                        }

                        pw.println(sourceNodeRenumberedId + separator + targetNodeRenumberedId);
                    });
        }
    }

    public void copyAllShortestPathLengthsToTextFile(List<ShortestPathLengthEntity> allPairsShortestPathLengthList, String requestFilePath, String separator, boolean prepareForNodeMetricResults) throws Exception {

        id = new AtomicInteger(0);
        try (PrintWriter pw = new PrintWriter(new FileWriter(requestFilePath))) {
            allPairsShortestPathLengthList
                    .stream()
                    .sorted(Comparator.comparing(ShortestPathLengthEntity::getNodeTargetName))
                    .sorted(Comparator.comparing(ShortestPathLengthEntity::getNodeSourceName))
                    .forEach(splEntity -> {
                        String sourceNodeRenumberedId = renumberNode(splEntity.getNodeSource(), prepareForNodeMetricResults);
                        String targetNodeRenumberedId = renumberNode(splEntity.getNodeTarget(), prepareForNodeMetricResults);
                        pw.println(sourceNodeRenumberedId + separator + targetNodeRenumberedId + separator + splEntity.getDistance());
                    });
        }
    }

    public void copyAllNodeMetricResultsToTextFile(DatasetEntity datasetEntity, HashMap<NodeEntity, HashMap<String, Double>> nodeMetricResultsMap, List<String> metricVariantKeys, String requestFilePath, String separator, boolean prepareForNodeMetricResults) throws Exception {
        id = new AtomicInteger(0);
        try (PrintWriter pw = new PrintWriter(new FileWriter(requestFilePath))) {
            pw.print("node" + separator);
            for (int i = 0; i < metricVariantKeys.size(); i++) {
                if (i < metricVariantKeys.size() - 1) {
                    pw.print(metricVariantKeys.get(i) + separator);
                } else {
                    pw.println(metricVariantKeys.get(i));
                }
            }

            for (var x : nodeMetricResultsMap.entrySet()) {
                String nodeRenumberedId = renumberNode(x.getKey(), prepareForNodeMetricResults);
                pw.print(nodeRenumberedId + separator);
                var metricResults = x.getValue();
                for (int i = 0; i < metricVariantKeys.size(); i++) {
                    if (i < metricVariantKeys.size() - 1) {
                        pw.print(metricResults.get(metricVariantKeys.get(i)) + separator);
                    } else {
                        pw.println(metricResults.get(metricVariantKeys.get(i)));
                    }
                }
            }
        }
    }

    private byte[] UUIDtoBytes(UUID uuid) {
        byte[] uuidBytes = new byte[16];
        ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        return uuidBytes;
    }

    /**
     * This method reads a CSV text file and inserts rows into the table node_metric_result in batches using the native JDBC interface
     * to improve the processing time of a large number of DB inserts without the overheads experienced when using JPA.
     * This is a small, but effective optimization.
     *
     * @param filePath
     * @param metricEntityList
     * @param repositoryManager
     * @throws Exception
     */
    private void copyNodeMetricResultsFromTextFile(String filePath, List<MetricEntity> metricEntityList, RepositoryManager repositoryManager) {
        Session session = (Session) repositoryManager.getEntityManager().getDelegate();
        session.doWork(connection -> {
            int count = 0;
            Instant starts = Instant.now();
            try (FileInputStream inputStream = new FileInputStream(filePath);
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader in = new BufferedReader(reader)
            ) {
                int lineNumber = 0;
                String line;
                String[] columns = null;
                List<MetricEntity> metricEntityListSorted = null;
                String[] keyValuePair;
                NodeEntity nodeEntity;

                int expectedColumnsCount = (int) (1 + metricEntityList.stream().filter(metricEntity -> metricEntity.getMetricAlgorithm().getType() == MetricAlgorithmType.Node).count());

                PreparedStatement insertNMRQuery = connection.prepareStatement("INSERT INTO `node_metric_result` (metric_id, node_id, metric_result_value ) VALUES (?,?,?)");
                while ((line = in.readLine()) != null) {
                    if (columns == null) {
                        columns = line.split("[\\s,]+");
                        // Sort the metrics the same order as the response file columns so we can access them by the index.
                        List<String> columnsList = Arrays.asList(columns);
                        metricEntityListSorted = metricEntityList
                                .stream()
                                .filter(m -> columnsList.contains(m.getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getId()))
                                .sorted(Comparator.comparingInt(m -> columnsList.indexOf(m.getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getId())))
                                .collect(Collectors.toList());
                        if (expectedColumnsCount != columnsList.size()) {
                            throw new Exception("Syntax error in CSV file " + filePath + " line " + lineNumber + ": Invalid number of columns. Expected: " + expectedColumnsCount + ", got: " + columnsList.size());
                        }
                    } else {
                        keyValuePair = line.split("[\\s,]+");
                        if (keyValuePair.length != columns.length) {
                            throw new Exception("Syntax error in CSV file " + filePath + " line " + lineNumber);
                        }
                        nodeEntity = nodeRenumbering2.get(keyValuePair[0]);

                        for (int i = 1; i < columns.length; i++) {
                            insertNMRQuery.setBytes(1, UUIDtoBytes(metricEntityListSorted.get(i - 1).getId()));
                            insertNMRQuery.setBytes(2, UUIDtoBytes(nodeEntity.getId()));
                            insertNMRQuery.setDouble(3, Double.parseDouble(keyValuePair[i]));
                            insertNMRQuery.addBatch();
                            // JPA style insert would be:
                            // nodeEntity.addMetricResult(metricEntityListSorted.get(i - 1), Double.parseDouble(keyValuePair[i]));
                        }
                        count++;
                    }
                    lineNumber++;
                }
                if (count > 0) {
                    var res = insertNMRQuery.executeBatch();
                    logger.debug("Inserted {} node metric result rows ({} nodes with {} metric results for each node).", res.length, count, columns.length - 1);
                    insertNMRQuery.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * TODO /3 rewrite with using native JDBC instead of JPA to improve speed.
     */
    public void copyAllMetricResultsFromTextFile(String filePath, List<MetricEntity> metricEntityList, PythonMetricResponseFileType pythonMetricResponseFileType, RepositoryManager repositoryManager) throws Exception {
        if (metricEntityList.size() == 0) {
            throw new Exception("Invalid operation: Empty metric list");
        }
        if (pythonMetricResponseFileType == PythonMetricResponseFileType.Node) {
            copyNodeMetricResultsFromTextFile(filePath, metricEntityList, repositoryManager);
        } else {
            Map<String, MetricEntity> metricMap = new HashMap<>();
            if (pythonMetricResponseFileType == PythonMetricResponseFileType.Dataset || pythonMetricResponseFileType == PythonMetricResponseFileType.Edge || pythonMetricResponseFileType == PythonMetricResponseFileType.ExecutionTimestamps)
                metricMap = metricEntityList.stream().collect(Collectors.toMap(metricEntity1 -> metricEntity1.getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getId(), metricEntity1 -> metricEntity1));

            ArrayList<ShortestPathLengthEntity> splList = new ArrayList<>();
            int count = 0;
            Instant starts = Instant.now();
            try (FileInputStream inputStream = new FileInputStream(filePath);
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader in = new BufferedReader(reader)
            ) {
                int lineNumber = 0;
                String line;
                String[] columns = null;
                String[] keyValuePair;
                NodeEntity nodeEntity;

                while ((line = in.readLine()) != null) {
                    if ((pythonMetricResponseFileType == PythonMetricResponseFileType.Dataset ||
                            pythonMetricResponseFileType == PythonMetricResponseFileType.Edge ||
                            pythonMetricResponseFileType == PythonMetricResponseFileType.ShortestPathLength ||
                            pythonMetricResponseFileType == PythonMetricResponseFileType.LayoutPosition) && columns == null) {
                        columns = line.split("[\\s,]+");
                    } else {
                        switch (pythonMetricResponseFileType) {
                            case Dataset:
                                keyValuePair = line.split("[\\s,]+");
                                if (keyValuePair.length != columns.length)
                                    throw new Exception("Syntax error in CSV file " + filePath + " line " + lineNumber);
                                for (int i = 0; i < columns.length; i++) {
                                    metricEntityList.get(0).getDataset().addMetricResult(metricMap.get(columns[i]), Double.parseDouble(keyValuePair[i]));
                                    count++;
                                }
                                break;
                            case Edge:
                                keyValuePair = line.split("[\\s,]+");
                                if (keyValuePair.length != columns.length)
                                    throw new Exception("Syntax error in CSV file " + filePath + " line " + lineNumber);
                                EdgeEntity edgeEntity = edgeRenumbering.get(keyValuePair[0] + "," + keyValuePair[1]);
                                if (edgeEntity == null) {
                                    edgeEntity = edgeRenumbering.get(keyValuePair[1] + "," + keyValuePair[0]);
                                    if (edgeEntity == null) {
                                        throw new Exception("Edge not found: " + keyValuePair[0] + " <-> " + keyValuePair[1]);
                                    }
                                }
                                for (int i = 2; i < columns.length; i++) {
                                    edgeEntity.addMetricResult(metricMap.get(columns[i]), Double.parseDouble(keyValuePair[i]));
                                    count++;
                                }
                                break;
                            case ShortestPathLength:
                                //TODO JSON
                                keyValuePair = line.split("[\\s,]+");
                                splList.add(new ShortestPathLengthEntity(metricEntityList.get(0), nodeRenumbering2.get(keyValuePair[0]), nodeRenumbering2.get(keyValuePair[1]), Integer.parseInt(keyValuePair[2])));
                                count++;
                                break;
                            case LayoutPosition:
                                keyValuePair = line.split("[\\s,]+");
                                nodeEntity = nodeRenumbering2.get(keyValuePair[0]);
                                nodeEntity.addLayoutPosition(metricEntityList.get(0), Double.parseDouble(keyValuePair[1]), Double.parseDouble(keyValuePair[2]));
                                count++;
                                //TODO /3: Space for improvement: Perform a min-max normalization of the x and y coordinates before writing into DB.
                                break;
                            case ExecutionTimestamps:
                                keyValuePair = line.split("[\\s,]+");
                                // keyValuePair[0] contains the MetricAlgorithmVariantId
                                MetricEntity metricEntity = metricMap.get(keyValuePair[0]);
                                metricEntity.setStarted(Timestamp.from(Instant.ofEpochMilli(Long.parseLong(keyValuePair[1]))));
                                metricEntity.setFinished(Timestamp.from(Instant.ofEpochMilli(Long.parseLong(keyValuePair[2]))));
                                break;
                            default:
                                throw new Exception("Unsupported operation.");
                        }
                    }
                    lineNumber++;
                }
                if (splList.size() > 0) {
                    metricEntityList.get(0).getShortestPathLengths().addAll(splList);
                }
                for (var metricEntity : metricEntityList)
                    repositoryManager.getMetricRepository().save(metricEntity);
            } catch (Exception e) {
                logger.error("could not insert attribute: {}", e.getMessage());
                e.printStackTrace();
                throw e;
            }
            logger.debug("[TABLE node_metric_results] Inserted {} rows ({})", count, Duration.between(starts, Instant.now()));
        }
    }

    HashMap<String, String> nodeRemapping;

    public void removeFile(String requestFilePath) throws Exception {
        File file = new File(requestFilePath);
        if (!file.delete()) {
            System.out.println("WARN: Could not delete file " + requestFilePath);
            //throw new Exception("Could not delete file " + requestFilePath);
        }
    }
    //TODO /3 unused. Check again later whether "streaming insert" can be fixed to work.
    /*public void copyAllEdgesFromStreamToTextFile(DatasetEntity datasetEntity, String requestFilePath, String separator, MetricAlgorithmType metricAlgorithmType, RepositoryManager repositoryManager) throws Exception {
        nodeRemapping = new HashMap<>();
        AtomicInteger id = new AtomicInteger(0);
        try (Stream<EdgeEntity> edgeStream = repositoryManager.getEdgeRepository().streamAllByDataset_Id(datasetEntity.getId());
             PrintWriter pw = new PrintWriter(new FileWriter(requestFilePath))) {
            edgeStream.forEach(edgeEntity -> {
                String source_uuid = edgeEntity.getNodeSourceId().toString();
                String source_int = nodeRemapping.get(source_uuid);
                if (source_int == null) {
                    source_int = String.valueOf(id.incrementAndGet());
                    nodeRemapping.put(source_uuid, source_int);
                    nodeRemapping.put(source_int, source_uuid);
                }

                String target_uuid = edgeEntity.getNodeTargetId().toString();
                String target_int = nodeRemapping.get(target_uuid);

                if (target_int == null) {
                    target_int = String.valueOf(id.incrementAndGet());
                    nodeRemapping.put(target_uuid, target_int);
                    nodeRemapping.put(target_int, target_uuid);
                }

                //pw.println(edgeEntity.getNodeSourceId().toString() + separator + edgeEntity.getNodeTargetId().toString());
                pw.println(source_int + separator + target_int);
                //entityManager.detach(edgeEntity);
                //
                // Working with Spring Data, Stream<T> and entityManager.detach() allows to reduce the working heap size,
                // as we avoid loading all nodes into memory first. Unfortunately calling entityManager.detach(edgeEntity) has not worked for me
                // due to some JPA transaction issues.
                // https://stackoverflow.com/questions/26795436/spring-jparepository-detach-and-attach-entity
                // https://www.geekyhacker.com/2019/03/26/high-performance-data-fetching-using-spring-data-jpa-stream/
                // https://codete.com/blog/5-common-spring-transactional-pitfalls/
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }*/
}