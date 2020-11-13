package com.coria.v3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphql.spring.boot.test.GraphQLResponse;
import org.assertj.core.data.Offset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by David Fradin, 2020
 * <p>
 * Datasets used: /TODO
 */
public class DatasetGraphQLTests extends BaseTest {

    private ArrayNode metricAlgorithmsNode;
    private ArrayNode nodeItemsNode;
    private ArrayNode edgeItemsNode;
    private ArrayNode splItemsNode;
    private ArrayNode datasetMetricResultsNode;
    private final boolean printResponse = false;
    private HashSet<String> executedVariants;

    /**
     * Uploads a dataset via GraphQL API and verifies the imported data.
     *
     * @throws IOException TODO Test all import modules
     */
    private void datasetUploadAndImport(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        byte[] fileContent = readResourceToBytes(testCase.getPath());
        assertNotNull(fileContent);

        String fileName = testCase.getPath().replace("datasets/", "unit_test_");
        String graphQLMutationRequestStr = readResourceToString("graphql/dataset-create.graphql");

        // Build the multipart request
        LinkedMultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition.builder("form-data").name("1").filename(fileName).build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(fileContent, fileMap);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        LinkedMultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("operations", "{\"operationName\":\"createDataset\",\"variables\":{\"importModule\":\"raw-csv-import-module\",\"name\":\"" + fileName + "\",\"files\":[null]},\"query\":\"" + graphQLMutationRequestStr + "\"}\n");
        requestBody.add("map", "{\"1\":[\"variables.files.0\"]}");
        requestBody.add("1", fileEntity);
        HttpEntity<Object> request = new HttpEntity<>(requestBody, requestHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(graphqlMapping, HttpMethod.POST, request, String.class);
        GraphQLResponse responseCreate = new GraphQLResponse(responseEntity, objectMapper);

        assertThat(responseCreate.isOk()).isTrue();

        String datasetName = responseCreate.get("$.data.data.name");
        assertThat(datasetName).isEqualTo(fileName);

        testCase.setDatasetUUID(responseCreate.get("$.data.data.id"));
        assertThat(testCase.getDatasetUUID()).isNotNull();

        assertThat(responseCreate.get("$.data.data.attributesList[0].key")).isEqualTo("count_nodes");
        assertThat(responseCreate.get("$.data.data.attributesList[0].value")).isEqualTo(String.valueOf(testCase.getCountNodes()));
        assertThat(responseCreate.get("$.data.data.attributesList[1].key")).isEqualTo("count_edges");
        assertThat(responseCreate.get("$.data.data.attributesList[1].value")).isEqualTo(String.valueOf(testCase.getCountEdges()));
    }

    void datasetDelete(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        ObjectNode variables = objectMapper.createObjectNode();
        variables.put("id", testCase.getDatasetUUID());
        GraphQLResponse responseDelete = graphQLTestTemplate.perform("graphql/dataset-delete.graphql", variables);
        assertThat(responseDelete.isOk()).isTrue();
        assertThat(responseDelete.get("$.data.data.id")).isNotNull().isEqualTo(testCase.getDatasetUUID());
        if (printResponse)
            System.out.println(responseDelete.getRawResponse().getBody());
        testCase.setDatasetUUID(null);
    }

    private void cudaDevicesQueryAll() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.perform(
                "graphql/cuda-device-query-all.graphql",
                objectMapper.readValue("{\"filter\":{},\"page\":0,\"perPage\":10,\"sortField\":\"id\",\"sortOrder\":\"ASC\"}", ObjectNode.class)
        );
        if (printResponse)
            System.out.println(response.getRawResponse().getBody());
        assertThat(response.isOk()).isTrue();
        int cudaDevicesCount = response.get("$.data.total.count", int.class);
        assertThat(cudaDevicesCount).as("checking number of CUDA devices").isGreaterThanOrEqualTo(1);
    }

