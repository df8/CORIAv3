package com.coria.v3.metrics.python;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.dbmodel.MetricEntity;
import com.coria.v3.metrics.MetricAlgorithmType;
import com.coria.v3.metrics.MetricAlgorithmVariant;
import com.coria.v3.metrics.MetricMultiAlgorithmImplementation;
import com.coria.v3.repository.RepositoryManager;
import com.coria.v3.utility.Slugify;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.*;

/**
 * Created by David Fradin, 2020
 */
public class PythonMetricMultiAlgorithmImplementation implements MetricMultiAlgorithmImplementation {
    protected final static Logger logger = LoggerFactory.getLogger(PythonMetricMultiAlgorithmImplementation.class);
    private final String id;

    public PythonMetricMultiAlgorithmImplementation(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    /*@Override
    public List<PythonMetricAlgorithmImplementation> getMetricAlgorithmImplementations() {
        return metricAlgorithmImplementations;
    }*/

    @Override
    public void performComputations(RepositoryManager repositoryManager, DatasetEntity datasetEntity, List<MetricEntity> metricEntityList, Map<String, UUID> dependencyMetricIds, List<HashMap<String, String>> parameters) throws Exception {
        String fullPathToScript = PythonMetricAlgorithmImplementation.getScriptPath();

        /*
         * Read from DB and write into a file on the file system
         */
        logger.debug("creating import file in filesystem for the executable to be found...");
        String requestFilePathTemplate = Paths.get(AppContext.getInstance().getWorkingDirectory(), Slugify.toSlug(datasetEntity.getName()) + "_import___%%variantId%%.csv").toAbsolutePath().toString();
        String responseFilePathTemplate = Paths.get(AppContext.getInstance().getWorkingDirectory(), Slugify.toSlug(datasetEntity.getName()) + "_export___%%type%%.csv").toAbsolutePath().toString();

        List<String> requestFilePaths = new ArrayList<>();
        HashMap<PythonMetricResponseFileType, String> responseFilePaths = new HashMap<>();

        StringBuilder execCmdLine = new StringBuilder(AppContext.getInstance().getCondaPath() +
                " run -n " + AppContext.getInstance().getCondaEnvironmentName() +
                " python " + fullPathToScript);

        for (var mai : metricEntityList) {
            execCmdLine.append(" -m ").append(mai.getMetricAlgorithmImplementationId());
            PythonMetricResponseFileType fileType = PythonMetricResponseFileType.valueOf(mai.getMetricAlgorithmImplementation().getType().toString());
            if (!responseFilePaths.containsKey(fileType) && (fileType != PythonMetricResponseFileType.ShortestPathLength || datasetEntity.getNodes().size() <= 500)) {
                // Storing all shortest path distances requires lots of GPU memory { O( 3 * |N| * (|N|-1) / 2) } therefore it will be skipped for large graphs
                responseFilePaths.put(fileType, responseFilePathTemplate.replace("%%type%%", Slugify.toSlug(fileType.toString())));
            }
            PythonMetricAlgorithmImplementation.processParameters(mai.getMetricAlgorithmImplementation().getMetricAlgorithmVariant(), parameters, execCmdLine);
        }
        responseFilePaths.put(PythonMetricResponseFileType.ExecutionTimestamps, responseFilePathTemplate.replace("%%type%%", Slugify.toSlug(PythonMetricResponseFileType.ExecutionTimestamps.toString())));

        for (String path : responseFilePaths.values()) {
            execCmdLine.append(" -o ").append(path);
        }

        //logger.debug("Using paths:\n\tOutput File:{}", responseFilePath);

        FileSystemUtility fileSystemUtility = new FileSystemUtility();
        String requestFilePathTemp;

        boolean prepareForNodeMetricResults = metricEntityList.stream().anyMatch(m -> m.getMetricAlgorithmImplementation().getType() == MetricAlgorithmType.Node || m.getMetricAlgorithmImplementation().getType() == MetricAlgorithmType.ShortestPathLength || m.getMetricAlgorithmImplementation().getType() == MetricAlgorithmType.LayoutPosition);
        boolean prepareForEdgeMetricResults = metricEntityList.stream().anyMatch(m -> m.getMetricAlgorithmImplementation().getType() == MetricAlgorithmType.Edge);
        for (var mai : metricEntityList) {
            if (((PythonMetricAlgorithmImplementation) mai.getMetricAlgorithmImplementation()).isEdgeListInputFileRequired()) {
                requestFilePathTemp = requestFilePathTemplate.replace("%%variantId%%", "edges");
                requestFilePaths.add(requestFilePathTemp);
                logger.debug("\tInput File: {}", requestFilePathTemp);
                fileSystemUtility.copyAllEdgesToTextFile(datasetEntity, requestFilePathTemp, "\t", prepareForNodeMetricResults, prepareForEdgeMetricResults);
                execCmdLine.append(" -i ").append(requestFilePathTemp);
                break;
            }
        }

        List<String> nmrVariantKeys = new ArrayList<>();
        boolean shortestPathLengthRequired = false;
        for (var mai : metricEntityList) {
            if (mai.getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getDependencies() != null) {
                for (MetricAlgorithmVariant depVariant : mai.getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getDependencies()) {
                    // If a particular metric variant is part of this multi request, than skip it from dependencies.
                    if (metricEntityList.stream().noneMatch(m -> m.getMetricAlgorithmImplementation().getMetricAlgorithmVariant().getId().equals(depVariant.getId()))) {
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
            }
        }
        requestFilePathTemp = requestFilePathTemplate.replace("%%variantId%%", "nmr-dependencies-for-python-metric-multi-alg");
        requestFilePaths.add(requestFilePathTemp);
        PythonMetricAlgorithmImplementation.prepareNodeMetricResultsTextFile(repositoryManager,
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


        PythonMetricAlgorithmImplementation.executeMetricAndReadResults(repositoryManager, execCmdLine.toString(), requestFilePaths, responseFilePaths, fileSystemUtility, metricEntityList);
    }
}
