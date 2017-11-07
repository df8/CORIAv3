package com.bigbasti.coria.controller;

import com.bigbasti.coria.export.ExportModule;
import com.bigbasti.coria.parser.ImportModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by Sebastian Gross
 */
@Controller
@RequestMapping(path = "/api/modules")
public class ModulesController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(ModulesController.class);

    /**
     * get all registered import providers
     * @return
     */
    @GetMapping(path = "/import")
    public @ResponseBody List<ImportModule> getAllImportProviders(){
        logger.debug("retrieving all input providers");
        return availableImportModules;
    }

    /**
     * get all registered export providers
     * @return
     */
    @GetMapping(path = "/export")
    public @ResponseBody List<ExportModule> getAllExportProviders(){
        logger.debug("retrieving all export providers");
        return exportModules;
    }

    /**
     * get all registered metricModules providers
     * @return
     */
    @GetMapping(path = "/metrics")
    public @ResponseBody
    ResponseEntity getAllMetrics() {
        logger.debug("retrieving all available metricModules");
        return ResponseEntity.ok(metricModules);
    }

    /**
     * get all registered storage providers
     * @return
     */
    @GetMapping(path = "/storage")
    public @ResponseBody
    ResponseEntity getAllStorageadapters() {
        logger.debug("retrieving all available storage adapters");
        return ResponseEntity.ok(storageModules);
    }
}