    /**
     * TODO test different filters to increase code coverage
     *
     * @throws IOException Exception is thrown when the backend cannot be reached or the GraphQL request is (syntactically) invalid.
     */
    private void datasetQueryAll(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        String variables = testCase.getDatasetUUID() != null ?
                "{\"filter\":{\"ids\": [\"" + testCase.getDatasetUUID() + "\"]},\"page\":0,\"perPage\":10,\"sortField\":\"name\",\"sortOrder\":\"ASC\"}" :
                "{\"filter\":null,\"page\":0,\"perPage\":10,\"sortField\":\"name\",\"sortOrder\":\"ASC\"}";
        GraphQLResponse response = graphQLTestTemplate.perform(
                "graphql/dataset-query-all.graphql",
                objectMapper.readValue(variables, ObjectNode.class)
        );
        if (printResponse)
            System.out.println(response.getRawResponse().getBody());
        assertThat(response.isOk()).isTrue();
        assertThat(response.get("$.data.total.count", int.class)).isGreaterThan(0);

        String datasetId2 = response.get("$.data.items[0].id");
        if (testCase.getDatasetUUID() == null)
            testCase.setDatasetUUID(datasetId2);
        else
            assertThat(datasetId2).isNotNull().isEqualTo(testCase.getDatasetUUID());
    }

    private void metricAlgorithmQueryAll() throws IOException {
        GraphQLResponse response = graphQLTestTemplate.perform(
                "graphql/metric-algorithm-query-all.graphql",
                objectMapper.readValue("{\"filter\": {}, \"sortField\": \"name\", \"sortOrder\": \"ASC\"}", ObjectNode.class)
        );
        if (printResponse)
            System.out.println(response.getRawResponse().getBody());
        assertThat(response.isOk()).isTrue();
        assertThat(response.get("$.data.total.count", int.class)).isGreaterThan(0);


        JsonNode node = response.readTree().get("data");
        assertThat(node.has("items")).isTrue();
        node = node.get("items");
        assertThat(node.isArray()).isTrue();
        metricAlgorithmsNode = (ArrayNode) node;
    }

    HashMap<String, HashMap<String, List<String>>> algTree;
    HashMap<String, List<MetricAlgorithmDep>> variantDependencies;

    static class MetricAlgorithmDep {
        public final String algorithmId, variantId;

        public MetricAlgorithmDep(String algorithmId, String variantId) {
            this.algorithmId = algorithmId;
            this.variantId = variantId;
        }
    }

    /**
     * Converts the Json response into a custom HashMap/List data structure which we afterwards use to launch all implementations in a logical order.
     *
     * @throws IOException
     */
    void collectDependencies() {
        assertNotNull(graphQLTestTemplate);

        algTree = new HashMap<>();
        variantDependencies = new HashMap<>();

        for (int i = 0; i < metricAlgorithmsNode.size(); i++) {
            JsonNode metricAlgorithmNode = metricAlgorithmsNode.get(i);
            String metricAlgorithm = metricAlgorithmNode.get("id").asText();
            System.out.println("\t" + metricAlgorithm);
            ArrayNode metricAlgorithmVariantsArrayNode = (ArrayNode) metricAlgorithmNode.get("metricAlgorithmVariants");

            HashMap<String, List<String>> algVariantTree = new HashMap<>();
            for (int j = 0; j < metricAlgorithmVariantsArrayNode.size(); j++) {
                JsonNode metricAlgorithmVariantNode = metricAlgorithmVariantsArrayNode.get(j);
                String metricAlgorithmVariant = metricAlgorithmVariantNode.get("id").asText();
                System.out.println("\t\t" + metricAlgorithmVariant);
                if (!metricAlgorithmVariantNode.get("dependencies").isNull()) {
                    ArrayNode dependenciesArrayNode = (ArrayNode) metricAlgorithmVariantNode.get("dependencies");
                    List<MetricAlgorithmDep> depList = new ArrayList<>();
                    if (dependenciesArrayNode != null) {
                        for (int k = 0; k < dependenciesArrayNode.size(); k++) {
                            JsonNode depNode = dependenciesArrayNode.get(k);
                            depList.add(new MetricAlgorithmDep(depNode.get("metricAlgorithm").get("id").asText(), depNode.get("id").asText()));
                        }
                    }
                    if (depList.size() > 0)
                        variantDependencies.put(metricAlgorithmVariant, depList);
                }
                ArrayNode implementationsArrayNode = (ArrayNode) metricAlgorithmVariantNode.get("implementations");
                List<String> implList = new ArrayList<>();
                for (int k = 0; k < implementationsArrayNode.size(); k++) {
                    JsonNode metricAlgorithmImplementationNode = implementationsArrayNode.get(k);
                    String metricAlgorithmImplementation = metricAlgorithmImplementationNode.get("id").asText();
                    boolean isAvailable = metricAlgorithmImplementationNode.get("available").asBoolean();
                    assertThat(isAvailable).as("Checking that the metric implementation " + metricAlgorithmImplementation + " is available.").isTrue();
                    System.out.println("\t\t\t" + metricAlgorithmImplementation);
                    implList.add(metricAlgorithmImplementation);
                }
                algVariantTree.put(metricAlgorithmVariant, implList);
            }
            algTree.put(metricAlgorithm, algVariantTree);
        }
    }

