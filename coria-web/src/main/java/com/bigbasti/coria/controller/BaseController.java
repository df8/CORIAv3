package com.bigbasti.coria.controller;

import com.bigbasti.coria.config.AppContext;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.parser.InputParser;
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
    List<InputParser> availableInputParsers;

    @Autowired
    List<DataStorage> dataStorages;

    @Autowired
    List<Metric> metrics;

    protected InputParser getInputParser(String id){
        InputParser parser = availableInputParsers
                .stream()
                .filter(inputParser -> inputParser.getIdentification().equals(id))
                .findFirst()
                .get();
        logger.debug("using following inputParser: " + parser);
        return parser;
    }

    protected DataStorage getActiveStorage(){
        String targetDb = AppContext.getInstance().getDatabaseProvider();
        DataStorage storage = dataStorages
                .stream()
                .filter(dataStorage -> dataStorage.getIdentification().equals(targetDb))
                .findFirst()
                .get();
        logger.debug("using dataStorage: " + storage);
        return storage;
    }

    protected Metric getMetric(String id){
        Metric met = metrics
                .stream()
                .filter(m -> m.getIdentification().equals(id))
                .findFirst()
                .get();
        logger.debug("using following Metric: " + met);
        return met;
    }
}
