package com.coria.v3.metrics.python;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.dbmodel.NodeMetricResultEntity;
import com.coria.v3.interop.FSTools;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import com.coria.v3.metrics.MetricAlgorithmType;
import com.coria.v3.metrics.MetricAlgorithmVariant;
import com.coria.v3.metrics.MetricAlgorithmVariantParameter;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.Slugify;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

import static com.coria.v3.metrics.MetricAlgorithmType.ShortestPathLength;

/**
 * Created by David Fradin, 2020
 * Based on work by Sebastian Gross, 2017 in coria-metric-python-nx/src/main/java/com/bigbasti/coria/metric/nx/NXBetweennessCentrality.java
 */
public class PythonMetricAlgorithmImplementation extends MetricAlgorithmImplementation {
    protected final static Logger logger = LoggerFactory.getLogger(PythonMetricAlgorithmImplementation.class);

    protected boolean edgeListInputFileRequired;

    @JsonCreator
    public PythonMetricAlgorithmImplementation(
            @JsonProperty("provider") String provider,
            @JsonProperty("algorithm") String metricAlgorithmName,
            @JsonProperty("algorithm-variant") String metricAlgorithmVariantName,
            @JsonProperty(value = "edge-list-input-file-required", defaultValue = "false") Boolean edgeListInputFileRequired
    ) throws Exception {
        //TODO /1 write description about cuGraph and NetworkX
        super(provider.equals("RAPIDS cuGraph") ? "Python3 / C++ / CUDA" : "Python3",
                provider,
                provider.equals("RAPIDS cuGraph") ?
                        "Description about RAPIDS cuGraph" :
                        "Description about NetworkX",
                provider.equals("RAPIDS cuGraph") ? 40 : 20,
                AppContext.getInstance().getMetricAlgorithmByName(metricAlgorithmName).getMetricAlgorithmVariantByName(metricAlgorithmVariantName),
                !provider.equals("RAPIDS cuGraph") || (AppContext.getInstance().getCudaDevices() != null && AppContext.getInstance().getCudaDevices().size() > 0),
                provider.equals("RAPIDS cuGraph") && (AppContext.getInstance().getCudaDevices() == null || AppContext.getInstance().getCudaDevices().size() == 0) ? "There are no Nvidia CUDA devices currently available on this server." : null);
        this.edgeListInputFileRequired = edgeListInputFileRequired != null && edgeListInputFileRequired;
    }

    protected static String getScriptPath() throws Exception {
        try {
            URL resource = PythonMetricAlgorithmImplementation.class.getResource("/coria_py/__main__.py");
            return Paths.get(resource.toURI()).toFile().getAbsolutePath();
        } catch (Exception e) {
            throw new Exception("Could not find python script. " + e.getMessage());
        }
    }

    protected static void prepareNodeMetricResultsTextFile(RepositoryManager repositoryManager, DatasetEntity datasetEntity, Map<String, UUID> dependencyMetricIds, String requestFilePath, List<String> nmrVariantKeys, boolean prepareForNodeMetricResults, FileSystemUtility fileSystemUtility, StringBuilder execCmdLine) throws Exception {
        if (nmrVariantKeys.size() > 0) {
            //map structure: node -> metric -> metric result value
            HashMap<NodeEntity, HashMap<String, Double>> nodeMetricResultsMap = new HashMap<>();
            for (var key : nmrVariantKeys) {
                List<NodeMetricResultEntity> nmrList = repositoryManager.getNodeMetricResultRepository().findAllByMetric_Id(dependencyMetricIds.get(key));
                if (nmrList.size() != datasetEntity.getNodes().size()) {
                    throw new Exception("Failed to compute metric ");
                }
                for (NodeMetricResultEntity nmr : nmrList) {
                    if (!nodeMetricResultsMap.containsKey(nmr.getNode())) {
                        nodeMetricResultsMap.put(nmr.getNode(), new HashMap<>());
                    }
                    nodeMetricResultsMap.get(nmr.getNode()).put(nmr.getMetric().getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getId(), nmr.getValue());
                }
            }
            fileSystemUtility.copyAllNodeMetricResultsToTextFile(datasetEntity, nodeMetricResultsMap, nmrVariantKeys, requestFilePath, "\t", prepareForNodeMetricResults);
            execCmdLine.append(" -i ").append(requestFilePath);
        }
    }

    final static boolean doRemove = true;

