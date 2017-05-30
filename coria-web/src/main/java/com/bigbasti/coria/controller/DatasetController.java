package com.bigbasti.coria.controller;

import com.bigbasti.coria.config.AppContext;
import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.model.DataSetUpload;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.parser.InputParser;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Sebastian Gross
 */
@Controller
@RequestMapping(path = "/api/datasets")
public class DatasetController {
    private Logger logger = LoggerFactory.getLogger(DatasetController.class);

    @Autowired
    List<InputParser> availableInputParsers;

    @Autowired
    List<DataStorage> dataStorages;

    @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
    public String getFullDataSets(){
        logger.debug("retrieving full dataset list");
        DataStorage storage = getActiveStorage();

        return "forward:/index.html";
    }

    @GetMapping(path = "/short")
    public @ResponseBody ResponseEntity getShortDataSets(){
        logger.debug("retrieving short dataset list");
        DataStorage storage = getActiveStorage();

        List<DataSet> datasets = storage.getDataSetsShort();

        return ResponseEntity.ok(datasets);
    }

    /**
     * Handles the upload of a new dataset containing its name, the target parser and the raw data
     * @param upload form fields containing the data
     * @return http ok if import was successful of http 500 if not
     */
    @PostMapping("/upload")
    public @ResponseBody
    ResponseEntity handleDataSetUpload(DataSetUpload upload) {
        logger.debug("received dataset file for import");

        if(upload.isValid()){
            InputParser parser = getInputParser(upload.getParser());

            try {
                List<CoriaEdge> edges = parser.getParsedObjects(upload.getFile().getBytes());
                List<CoriaNode> nodes = null;
                if(edges == null){
                    //user specified an unknown parser
                    logger.error("the parser " + upload.getParser() + " is unknown to coria");
                    return ResponseEntity.status(500).body("{\"error\":\"The specified parser is not registered in CORIA\"}");
                }

                try{
                    logger.debug("successfully parsed " + edges.size() + " edges from upload");
                    if(edges.size() == 0){
                        logger.debug("no edges was created by the import - there must be something wrong with the data file");
                        return ResponseEntity.status(500).body("{\"error\":\"no data was created by the import - there must be something wrong with the data file\"}");
                    }

                    //setup a temp graph using graphstream to check and prepare graph data
                    Graph g = new DefaultGraph("Temp Graph");
                    g.setStrict(false);
                    g.setAutoCreate(true); //automatically create nodes based on edges
                    logger.debug("trying to create a temp graph with provided data...");

                    //create graph based on edges
                    for(CoriaEdge edge : edges){
                        logger.trace("Edge: " + edge);
                        Edge e = g.addEdge(edge.getSourceNode().getName()+"->"+edge.getDestinationNode().getName(), edge.getSourceNode().getName(), edge.getDestinationNode().getName());
                        //retrieve created nodes to name them properly
                        Node fn = g.getNode(edge.getSourceNode().getName());
                        Node tn = g.getNode(edge.getDestinationNode().getName());
                        //optional: add attributes
                        fn.addAttribute("label", edge.getSourceNode().getName());
                        tn.addAttribute("label", edge.getDestinationNode().getName());
                    }

                    logger.debug("successfully created temp graph containing {} nodes and {} edges", g.getNodeCount(), g.getEdgeCount());
                    logger.debug("creating internal dataset from temp graph...");

                    DataSet dataSet = new DataSet();
                    dataSet.setCreated(new Date());

                    edges = new ArrayList<>();
                    nodes = new ArrayList<>();

                    List<String> nodeDict = new ArrayList<>();
                    List<String> edgeDict = new ArrayList<>();

                    for(Edge e : g.getEachEdge()){
                        CoriaNode fn = null;
                        if(e.getSourceNode() != null) {
                            fn = new CoriaNode(e.getSourceNode().getId(), e.getSourceNode().getId());
                        }
                        CoriaNode dn = null;
                        if(e.getTargetNode() != null) {
                            dn = new CoriaNode(e.getTargetNode().getId(), e.getTargetNode().getId());
                        }
                        CoriaEdge ce = null;
                        if(fn != null && dn != null) {
                            ce = new CoriaEdge(e.getId(), e.getId(), fn, dn);
                            // check id there is already a node with this id in the database
                            if (!nodeDict.contains(fn.getId())) { nodes.add(fn); nodeDict.add(fn.getId());}
                            if (!nodeDict.contains(dn.getId())) { nodes.add(dn); nodeDict.add(dn.getId());}
                            //TODO: are duplicates of edges possible? duplicate checks are extremely time consuming
//                            if (!edgeDict.contains(ce.getId())) { edges.add(ce); edgeDict.add(ce.getId());}
                            edges.add(ce);
                        }
                    }
                    logger.debug("successfully created internal dataset, persisting...");

                    dataSet.setEdges(edges);
                    dataSet.setNodes(nodes);
                    dataSet.setName(upload.getName());
                    getActiveStorage().addDataSet(dataSet);

                    logger.debug("new dataset successfully stored");
                    return ResponseEntity.ok(dataSet);
                }catch (Exception ex){
                    logger.error("Saving Edges failed: {}", ex.getMessage());
                }

                //if we come this far there is something wring with the upload/parser
                return ResponseEntity.status(500).body("{\"error\":\"Please check if the file has the appropriate format\"}");

            } catch (FormatNotSupportedException e) {
                return ResponseEntity.status(500).body("{\"error\":\"The uploaded file does not match the specified format\"}");
            } catch (IOException e) {
                return ResponseEntity.status(500).body("{\"error\":\"Error while processing the uploaded file: " + e.getMessage() + "\"}");
            }
        }else{
            logger.error("received an invalid upload");
            return ResponseEntity.status(500).body("{\"error\":\"Please make sure all required fields are provided\"}");
        }
    }

    private InputParser getInputParser(String id){
        InputParser parser = availableInputParsers
                .stream()
                .filter(inputParser -> inputParser.getIdentification().equals(id))
                .findFirst()
                .get();
        logger.debug("using following inputParser: " + parser);
        return parser;
    }

    private DataStorage getActiveStorage(){
        String targetDb = AppContext.getInstance().getDatabaseProvider();
        DataStorage storage = dataStorages
                .stream()
                .filter(dataStorage -> dataStorage.getIdentification().equals(targetDb))
                .findFirst()
                .get();
        logger.debug("using dataStorage: " + storage);
        return storage;
    }
}
