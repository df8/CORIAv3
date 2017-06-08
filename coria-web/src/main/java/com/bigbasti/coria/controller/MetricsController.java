package com.bigbasti.coria.controller;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import com.bigbasti.coria.model.DataSetUpload;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.parser.InputParser;
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
    ResponseEntity getAllMetrics(){
        logger.debug("retrieving all available metrics");

        return ResponseEntity.ok(metrics);
    }

    @Async
    @PostMapping("/start")
    public @ResponseBody
    Future<ResponseEntity> handleDataSetUpload(@RequestParam("identification") String metricid, @RequestParam ("datasetid") String datasetid) {
        logger.debug("starting metric {} on dataset {}", metricid, datasetid);

        //TODO: check if this metric is already present on the dataset -> delete it if so

        Thread.currentThread().setName(metricid);
        logger.debug("begin metric execution for {}", metricid);

        DataStorage storage = getActiveStorage();
        Metric metric = getMetric(metricid);

        logger.debug("inserting new metric information to dataset {}", datasetid);
        MetricInfo mInfo = new MetricInfo("", metric.getName(), metric.getShortcut(), metric.getProvider(), metric.getTechnology(), new Date(), null);
        String newIndex = storage.addMetricInfo(mInfo, datasetid);
        try{
            //check if index was returned
            Integer index = Integer.getInteger(newIndex);
            mInfo.setId(newIndex);
        }catch(Exception ex){
            //there was an error while isnerting
            logger.error("error while inserting metric, canceling execution");
            return new AsyncResult<>(ResponseEntity.status(500).build());
        }

        logger.debug("loading dataset {} from db...", datasetid);
        DataSet dataset = storage.getDataSet(datasetid);
        logger.debug("starting metric calculation for dataset {}", datasetid);
        DataSet updatedSet = metric.performCalculations(dataset);

        logger.debug("finished metric calculation, updating dataset in db...");
        storage.updateDataSet(updatedSet);

        mInfo.setExecutionFinished(new Date());
        mInfo.setStatus(MetricInfo.MetricStatus.FINISHED);
        storage.updateMetricInfo(mInfo);
        logger.debug("metric execution {} finished for {}", metricid, datasetid);

        return new AsyncResult<>(ResponseEntity.ok(null));
    }
}
