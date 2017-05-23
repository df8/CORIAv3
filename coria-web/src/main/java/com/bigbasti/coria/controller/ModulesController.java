package com.bigbasti.coria.controller;

import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.parser.InputParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ModulesController {
    private Logger logger = LoggerFactory.getLogger(ModulesController.class);

    @Autowired
    List<InputParser> availableInputParsers;

    @Autowired
    List<DataStorage> dataStorages;


    /**
     * get all registered import providers
     * @return
     */
    @GetMapping(path = "/import")
    public @ResponseBody List<InputParser> getAllImportProviders(){
        logger.debug("retrieving all input providers");
        return availableInputParsers;
    }
}
