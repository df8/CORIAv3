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
public class HomeController {
    private Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    List<InputParser> availableInputParsers;

    @Autowired
    List<DataStorage> dataStorage;

    @GetMapping(path = "/")
    public @ResponseBody String getHome(){
        String res = "";
        for(InputParser p : availableInputParsers){
            res = res + "<br/>" + p.getName();
        }
        for(DataStorage s : dataStorage){
            res = res + "<br/>" + s.getName();
        }
        return res;
    }
}
