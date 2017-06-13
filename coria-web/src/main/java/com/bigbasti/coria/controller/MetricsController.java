package com.bigbasti.coria.controller;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.db.StorageStatus;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import com.bigbasti.coria.model.DataSetUpload;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.parser.InputParser;
import com.google.common.base.Strings;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by Sebastian Gross on 02.06.2017.
 */
@Controller
@RequestMapping(path = "/api/metrics")
public class MetricsController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(MetricsController.class);

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public @ResponseBody
    ResponseEntity getAllMetrics() {
        logger.debug("retrieving all available metrics");

        return ResponseEntity.ok(metrics);
    }

    @GetMapping(path = "/dataset/{datasetid}")
    public @ResponseBody ResponseEntity getMetricsForDataSet(@PathVariable("datasetid") String datasetid){
        logger.debug("retrieving metrics for dataset {}", datasetid);
        DataStorage storage = getActiveStorage();

        List<MetricInfo> metrics = storage.getMetricInfos(datasetid);
        if(metrics == null){
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok(metrics);
    }

    @Async
    @PostMapping("/start")
    public @ResponseBody
    Future<ResponseEntity> startMetricProcession(@RequestParam("identification") String metricid, @RequestParam("datasetid") String datasetid) {
        logger.debug("starting metric {} on dataset {}", metricid, datasetid);

        //TODO: check if this metric is already present on the dataset -> delete it if so
        DataStorage storage = null;
        Metric metric = null;
        MetricInfo mInfo = null;
        try {
            Thread.currentThread().setName(metricid);
            logger.debug("begin metric execution for {}", metricid);

            storage = getActiveStorage();
            metric = getMetric(metricid);

            logger.debug("inserting new metric information to dataset {}", datasetid);
            mInfo = new MetricInfo("", metric.getName(), metric.getShortcut(), metric.getProvider(), metric.getTechnology(), new Date(), null);
            mInfo.setType(metric.getType());
            String newIndex = storage.addMetricInfo(mInfo, datasetid);
            try {
                //check if index was returned
                Integer index = Integer.getInteger(newIndex);
                mInfo.setId(newIndex);
            } catch (Exception ex) {
                //there was an error while isnerting
                logger.error("error while inserting metric, canceling execution");
                setMetricInfoToFailed(storage, mInfo, "Metric could not be inserted into the database: " + ex.getMessage());
                return new AsyncResult<>(ResponseEntity.status(500).build());
            }

            logger.debug("loading dataset {} from db...", datasetid);
            DataSet dataset = storage.getDataSet(datasetid);
            logger.debug("starting metric calculation for dataset {}", datasetid);
            DataSet updatedSet = metric.performCalculations(dataset);
            if(updatedSet == null){
                //error in db execution
                setMetricInfoToFailed(storage, mInfo, "Error while Metric calculation. Additional infos in the log file");
                return new AsyncResult<>(ResponseEntity.status(500).build());
            }

            logger.debug("finished metric calculation, updating dataset in db...");
            String result = storage.updateDataSet(updatedSet);
            if(!Strings.isNullOrEmpty(result)){
                //error in db execution
                setMetricInfoToFailed(storage, mInfo, "Error while updating the DataSet in the database. Additional infos in the log file");
                return new AsyncResult<>(ResponseEntity.status(500).build());
            }

            mInfo.setExecutionFinished(new Date());
            mInfo.setStatus(MetricInfo.MetricStatus.FINISHED);
            result = storage.updateMetricInfo(mInfo);
            if(!Strings.isNullOrEmpty(result)){
                //error in db execution
                setMetricInfoToFailed(storage, mInfo, "Error while updating the MetricInfo in the database. Additional infos in the log file. You can try to execute this metric one more time. The metric results could be available in the Dataset even if this metric is displayed as 'in progress'");
                return new AsyncResult<>(ResponseEntity.status(500).build());
            }
            logger.debug("metric execution {} finished for {}", metricid, datasetid);
        } catch (Exception e) {
            //something went wrong while executing the metric
            setMetricInfoToFailed(storage, mInfo, e.getMessage());
            e.printStackTrace();
        }
        return new AsyncResult<>(ResponseEntity.ok(null));
    }

    private void setMetricInfoToFailed(DataStorage storage, MetricInfo mInfo, String message) {
        logger.error("error while metrirc calculation: {}", message);
        if(mInfo != null){
            mInfo.setStatus(MetricInfo.MetricStatus.FAILED);
            mInfo.setMessage(message);
            mInfo.setExecutionFinished(new Date());
            if(storage != null && storage.getStorageStatus().isReadyToUse()){
                storage.updateMetricInfo(mInfo);
            }else{
                logger.warn("storage is not ready to use, can't update the metricinfo");
            }
        }
    }
}