    /**
     * Calculates an execution plan and launches implementations one by one.
     * The execution plan first executes metric variants that do not have any dependencies, and afterwards the rest.
     *
     * @param parentAlgorithmId
     * @param parentVariantId
     * @throws IOException
     */
    void launchAllMetricVariantsRecursively(CoriaV3JUnitDatasetTestCase testCase, String parentAlgorithmId, String parentVariantId) throws Exception {
        if (parentVariantId == null) {
            for (var algorithmEntry : algTree.entrySet()) {
                for (var variantEntry : algorithmEntry.getValue().entrySet()) {
                    launchAllMetricVariantsRecursively(testCase, algorithmEntry.getKey(), variantEntry.getKey());
                }
            }
        } else if (!executedVariants.contains(parentVariantId)) {
            executedVariants.add(parentVariantId);
            if (variantDependencies.containsKey(parentVariantId)) {
                for (MetricAlgorithmDep depVariant : variantDependencies.get(parentVariantId)) {
                    launchAllMetricVariantsRecursively(testCase, depVariant.algorithmId, depVariant.variantId);
                }
            }
            for (var implementationId : algTree.get(parentAlgorithmId).get(parentVariantId)) {
                testCase.getImplementationExecutionTimes().put(implementationId, metricCreate(testCase, parentAlgorithmId, parentVariantId, implementationId));
            }
        }
    }

    private JsonNode queryMetricById(String metricId) throws IOException {
        GraphQLResponse response = graphQLTestTemplate.perform("graphql/metric-query-one.graphql",
                objectMapper.readValue("{\"id\":\"" + metricId + "\"}", ObjectNode.class));
        if (printResponse) {
            System.out.println("queryDatasetById() response:");
            System.out.println("\t" + response.getRawResponse().getBody());
        }
        assertThat(response.isOk()).isTrue();
        assertThat(response.get("$.data.data.id")).isEqualTo(metricId);

        return response.readTree().get("data").get("data");
    }

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