    protected static void executeMetricAndReadResults(RepositoryManager repositoryManager, String execCmdLine, List<String> requestFilePaths, Map<PythonMetricResponseFileType, String> responseFilePaths, FileSystemUtility fileSystemUtility, List<MetricEntity> metricEntityList) throws Exception {
        System.out.println("execCmdLine\t\t\t" + execCmdLine);
        FSTools fsTools = PythonMetricAlgorithmImplementationFactory.getFSTools();
        FSTools.ProcessExecutionResult result = fsTools.startSyncSystemProcess(execCmdLine);
        if (result.getExitCode() != 0) {
            String message = "Unknown error.";
            for (String msg : result.getErrOutLines()) {
                if (msg.contains("No NVIDIA GPU detected")) {
                    throw new Exception("No NVIDIA GPU detected");
                } else {
                    msg = msg.trim();
                    if (msg.length() > 0) { //Keep the last non-empty message line
                        message = msg;
                    }
                }
            }
            throw new Exception(message);
        }
        for (var responseFilePath : responseFilePaths.values()) {
            File responseFile = new File(responseFilePath);
            if (!responseFile.exists()) {
                logger.error("the expected calculation results file ({}) was not found, canceling metric execution", responseFilePaths);
                throw new RuntimeException("the expected calculation results file was not found. (" + String.join(System.lineSeparator(), result.getErrOutLines()) + ")");
            }
            logger.debug("Result file path: {}", responseFilePath);
        }

        for (var responseFilePathEntry : responseFilePaths.entrySet()) {
            fileSystemUtility.copyAllMetricResultsFromTextFile(responseFilePathEntry.getValue(), metricEntityList, responseFilePathEntry.getKey(), repositoryManager);
        }
        // Remove request files from file system
        if (doRemove) {
            requestFilePaths.forEach((requestFilePath) -> {
                try {
                    fileSystemUtility.removeFile(requestFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Remove response files from file system
            responseFilePaths.forEach((fileType, responseFilePath) -> {
                try {
                    fileSystemUtility.removeFile(responseFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    /**
     * Creates a string similar to "-p connectivity-risk-classification--default:threshold-low:0.45" and appends it to execCmdLine.
     *
     * @param metricAlgorithmVariant
     * @param parameters
     * @param execCmdLine
     * @throws Exception
     */
    protected static void processParameters(MetricAlgorithmVariant metricAlgorithmVariant, List<HashMap<String, String>> parameters, StringBuilder execCmdLine) throws Exception {
        List<MetricAlgorithmVariantParameter> parameterMeta = metricAlgorithmVariant.getParameters();
        if (parameterMeta != null && parameterMeta.size() > 0) {
            for (var p : parameterMeta) {
                if (parameters != null) {
                    var currentParameterValue = parameters.stream().filter(map -> map.get("key").equals(p.getId())).findFirst();
                    if (currentParameterValue.isPresent()) {
                        // TODO /3 implement other parameter types: - INT, STRING
                        // TODO /3 Vulnerability for malicious code execution: If `value` is of type STRING, it may contain unwanted shell code. For now we only accept valid floats, as there are no metrics requiring strings.
                        if (p.getType() == MetricAlgorithmVariantParameter.MetricAlgorithmVariantParameterType.FLOAT) {
                            String value = currentParameterValue.get().get("value");
                            if (value.matches("[-+]?[0-9]*\\.?[0-9]+")) {
                                execCmdLine
                                        .append(" -p ")
                                        .append(metricAlgorithmVariant.getId())
                                        .append(":")
                                        .append(currentParameterValue.get().get("key"))
                                        .append(":")
                                        .append(value);
                            } else {
                                throw new Exception("Parameter \"" + p.getId() + "\" for metric variant " + metricAlgorithmVariant.getId() + " is not a valid decimal number.");
                            }
                        } else {
                            throw new Exception("Parameter type " + p.getType() + " ot supported yet.");
                        }
                    } else if (p.isRequired()) {
                        throw new Exception("Missing required parameter \"" + p.getId() + "\" for metric variant " + metricAlgorithmVariant.getId());
                    }
                } else if (p.isRequired()) {
                    throw new Exception("Missing required parameter \"" + p.getId() + "\" for metric variant " + metricAlgorithmVariant.getId());
                }
            }
        }
    }

    @Override
    public void performComputation(RepositoryManager repositoryManager, DatasetEntity datasetEntity, MetricEntity metricEntity, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        String fullPathToScript = getScriptPath();
        /*
         * Read from DB and write into a file on the file system
         */
        logger.debug("creating import file in filesystem for the executable to be found...");
        String requestFilePathTemplate = Paths.get(AppContext.getInstance().getWorkingDirectory(), Slugify.toSlug(datasetEntity.getName()) + "_import___%%variantId%%.csv").toAbsolutePath().toString();
        String responseFilePath = Paths.get(AppContext.getInstance().getWorkingDirectory(), Slugify.toSlug(datasetEntity.getName()) + "_export___" + this.getType() + (this.getType() == ShortestPathLength ? ".json" : ".csv")).toAbsolutePath().toString();
        List<String> requestFilePaths = new ArrayList<>();
        StringBuilder execCmdLine = new StringBuilder(AppContext.getInstance().getCondaPath() + " run -n " +
                AppContext.getInstance().getCondaEnvironmentName() +
                " python " + fullPathToScript +
                " -m " + getId() + //ModuleId
                " -o " + responseFilePath);

        processParameters(this.getMetricAlgorithmVariant(), parameters, execCmdLine);

        logger.debug("Using paths:\n\tOutput File:{}", responseFilePath);

        FileSystemUtility fileSystemUtility = new FileSystemUtility();
        String requestFilePathTemp;
        boolean prepareForNodeMetricResults = this.getType() == MetricAlgorithmType.Node || this.getType() == ShortestPathLength || this.getType() == MetricAlgorithmType.LayoutPosition;
        boolean prepareForEdgeMetricResults = this.getType() == MetricAlgorithmType.Edge;
        if (this.edgeListInputFileRequired) {
            requestFilePathTemp = requestFilePathTemplate.replace("%%variantId%%", "edges");
            requestFilePaths.add(requestFilePathTemp);
            logger.debug("\tInput File: {}", requestFilePathTemp);
            fileSystemUtility.copyAllEdgesToTextFile(datasetEntity, requestFilePathTemp, "\t", prepareForNodeMetricResults, prepareForEdgeMetricResults);
            execCmdLine.append(" -i ").append(requestFilePathTemp);
        }
        List<String> nmrVariantKeys = new ArrayList<>();
        boolean shortestPathLengthRequired = false;
        if (this.getMetricAlgorithmVariant().getDependencies() != null) {
            for (MetricAlgorithmVariant depVariant : this.getMetricAlgorithmVariant().getDependencies()) {
                switch (depVariant.getMetricAlgorithm().getType()) {
                    case Node:
                        nmrVariantKeys.add(depVariant.getId());
                        break;
                    case ShortestPathLength:
                        shortestPathLengthRequired = true;
                        break;
                    default:
                        throw new Exception("Unsupported operation.");
                }
            }
        }
        requestFilePathTemp = requestFilePathTemplate.replace("%%variantId%%", "nmr-dependencies-for-" + this.getMetricAlgorithmVariant().getId());
        requestFilePaths.add(requestFilePathTemp);
        prepareNodeMetricResultsTextFile(repositoryManager,
                datasetEntity,
                dependencyMetricIds,
                requestFilePathTemp,
                nmrVariantKeys,
                prepareForNodeMetricResults,
                fileSystemUtility,
                execCmdLine
        );

        if (shortestPathLengthRequired) {
            requestFilePathTemp = requestFilePathTemplate.replace("%%variantId%%", "shortest-path-lengths");
            requestFilePaths.add(requestFilePathTemp);
            logger.debug("\tInput File: {}", requestFilePathTemp);
            fileSystemUtility.copyAllShortestPathLengthsToTextFile(repositoryManager.getShortestPathLengthRepository().findAllByMetric_Id(dependencyMetricIds.get("shortest-path-lengths--default")), requestFilePathTemp, "\t", prepareForNodeMetricResults);
            execCmdLine.append(" -i ").append(requestFilePathTemp);
        }

        executeMetricAndReadResults(repositoryManager, execCmdLine.toString(), requestFilePaths, Map.of(PythonMetricResponseFileType.valueOf(this.getType().toString()), responseFilePath), fileSystemUtility, List.of(metricEntity));
    }

    public boolean isEdgeListInputFileRequired() {
        return edgeListInputFileRequired;
    }

    public void setEdgeListInputFileRequired(boolean edgeListInputFileRequired) {
        this.edgeListInputFileRequired = edgeListInputFileRequired;
    }
}
