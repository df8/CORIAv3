package com.bigbasti.coria.controller;

import com.bigbasti.coria.config.AppContext;
import com.bigbasti.coria.db.StorageModule;
import com.bigbasti.coria.export.ExportModule;
import com.bigbasti.coria.metrics.MetricModule;
import com.bigbasti.coria.parser.ImportModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Sebastian Gross on 02.06.2017.
 */
@Component
public class BaseController {
    private Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    List<ImportModule> availableImportModules;

    @Autowired
    List<StorageModule> storageModules;

    @Autowired
    List<MetricModule> metricModules;

    @Autowired
    List<ExportModule> exportModules;

    protected ExportModule getExportModule(String id){
        ExportModule parser = exportModules
                .stream()
                .filter(exportModule -> exportModule.getIdentification().equals(id))
                .findFirst()
                .get();
        logger.debug("using following export adapter: " + parser);
        return parser;
    }

    protected ImportModule getImportModule(String id){
        ImportModule parser = availableImportModules
                .stream()
                .filter(importModule -> importModule.getIdentification().equals(id))
                .findFirst()
                .get();
        logger.debug("using following inputParser: " + parser);
        return parser;
    }

    protected StorageModule getActiveStorageModule(){
        String targetDb = AppContext.getInstance().getDatabaseProvider();
        StorageModule storage = storageModules
                .stream()
                .filter(dataStorage -> dataStorage.getIdentification().equals(targetDb))
                .findFirst()
                .get();
        logger.debug("using dataStorage: " + storage);
        return storage;
    }

    protected MetricModule getMetricModule(String id){
        MetricModule met = metricModules
                .stream()
                .filter(m -> m.getIdentification().equals(id))
                .findFirst()
                .get();
        logger.debug("using following MetricModule: " + met);
        return met;
    }
}