    private long metricCreate(CoriaV3JUnitDatasetTestCase testCase, String metricAlgorithm, String metricAlgorithmVariant, String metricAlgorithmImplementation) throws Exception {

        try {
            /*List<String> whitelist = List.of(
                    "shortest-path-lengths--default--",
                    "betweenness-centrality--default--",
                    "average-shortest-path-length--default--"
            );
            if (whitelist.stream().noneMatch(metricAlgorithmImplementation::startsWith)) {
                return -1;
            }*/

            System.out.println("[" + metricAlgorithmImplementation + "] start");
            //Instant start = Instant.now();
            //Special case: connectivity-risk-classification--default has two parameters
            String parameters = (metricAlgorithmVariant.equals("connectivity-risk-classification--default")) ? "[{\"key\":\"threshold-low\",\"value\":\"0.45\"},{\"key\":\"threshold-high\",\"value\":\"0.55\"}]" : "null";
            GraphQLResponse response = graphQLTestTemplate.perform("graphql/metric-create.graphql",
                    objectMapper.readValue("{\"datasetId\":\"" + testCase.getDatasetUUID() + "\",\"metricAlgorithm\":\"" + metricAlgorithm + "\",\"metricAlgorithmVariant\":\"" + metricAlgorithmVariant + "\",\"metricAlgorithmImplementation\":\"" + metricAlgorithmImplementation + "\",\"parameters\":" + parameters + "}", ObjectNode.class)
            );
            //long millis = Duration.between(start, Instant.now()).toMillis();
            if (printResponse) {
                System.out.println("[" + metricAlgorithmImplementation + "] response:");
                System.out.println("\t" + response.getRawResponse().getBody());
            }
            assertThat(response.isOk()).isTrue();
            String metricStatus = response.get("$.data.data.status");
            assertThat(metricStatus).as("metric %s", metricAlgorithmImplementation).isEqualTo("SCHEDULED");
            assertThat(response.get("$.data.data.message")).isNull();

            assertThat(response.get("$.data.data.id")).isNotNull();
            String metricId = response.get("$.data.data.id");
            // The metric is scheduled now.
            int timeoutCounter = 0;
            JsonNode metricData;
            do {
                Thread.sleep(500);
                timeoutCounter += 500;
                // Note: Using sleep in a loop is not ideal, however this line is purely for unit testing purposes.
                // It solves race conditions by giving the backend enough time to insert all the entries into DB.
                metricData = queryMetricById(metricId);
                metricStatus = metricData.get("status").asText();
            }
            while (metricStatus.equals("SCHEDULED") && timeoutCounter < 30000);
            assertThat(metricStatus).as("checking status for metric " + metricData.get("metricAlgorithmImplementation").get("id").asText()).isEqualTo("FINISHED");
            String metricType = metricData.get("metricAlgorithmImplementation").get("metricAlgorithmVariant").get("metricAlgorithm").get("type").asText();
            ArrayNode metricResults;
            switch (metricType) {
                case "Node":
                    metricResults = (ArrayNode) metricData.get("nodeMetricResults");
                    assertThat(metricResults.size()).as("checking metricResults.size() for " + metricData.get("metricAlgorithmImplementation").get("id").asText()).isEqualTo(testCase.getCountNodes());
                    break;
                case "Edge":
                    metricResults = (ArrayNode) metricData.get("edgeMetricResults");
                    assertThat(metricResults.size()).as("checking metricResults.size() for " + metricData.get("metricAlgorithmImplementation").get("id").asText()).isEqualTo(testCase.getCountEdges());
                    break;
                case "Dataset":
                    metricResults = (ArrayNode) metricData.get("datasetMetricResults");
                    assertThat(metricResults.size()).as("checking metricResults.size() for " + metricData.get("metricAlgorithmImplementation").get("id").asText()).isEqualTo(1);
                    break;
                case "ShortestPathLength":
                    metricResults = (ArrayNode) metricData.get("shortestPathLengths");
                    assertThat(metricResults.size()).as("checking metricResults.size() for " + metricData.get("metricAlgorithmImplementation").get("id").asText()).isEqualTo(testCase.getCountNodes() * (testCase.getCountNodes() - 1) / 2);
                    break;
                case "LayoutPosition":
                    metricResults = (ArrayNode) metricData.get("nodeLayoutPositions");
                    assertThat(metricResults.size()).as("checking metricResults.size() for " + metricData.get("metricAlgorithmImplementation").get("id").asText()).isEqualTo(testCase.getCountNodes());
                    break;
                default:
                    throw new Exception("Unknown metric type: " + metricType);
            }
            return Duration.between(dateFormat.parse(metricData.get("started").asText()).toInstant(), dateFormat.parse(metricData.get("finished").asText()).toInstant()).toMillis();
            //return millis;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void queryAllNodes(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        nodeItemsNode = _queryAllEntities(testCase, "graphql/node-query-all.graphql", "\"name\"", testCase.getCountNodes());
    }


    private void queryAllEdges(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        edgeItemsNode = _queryAllEntities(testCase, "graphql/edge-query-all.graphql", "\"name\"", testCase.getCountEdges());
    }

    private void queryAllShortestPathLengths(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        splItemsNode = _queryAllEntities(testCase, "graphql/shortest-path-lengths-query-all.graphql", "\"nodeSourceName\"",
                // Number of unique pairs of nodes = |N| * |N-1| / 2. We count A->B and B->A as one.
                (testCase.getCountNodes() * (testCase.getCountNodes() - 1) / 2) * testCase.countExecutedMetrics("shortest-path-lengths--default--"));
    }

    private ArrayNode _queryAllEntities(CoriaV3JUnitDatasetTestCase testCase, String graphQLQueryFile, String sortField, int expectedCount) throws IOException {
        GraphQLResponse response = graphQLTestTemplate.perform(graphQLQueryFile,
                objectMapper.readValue("{\"filter\":{\"datasetId\":\"" + testCase.getDatasetUUID() + "\"},\"page\":0,\"perPage\":200,\"sortField\":" + sortField + ",\"sortOrder\":\"ASC\"}", ObjectNode.class));
        if (printResponse) {
            System.out.println("queryAll " + graphQLQueryFile + " response:");
            System.out.println("\t" + response.getRawResponse().getBody());
        }
        assertThat(response.isOk()).isTrue();
        assertThat(response.get("$.data.total.count", int.class)).as("Checking number of rows").isEqualTo(expectedCount);

        JsonNode node = response.readTree().get("data");
        assertThat(node.has("items")).isTrue();
        node = node.get("items");
        assertThat(node.isArray()).isTrue();
        return (ArrayNode) node;
    }

    private void queryDatasetById(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        GraphQLResponse response = graphQLTestTemplate.perform("graphql/dataset-query-one.graphql",
                objectMapper.readValue("{\"id\":\"" + testCase.getDatasetUUID() + "\"}", ObjectNode.class));
        if (printResponse) {
            System.out.println("queryDatasetById() response:");
            System.out.println("\t" + response.getRawResponse().getBody());
        }
        assertThat(response.isOk()).isTrue();
        assertThat(response.get("$.data.data.id")).isEqualTo(testCase.getDatasetUUID());

        JsonNode node = response.readTree().get("data").get("data");
        assertThat(node.has("metricResults")).isTrue();
        node = node.get("metricResults");
        assertThat(node.isArray()).isTrue();
        datasetMetricResultsNode = (ArrayNode) node;
    }

    private void verifyNodeMetricResults(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        HashMap<String, HashMap<String, Double>> expectedResultsMap = new HashMap<>();
        String expectedResultsStr = readResourceToString(testCase.getPath().replace(".txt", ".node-metric-results.csv"));
        assertThat(expectedResultsStr).isNotNull();
        String[] expectedResultsLines = expectedResultsStr.split("\n");
        String[] headerRow = expectedResultsLines[0].split("[,;]");
        for (int j = 1; j < expectedResultsLines.length; j++) {
            String[] lineValues = expectedResultsLines[j].split("[,;]");
            HashMap<String, Double> nmrRow = new HashMap<>();
            for (int k = 1; k < lineValues.length; k++) {
                nmrRow.put(headerRow[k], Double.parseDouble(lineValues[k]));
            }
            expectedResultsMap.put(lineValues[0], nmrRow);
        }

        for (int i = 0; i < nodeItemsNode.size(); i++) {
            HashMap<String, HashMap<String, Double>> nodeMetricResultsMap = new HashMap<>();
            JsonNode currentNode = nodeItemsNode.get(i);
            ArrayNode metricResults = (ArrayNode) currentNode.get("metricResults");
            for (int j = 0; j < metricResults.size(); j++) {
                JsonNode mr = metricResults.get(j);
                double resultValue = mr.get("value").asDouble();
                JsonNode mai = mr.get("metric").get("metricAlgorithmImplementation");
                String maiId = mai.get("id").asText();
                String mavId = mai.get("metricAlgorithmVariant").get("id").asText();
                if (!nodeMetricResultsMap.containsKey(mavId)) {
                    nodeMetricResultsMap.put(mavId, new HashMap<>());
                }
                nodeMetricResultsMap.get(mavId).put(maiId, resultValue);
            }

            String nodeName = currentNode.get("name").asText();
            for (var mav : nodeMetricResultsMap.entrySet()) {
                assertThat(expectedResultsMap).as("Checking that the (expected) node-metric-results.csv contains values for metric " + mav.getKey()).containsKey(mav.getKey());
                var currentNodeNMR = expectedResultsMap.get(mav.getKey());
                assertThat(currentNodeNMR.containsKey(nodeName)).isTrue();
                double prevValue = currentNodeNMR.get(nodeName);
                for (var mai : mav.getValue().entrySet()) {
                    assertThat(mai.getValue()).as("dataset: %s, check node metric: %s", testCase.getPath(), mai.getKey()).isCloseTo(prevValue, Offset.offset(0.0001));//Compare two floating point numbers with an epsilon tolerance.
                }
            }
        }
    }

    private void verifyEdgeMetricResults(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        HashMap<String, HashMap<String, Double>> expectedResultsMap = new HashMap<>();
        String[] expectedResultsLines = readResourceToString(testCase.getPath().replace(".txt", ".edge-metric-results.csv")).split("\n");
        String[] headerRow = expectedResultsLines[0].split("[,;]");
        for (int j = 1; j < expectedResultsLines.length; j++) {
            String[] lineValues = expectedResultsLines[j].split("[,;]");
            HashMap<String, Double> nmrRow = new HashMap<>();
            for (int k = 1; k < lineValues.length; k++) {
                nmrRow.put(headerRow[k], Double.parseDouble(lineValues[k]));
            }
            expectedResultsMap.put(lineValues[0], nmrRow);
        }

        for (int i = 0; i < edgeItemsNode.size(); i++) {
            HashMap<String, HashMap<String, Double>> edgeMetricResultsMap = new HashMap<>();
            JsonNode currentEdge = edgeItemsNode.get(i);
            ArrayNode metricResults = (ArrayNode) currentEdge.get("metricResults");
            for (int j = 0; j < metricResults.size(); j++) {
                JsonNode mr = metricResults.get(j);
                double resultValue = mr.get("value").asDouble();
                JsonNode mai = mr.get("metric").get("metricAlgorithmImplementation");
                String maiId = mai.get("id").asText();
                String mavId = mai.get("metricAlgorithmVariant").get("id").asText();
                if (!edgeMetricResultsMap.containsKey(mavId)) {
                    edgeMetricResultsMap.put(mavId, new HashMap<>());
                }
                edgeMetricResultsMap.get(mavId).put(maiId, resultValue);
            }

            String edgeName = currentEdge.get("name").asText();
            for (var mav : edgeMetricResultsMap.entrySet()) {
                assertThat(expectedResultsMap.containsKey(mav.getKey())).isTrue();
                var currentEdgeNMR = expectedResultsMap.get(mav.getKey());
                assertThat(currentEdgeNMR.containsKey(edgeName)).isTrue();
                double prevValue = currentEdgeNMR.get(edgeName);
                for (var mai : mav.getValue().entrySet()) {
                    assertThat(mai.getValue()).as("check edge metric: %s", mai.getKey()).isCloseTo(prevValue, Offset.offset(0.0001));//Compare two floating point numbers with an epsilon tolerance.
                }
            }
        }
    }

    private void verifyShortestPathLengthsMetricResults(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        HashMap<String, Double> expectedResultsMap = new HashMap<>(); //nodeSource -> nodeTarget -> distance
        String[] expectedResultsLines = readResourceToString(testCase.getPath().replace(".txt", ".shortest-path-lengths-metric-results.csv")).split("\n");
        for (int j = 1; j < expectedResultsLines.length; j++) {
            String[] lineValues = expectedResultsLines[j].split("[,;]");
            expectedResultsMap.put(lineValues[0] + "$" + lineValues[1], Double.parseDouble(lineValues[2]));
        }

        for (int i = 0; i < splItemsNode.size(); i++) {
            JsonNode currentSpl = splItemsNode.get(i);
            String key = currentSpl.get("nodeSourceName").asText() + "$" + currentSpl.get("nodeTargetName").asText();
            String implementationId = currentSpl.get("metric").get("metricAlgorithmImplementation").get("id").asText();
            double distance = currentSpl.get("distance").asDouble();
            assertThat(expectedResultsMap.containsKey(key)).isTrue();
            assertThat(expectedResultsMap.get(key)).as("check SPL: %s", implementationId).isCloseTo(distance, Offset.offset(0.0001));//Compare two floating point numbers with an epsilon tolerance.
        }
    }

    private void verifyDatasetMetricResults(CoriaV3JUnitDatasetTestCase testCase) throws IOException {
        HashMap<String, HashMap<String, Double>> datasetMetricResultsMap = new HashMap<>();

        for (int j = 0; j < datasetMetricResultsNode.size(); j++) {
            JsonNode mr = datasetMetricResultsNode.get(j);
            double resultValue = mr.get("value").asDouble();
            JsonNode mai = mr.get("metric").get("metricAlgorithmImplementation");
            String maiId = mai.get("id").asText();
            String mavId = mai.get("metricAlgorithmVariant").get("id").asText();
            if (!datasetMetricResultsMap.containsKey(mavId)) {
                datasetMetricResultsMap.put(mavId, new HashMap<>());
            }
            datasetMetricResultsMap.get(mavId).put(maiId, resultValue);
        }

        HashMap<String, Double> expectedResultsMap = new HashMap<>();
        String[] expectedResultsLines = readResourceToString(testCase.getPath().replace(".txt", ".dataset-metric-results.csv")).split("\n");
        for (int j = 1; j < expectedResultsLines.length; j++) {
            String[] lineValues = expectedResultsLines[j].split("[,;]");
            expectedResultsMap.put(lineValues[0], Double.parseDouble(lineValues[1]));
        }

        for (var mav : datasetMetricResultsMap.entrySet()) {
            assertThat(expectedResultsMap.containsKey(mav.getKey())).isTrue();
            double prevValue = expectedResultsMap.get(mav.getKey());
            for (var mai : mav.getValue().entrySet()) {
                assertThat(mai.getValue()).as("check dataset metric: %s", mai.getKey()).isCloseTo(prevValue, Offset.offset(0.0001));//Compare two floating point numbers with an epsilon tolerance.
            }
        }
    }

    /**
     * Before all the tests, setUp() is called to import the dataset.
     *
     * @throws Exception
     */
    @Before
    public void setUp() {
        System.out.println("[Dataset Test] setUp()");
    }

    final boolean create = true;
    private final List<CoriaV3JUnitDatasetTestCase> datasetsToBeTested = List.of(
            new CoriaV3JUnitDatasetTestCase("datasets/trivial-graph-3nodes-2edges.txt", 3, 2)
            , new CoriaV3JUnitDatasetTestCase("datasets/mclaughlin2014-9nodes-14edges.txt", 9, 14)
    );

    @Test
    public void mainTest() throws Exception {
        cudaDevicesQueryAll();

        metricAlgorithmQueryAll();

        collectDependencies();

        for (CoriaV3JUnitDatasetTestCase testCase : datasetsToBeTested) {
            //Create the dataset
            if (create)
                datasetUploadAndImport(testCase);

            datasetQueryAll(testCase);

            if (create) {
                executedVariants = new HashSet<>();
                launchAllMetricVariantsRecursively(testCase, null, null);
            }

            queryAllNodes(testCase);

            queryAllEdges(testCase);

            queryAllShortestPathLengths(testCase);

            queryDatasetById(testCase);

            verifyNodeMetricResults(testCase);

            verifyEdgeMetricResults(testCase);

            verifyShortestPathLengthsMetricResults(testCase);

            verifyDatasetMetricResults(testCase);
        }

        //TODO /2 run every metric 10 times and average the times.
    }

    /**
     * After all the tests, tearDown() is called to delete the dataset.
     */
    @After
    public void tearDown() throws IOException {
        System.out.println("[Dataset Test] tearDown()");
        for (CoriaV3JUnitDatasetTestCase testCase : datasetsToBeTested) {
            if (testCase.getImplementationExecutionTimes() != null && testCase.getImplementationExecutionTimes().size() > 0) {
                System.out.println("\n\n\t*** Execution Times ***");
                testCase.getImplementationExecutionTimesSortedByMetricAlgorithmAndTime().forEach((mavEntry) -> {
                    System.out.println(String.format("%5d", mavEntry.getValue()) + "ms\t\t" + mavEntry.getKey());// This call of String.format() adds spaces to fill up to 5 chars
                });
            }

            if (testCase.getDatasetUUID() != null) {
                datasetDelete(testCase);
            }
        }
    }


    static class CoriaV3JUnitDatasetTestCase {
        private final String path;
        private final int countNodes, countEdges;
        private final HashMap<String, Long> implementationExecutionTimes;
        private String datasetUUID;

        public CoriaV3JUnitDatasetTestCase(String path, int countNodes, int countEdges) {
            this.path = path;
            this.countNodes = countNodes;
            this.countEdges = countEdges;
            this.implementationExecutionTimes = new HashMap<>();
        }

        public String getPath() {
            return path;
        }

        public int getCountNodes() {
            return countNodes;
        }

        public int getCountEdges() {
            return countEdges;
        }

        public String getDatasetUUID() {
            return datasetUUID;
        }

        public void setDatasetUUID(String datasetUUID) {
            this.datasetUUID = datasetUUID;
        }

        public HashMap<String, Long> getImplementationExecutionTimes() {
            return implementationExecutionTimes;
        }

        public List<Map.Entry<String, Long>> getImplementationExecutionTimesSortedByMetricAlgorithmAndTime() {
            List<Map.Entry<String, Long>> list = new LinkedList<>(this.implementationExecutionTimes.entrySet());

            // Sorting the list based on values
            list.sort((o1, o2) -> {
                String[] parts1 = o1.getKey().split("--");
                String[] parts2 = o2.getKey().split("--");
                return parts1[0].compareTo(parts2[0]) != 0 ? parts1[0].compareTo(parts2[0]) : o1.getValue().compareTo(o2.getValue());
            });
            return list;
        }

        public int countExecutedMetrics(String prefix) {
            return (int) this.implementationExecutionTimes.keySet().stream().filter(s -> s.startsWith(prefix)).count();
        }
    }
}
