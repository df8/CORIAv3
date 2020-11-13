package com.coria.v3.controller;

import com.coria.v3.config.AppContext;
import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.export.ExportModule;
import com.coria.v3.export.ExportResult;
import com.coria.v3.repository.DatasetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * ExportDatasetController processes incoming REST requests.
 * This is the only controller that was implemented without GraphQL by design in order to be able to collect all request parameters in a single GET request.
 */
@Controller
public class ExportDatasetController {
    private final Logger logger = LoggerFactory.getLogger(ExportDatasetController.class);
    protected AppContext appContext;
    protected DatasetRepository datasetRepository;

    @Autowired
    public void setDatasetRepository(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    @Autowired
    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    @RequestMapping(value = "/dataset-export/{exportModuleId}/{datasetId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> handleDatasetExport(@PathVariable String exportModuleId, @PathVariable UUID datasetId, HttpServletRequest request) {
        logger.debug("received export request for dataset {}", datasetId);
        ExportModule exportModule = appContext.getExportModule(exportModuleId);

        if (exportModule == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"Invalid export adapter.\"}");
        }


        DatasetEntity dataset = datasetRepository.findById(datasetId).orElse(null);
        if (dataset == null) {
            return ResponseEntity.badRequest().body("{\"error\":\"Dataset not found\"}");
        }

        //try reading additional parameters
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) params.put(key, values[0]);
        });

        ExportResult result = exportModule.exportDataset(dataset, params);

        HttpHeaders responseHeaders = new HttpHeaders();

        responseHeaders.add(HttpHeaders.CONTENT_TYPE, result.getContentType());
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFileName() + "\"");

        return new ResponseEntity<>(result.getExportResult(), responseHeaders, HttpStatus.OK);
    }
}
