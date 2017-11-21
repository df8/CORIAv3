package com.bigbasti.coria.controller;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.StorageModule;
import com.bigbasti.coria.metrics.MetricModule;
import com.bigbasti.coria.metrics.MetricInfo;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.FileSystemNotFoundException;
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

    /**
     * Reads all metrics executed on a given dataset
     * @param datasetid dataset to read the metrics for
     * @return array of all metrics for a dataset
     */
    @GetMapping(path = "/dataset/{datasetid}")
    public @ResponseBody ResponseEntity getMetricsForDataSet(@PathVariable("datasetid") String datasetid){
        logger.trace("retrieving metricModules for dataset {}", datasetid);
        StorageModule storage = getActiveStorageModule();

        List<MetricInfo> metrics = storage.getMetricInfos(datasetid);
        if(metrics == null){
            return ResponseEntity.status(500).build();
        }

        return ResponseEntity.ok(metrics);
    }

    /**
     * Starts execution of a given metric.<br/>
     * This method operates async, meaning each time it is called a new thread is created to execute the metric module.<br/>
     * Calling this method instantly returns a 200 ok response, which just indicates that the thread was started successful
     * if you then need to know how the metric is doing you need to query all metrics for a dataset and filter the started one
     * @param metricid metric to start executing
     * @param datasetid dataset on which to execute the metric
     * @return 200 ok instantly for executing the thread
     */
    @Async
    @PostMapping("/start")
    public @ResponseBody
    Future<ResponseEntity> startMetricExecution(@RequestParam("identification") String metricid, @RequestParam("datasetid") String datasetid) {
        logger.debug("starting metricModule {} on dataset {}", metricid, datasetid);

        StorageModule storage = null;
        MetricModule metricModule = null;
        MetricInfo mInfo = null;
        try {
            Thread.currentThread().setName(metricid);
            logger.debug("begin metricModule execution for {}", metricid);

            storage = getActiveStorageModule();
            metricModule = getMetricModule(metricid);

            logger.debug("inserting new metricModule information to dataset {}", datasetid);
            mInfo = new MetricInfo("", metricModule.getName(), metricModule.getShortcut(), metricModule.getProvider(), metricModule.getTechnology(), new Date(), null);
            mInfo.setType(metricModule.getType());

            logger.debug("checking if dataset already contains this metricModule");
            List<MetricInfo> infos = storage.getMetricInfos(datasetid);
            boolean metricAlreadyPresent = false;
            for(MetricInfo i : infos){
                if(i.getShortcut().equals(mInfo.getShortcut())){
                    metricAlreadyPresent = true;
                    logger.debug("metricModule is being reexecuted");
                    mInfo = i;
                    mInfo.setExecutionFinished(null);
                    mInfo.setExecutionStarted(new Date());
                    mInfo.setProvider(metricModule.getProvider());
                    mInfo.setTechnology(metricModule.getTechnology());
                    mInfo.setStatus(MetricInfo.MetricStatus.RUNNING);
                    mInfo.setName(metricModule.getName());
                    storage.updateMetricInfo(mInfo);
                }
            }
            if(!metricAlreadyPresent) {
                String newIndex = storage.addMetricInfo(mInfo, datasetid);
                try {
                    //check if index was returned
                    Integer index = Integer.getInteger(newIndex);
                    mInfo.setId(newIndex);
                } catch (Exception ex) {
                    //there was an error while isnerting
                    logger.error("error while inserting metricModule, canceling execution");
                    setMetricInfoToFailed(storage, mInfo, "MetricModule could not be inserted into the database: " + ex.getMessage());
                    return new AsyncResult<>(ResponseEntity.status(500).build());
                }
            }

            logger.debug("loading dataset {} from db...", datasetid);
            DataSet dataset = storage.getDataSet(datasetid);                //System.gc();
            DataSet updatedSet = metricModule.performCalculations(dataset);
            if(updatedSet == null){
                //error in db execution
                setMetricInfoToFailed(storage, mInfo, "Error while MetricModule calculation. Additional infos in the log file");
                return new AsyncResult<>(ResponseEntity.status(500).build());
            }
            if(metricModule.getType() == MetricInfo.MetricType.DATASET){
                //DataSet metricModules often have a specific result, this result is saved in the metricInfo
                mInfo.setValue(updatedSet.getAttribute(metricModule.getShortcut()));
            }

            logger.debug("finished metricModule calculation, updating dataset in db...");
            String result = storage.updateDataSet(updatedSet);              //System.gc();
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
                setMetricInfoToFailed(storage, mInfo, "Error while updating the MetricInfo in the database. Additional infos in the log file. You can try to execute this metricModule one more time. The metricModule results could be available in the Dataset even if this metricModule is displayed as 'in progress'");
                return new AsyncResult<>(ResponseEntity.status(500).build());
            }
            logger.debug("metricModule execution {} finished for {}", metricid, datasetid);
        } catch (Exception e) {
            //something went wrong while executing the metricModule
            if(e instanceof FileSystemNotFoundException){
                //this usually happens when the module resources were not copied into coria-api resources
                //if this is the case please perform a maven install on the root project
                setMetricInfoToFailed(storage, mInfo, "Could not find required file resource for metric execution. Please contact your system administrator and check if all resources are in place.");
            }else {
                setMetricInfoToFailed(storage, mInfo, e.getMessage());
            }
            e.printStackTrace();
        }
        return new AsyncResult<>(ResponseEntity.ok(null));
    }

    /**
     * convenience method to quickly change metric info to failed
     * @param storage storage module to save the updated data
     * @param mInfo metricInfo to update
     * @param message message to write in the update
     */
    private void setMetricInfoToFailed(StorageModule storage, MetricInfo mInfo, String message) {
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
