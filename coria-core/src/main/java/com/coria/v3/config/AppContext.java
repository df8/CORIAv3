package com.coria.v3.config;

import com.coria.v3.cuda.CudaDeviceInfo;
import com.coria.v3.cuda.CudaDeviceInfoFactory;
import com.coria.v3.export.ExportModule;
import com.coria.v3.metrics.*;
import com.coria.v3.parser.ASLocationEntityImportModule;
import com.coria.v3.parser.ASOrganizationEntityImportModule;
import com.coria.v3.parser.DatasetEntityImportModule;
import com.coria.v3.parser.ImportModuleFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sebastian Gross, 2017 (as coria-api/src/main/java/com/bigbasti/coria/config/AppContext.java)
 * Modified by David Fradin, 2020:
 * Moved to core package and added support for
 * - Conda (Python package manager)
 * - management of the entire MetricAlgorithm hierarchy
 * - ASOrganizationEntityImportModules
 * - ASLocationEntityImportModules
 * - CUDACapableDevices
 * - Fast lookup using HashMaps
 * Removed: databaseProvider
 */
@Service
public class AppContext {
    private final Logger logger = LoggerFactory.getLogger(AppContext.class);

    public static AppContext getInstance() {
        return instance;
    }

    private static AppContext instance;

    private AppContext() {
        instance = this;
        logger.debug("AppContext()");

        this.metricAlgorithms = new HashMap<>();
        this.datasetEntityImportModules = new HashMap<>();
        this.asOrganizationEntityImportModules = new HashMap<>();
        this.asLocationEntityImportModules = new HashMap<>();
        this.cudaCapableDevices = new HashMap<>();
        this.metricAlgorithmImplementations = new HashMap<>();
        this.metricMultiAlgorithmImplementations = new HashMap<>();
        this.exportModules = new HashMap<>();

        MetricAlgorithmFactory.getInstance().getList().forEach(module -> metricAlgorithms.put(module.getName(), module));
    }

    private String workingDirectory;
    private String condaEnvironmentName;
    private String condaPath;
    private String cudaDeviceQueryPath;
    private final HashMap<String, DatasetEntityImportModule> datasetEntityImportModules;
    private final HashMap<String, ASLocationEntityImportModule> asLocationEntityImportModules;
    private final HashMap<String, ASOrganizationEntityImportModule> asOrganizationEntityImportModules;
    private final HashMap<String, ExportModule> exportModules;
    private final HashMap<String, MetricAlgorithm> metricAlgorithms;
    private final HashMap<String, MetricAlgorithmImplementation> metricAlgorithmImplementations;
    private final HashMap<String, MetricMultiAlgorithmImplementation> metricMultiAlgorithmImplementations;
    private final HashMap<String, CudaDeviceInfo> cudaCapableDevices;


    public Map<String, DatasetEntityImportModule> getDatasetEntityImportModules() {
        return datasetEntityImportModules;
    }

    public Map<String, ASLocationEntityImportModule> getASLocationEntityImportModules() {
        return asLocationEntityImportModules;
    }

    public Map<String, ASOrganizationEntityImportModule> getASOrganizationEntityImportModules() {
        return asOrganizationEntityImportModules;
    }

    public HashMap<String, MetricAlgorithm> getMetricAlgorithms() {
        return metricAlgorithms;
    }

    public Map<String, MetricAlgorithmImplementation> getMetricAlgorithmImplementations() {
        return metricAlgorithmImplementations;
    }

    public Map<String, ExportModule> getExportModules() {
        return exportModules;
    }

    public Map<String, CudaDeviceInfo> getCudaDevices() {
        return cudaCapableDevices;
    }

    @Autowired
    public void setEnv(Environment env) throws Exception {
        //this.env = env;
        if (env != null) {
            this.condaPath = env.getProperty("coria.metrics.conda.path");
            if (this.condaPath == null) {
                throw new Exception("Property coria.metrics.conda.path is not set to a valid conda executable path.");
            }
            this.condaEnvironmentName = env.getProperty("coria.metrics.conda.environment", "base");
            this.cudaDeviceQueryPath = env.getProperty("coria.metrics.cuda-devicequery-path");

            /*
             * Tries to determine the CORIA working directory.<br/>
             * First the <code>application.properties</code> setting <code>coria.working-directory</code> will be checked,
             * if it does not exist the system environment property <code>CORIA_HOME</code> will be read. If this property
             * also does not yield a result an exception will be thrown.
             */
            this.workingDirectory = env.getProperty("coria.working-directory");
            logger.debug("determining coria working directory...");
            if (this.workingDirectory == null || this.workingDirectory.trim().length() == 0) {
                logger.debug("could not load working directory from properties, trying system variable 'CORIA_HOME'...");
                this.workingDirectory = System.getenv("CORIA_HOME");
                if (this.workingDirectory == null || this.workingDirectory.trim().length() == 0) {
                    logger.error("could not determine working directory");
                    throw new Exception("could not determine working directory");
                }
            }
            File dir = new File(this.workingDirectory);
            if (!dir.exists() && !dir.mkdirs()) {
                logger.error("Working directory does not exist. Could not create directory.");
                throw new Exception("Working directory does not exist. Could not create directory.");
            }
            logger.debug("using following directory as coria home: {}", this.workingDirectory);
        }
    }

    @Autowired
    public void setDatasetEntityImportModules(List<DatasetEntityImportModule> datasetEntityImportModules) {
        datasetEntityImportModules.forEach(importModule -> this.datasetEntityImportModules.put(importModule.getId(), importModule));
    }

    @Autowired
    public void setDatasetEntityImportModuleFactories(List<ImportModuleFactory<DatasetEntityImportModule>> importModuleFactories) {
        importModuleFactories.forEach(factory -> factory.getList().forEach(module -> {
            System.out.println(module.toString());
            this.datasetEntityImportModules.put(module.getId(), module);
        }));
    }

    @Autowired
    public void setASLocationEntityImportModuleFactories(List<ImportModuleFactory<ASLocationEntityImportModule>> importModuleFactories) {
        importModuleFactories.forEach(factory -> factory.getList().forEach(module -> asLocationEntityImportModules.put(module.getId(), module)));
    }

    @Autowired
    public void setASOrganizationEntityImportModuleFactories(List<ImportModuleFactory<ASOrganizationEntityImportModule>> importModuleFactories) {
        importModuleFactories.forEach(factory -> factory.getList().forEach(module -> asOrganizationEntityImportModules.put(module.getId(), module)));
    }

    @Autowired
    public void setCudaDeviceInfoFactory(CudaDeviceInfoFactory cudaDeviceInfoFactory) {
        try {
            cudaDeviceInfoFactory.getList().forEach(deviceInfo -> cudaCapableDevices.put(deviceInfo.getId(), deviceInfo));
        } catch (Exception e) { //Even if CUDA is not available, we can still work with the other CPU-based implementations
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Autowired
    public void setMetricAlgorithmImplementations(List<MetricAlgorithmImplementation> metricAlgorithmImplementations) {
        metricAlgorithmImplementations.forEach(metricAlgorithmImplementation -> this.metricAlgorithmImplementations.put(metricAlgorithmImplementation.getId(), metricAlgorithmImplementation));
    }

    @Autowired
    public void setMetricAlgorithmImplementationFactories(List<MetricAlgorithmImplementationFactory> metricAlgorithmImplementationFactories) {
        metricAlgorithmImplementationFactories.forEach(factory -> {
            try {
                factory.getList().forEach(module -> metricAlgorithmImplementations.put(module.getId(), module));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Autowired
    public void setMetricMultiAlgorithmImplementationFactories(List<MetricMultiAlgorithmImplementationFactory> metricMultiAlgorithmImplementationFactories) {
        metricMultiAlgorithmImplementationFactories.forEach(factory -> factory.getList().forEach(module -> metricMultiAlgorithmImplementations.put(module.getId(), module)));
    }

    @Autowired
    public void setExportModules(List<ExportModule> exportModules) {
        exportModules.forEach(exportModule -> this.exportModules.put(exportModule.getId(), exportModule));
    }


    public ExportModule getExportModule(String id) {
        return exportModules.get(id);
    }

    public DatasetEntityImportModule getDatasetEntityImportModule(String id) {
        return datasetEntityImportModules.get(id);
    }

    public ASLocationEntityImportModule getASLocationEntityImportModule(String id) {
        return asLocationEntityImportModules.get(id);
    }

    public ASOrganizationEntityImportModule getASOrganizationEntityImportModule(String id) {
        return asOrganizationEntityImportModules.get(id);
    }

    public MetricAlgorithmImplementation getMetricAlgorithmImplementation(String id) throws Exception {
        var mai = metricAlgorithmImplementations.get(id);
        if (mai == null) {
            throw new Exception("Unknown Metric Algorithm Implementation ID: " + id);
        }
        return mai;
    }

    public MetricMultiAlgorithmImplementation getMetricMultiAlgorithmImplementation(String id) {
        return metricMultiAlgorithmImplementations.get(id);
    }


    public MetricAlgorithm getMetricAlgorithmByName(String name) throws Exception {
        var ma = this.metricAlgorithms.get(name);
        if (ma == null) {
            throw new Exception("Unknown Metric Algorithm name: " + name);
        }
        return ma;
    }

    public CudaDeviceInfo getCudaDeviceInfo(String id) {
        return cudaCapableDevices.get(id);
    }

    public String getCondaEnvironmentName() {
        return condaEnvironmentName;
    }

    public String getCondaPath() {
        return condaPath;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public String getCudaDeviceQueryPath() {
        return cudaDeviceQueryPath;
    }

    @Override
    public String toString() {
        return "AppContext{" +
                "workingDirectory='" + workingDirectory + '\'' +
                ", condaEnvironmentName='" + condaEnvironmentName + '\'' +
                ", condaPath='" + condaPath + '\'' +
                ", cudaDeviceQueryPath='" + cudaDeviceQueryPath + '\'' +
                ", datasetEntityImportModules=" + datasetEntityImportModules.size() +
                ", asLocationEntityImportModules=" + asLocationEntityImportModules.size() +
                ", asOrganizationEntityImportModules=" + asOrganizationEntityImportModules.size() +
                ", exportModules=" + exportModules.size() +
                ", metricAlgorithms=" + metricAlgorithms.size() +
                ", metricAlgorithmImplementations=" + metricAlgorithmImplementations.size() +
                ", metricMultiAlgorithmImplementations=" + metricMultiAlgorithmImplementations.size() +
                ", cudaCapableDevices=" + cudaCapableDevices.size()
                +
                '}';
    }
}
