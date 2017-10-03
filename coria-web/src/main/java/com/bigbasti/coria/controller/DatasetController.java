package com.bigbasti.coria.controller;

import com.bigbasti.coria.config.AppContext;
import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.db.DataStorage;
import com.bigbasti.coria.export.ExportAdapter;
import com.bigbasti.coria.export.ExportResult;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metric.gs.GSHelper;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.model.DataSetMerge;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sebastian Gross
 */
@Controller
@RequestMapping(path = "/api/datasets")
public class DatasetController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(DatasetController.class);

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

        List<DataSet> datasets = null;
        try {
            datasets = storage.getDataSetsShort();
        } catch (Exception e) {
            logger.error("could not load datasets from db: {}", e.getMessage());
            if(dataStorages.stream().filter(dataStorage -> dataStorage.getStorageStatus().isReadyToUse()).toArray().length == 0){
                return ResponseEntity.status(500).body("{\"error\":\"There is no connection to the database. Please check CORIA configuration and if the database server is running and is reachable then restart the application.\"}");
            }
        }

        return ResponseEntity.ok(datasets);
    }

    @GetMapping(path = "/{datasetid}")
    public @ResponseBody ResponseEntity getDataSet(@PathVariable("datasetid") String datasetid){
        logger.debug("retrieving dataset {}", datasetid);
        DataStorage storage = getActiveStorage();

        DataSet ds = storage.getDataSet(datasetid);

        return ResponseEntity.ok(ds);
    }

    @GetMapping(path = "/short/{datasetid}")
    public @ResponseBody ResponseEntity getDataSetShort(@PathVariable("datasetid") String datasetid){
        logger.debug("retrieving dataset {}", datasetid);
        DataStorage storage = getActiveStorage();

        DataSet ds = storage.getDataSet(datasetid);

        return ResponseEntity.ok(ds);
    }

    @PostMapping("/export/{datasetid}")
    public @ResponseBody
    ResponseEntity handleDataSetExport(@PathVariable("datasetid") String datasetid, HttpServletRequest request) {
        logger.debug("received export request for dataset {}", datasetid);

        String adapterId = request.getParameter("adapterid");
        if(Strings.isNullOrEmpty(adapterId)){
            return ResponseEntity.badRequest().body("{\"error\":\"DataSet could not be exported because an invalid export adapter was specified!\"}");
        }

        ExportAdapter exportAdapter = getExportAdapter(adapterId);
        DataSet dataSet = getActiveStorage().getDataSet(datasetid);
        if(dataSet == null){
            return ResponseEntity.badRequest().body("{\"error\":\"DataSet could not be exported because the specified dataset could not be found!\"}");
        }

        //try reading additional parameters
        Map<String, Object> params = new HashMap<>();
        try {
            //load all additional text fields
            List<Part> additionalParts = request.getParts()
                    .stream()
                    .filter(part -> part.getName().startsWith("additional_"))
                    .collect(Collectors.toList());
            if(additionalParts.size() > 0){
                for(Part p : additionalParts){
                    int read = 0;
                    final byte[] bytes = new byte[1024];
                    InputStream filecontent = p.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    while ((read = filecontent.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                    //remove dynamically added "additional_" string from field name
                    params.put(p.getName().substring("additional_".length()), new String(out.toByteArray(), Charset.defaultCharset()));
                }
            }
        } catch (Exception e) {
            logger.error("could not read additional parameters");
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\":\"DataSet could not be stored because could not read additional parameters\"}");
        }

        ExportResult result = exportAdapter.exportDataSet(dataSet, params);

        logger.debug("finished exporting dataset");


        return ResponseEntity.ok(result);
    }

    /**
     * Handles the upload of a new dataset containing its name, the target parser and the raw data
     * @param upload form fields containing the data
     * @return http ok if import was successful of http 500 if not
     */
    @PostMapping("/upload")
    public @ResponseBody
    ResponseEntity handleDataSetUpload(DataSetUpload upload, HttpServletRequest request) {
        logger.debug("received dataset file for import");

        if(upload.isValid()){
            InputParser parser = getInputParser(upload.getParser());

            try {
                //try reading additional parameters
                Map<String, Object> params = new HashMap<>();
                try {
                    //load all uploaded files
                    List<Part> fileParts = request.getParts()
                            .stream()
                            .filter(part -> part.getSubmittedFileName() != null && !part.getName().equals("file"))
                            .collect(Collectors.toList());
                    if(fileParts.size() > 0){
                        for(Part p : fileParts){
                            int read = 0;
                            final byte[] bytes = new byte[1024];
                            InputStream filecontent = p.getInputStream();
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            while ((read = filecontent.read(bytes)) != -1) {
                                out.write(bytes, 0, read);
                            }
                            params.put(p.getName(), out.toByteArray());
                        }
                    }
                    //load all additional text fields
                    List<Part> additionalParts = request.getParts()
                            .stream()
                            .filter(part -> part.getName().startsWith("additional_"))
                            .collect(Collectors.toList());
                    if(additionalParts.size() > 0){
                        for(Part p : additionalParts){
                            int read = 0;
                            final byte[] bytes = new byte[1024];
                            InputStream filecontent = p.getInputStream();
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            while ((read = filecontent.read(bytes)) != -1) {
                                out.write(bytes, 0, read);
                            }
                            //remove dynamically added "additional_" string from field name
                            params.put(p.getName().substring("additional_".length()), new String(out.toByteArray(), Charset.defaultCharset()));
                        }
                    }
                } catch (ServletException e) {
                    logger.error("could not read additional parameters");
                    e.printStackTrace();
                    return ResponseEntity.status(500).body("{\"error\":\"DataSet could not be stored because could not read additional parameters\"}");
                }

                parser.parseInformation(upload.getFile().getBytes(), params);
                List<CoriaEdge> edges = parser.getParsedEdges();
                List<CoriaNode> nodes = parser.getParsedNodes();
                if(edges == null || nodes == null){
                    //user specified an unknown parser
                    logger.error("the parser " + upload.getParser() + " is unknown to coria");
                    return ResponseEntity.status(500).body("{\"error\":\"The specified parser is not registered in CORIA\"}");
                }

                try{
                    logger.debug("successfully parsed {} nodes and {} edges from upload", nodes.size(), edges.size());
                    if(edges.size() == 0 || nodes.size() == 0){
                        logger.debug("no edges/nodes was created by the import - there must be something wrong with the data file");
                        return ResponseEntity.status(500).body("{\"error\":\"no data was created by the import - there must be something wrong with the data file\"}");
                    }

                    if(parser.getImportType() == InputParser.ImportType.DATASET){
                        //if type is DATASET then everything should be already done and we can
                        //directly save the loaded dataset
                        DataSet parsed = parser.getDataSet();
                        parsed.setName(upload.getName());
                        String result = getActiveStorage().addDataSet(parsed);
                        if (!Strings.isNullOrEmpty(result)) {
                            logger.error("new dataset was not stored");
                            return ResponseEntity.status(500).body("{\"error\":\"DataSet could not be stored because of internal error\"}");
                        }
                    }else {
                        //setup a temp graph using graphstream to check and prepare graph data
                        Graph g = new DefaultGraph("Temp Graph");
                        g.setStrict(false);
                        g.setAutoCreate(true); //automatically create nodes based on edges
                        logger.debug("trying to create a temp graph with provided data...");

                        //create graph based on edges
                        for (CoriaEdge edge : edges) {
                            logger.trace("Edge: " + edge);
                            Edge e = g.addEdge(edge.getSourceNode() + "->" + edge.getDestinationNode(), edge.getSourceNode(), edge.getDestinationNode());
                            if (e == null) {
                                logger.trace("problem with edge {} (possibly this connection exists already in the other direction)", edge.getSourceNode() + "->" + edge.getDestinationNode());
                            }

                        }

                        logger.debug("successfully created temp graph containing {} nodes and {} edges", g.getNodeCount(), g.getEdgeCount());
                        logger.debug("creating internal dataset from temp graph...");

                        DataSet dataSet = new DataSet();
                        dataSet.setCreated(new Date());

                        edges = new ArrayList<>();
//                    nodes = new ArrayList<>();

                        List<String> nodeDict = new ArrayList<>();
                        List<String> edgeDict = new ArrayList<>();

                        for (Edge e : g.getEachEdge()) {
                            CoriaNode fn = null;
                            if (e.getSourceNode() != null) {
                                fn = new CoriaNode(e.getSourceNode().getId(), e.getSourceNode().getId());
                            }
                            CoriaNode dn = null;
                            if (e.getTargetNode() != null) {
                                dn = new CoriaNode(e.getTargetNode().getId(), e.getTargetNode().getId());
                            }
                            CoriaEdge ce = null;
                            if (fn != null && dn != null) {
                                ce = new CoriaEdge(e.getId(), e.getId(), fn.getId(), dn.getId());
                                // check id there is already a node with this id in the database
//                            if (!nodeDict.contains(fn.getId())) { nodes.add(fn); nodeDict.add(fn.getId());}
//                            if (!nodeDict.contains(dn.getId())) { nodes.add(dn); nodeDict.add(dn.getId());}
                                //TODO: are duplicates of edges possible? duplicate checks are extremely time consuming
//                            if (!edgeDict.contains(ce.getId())) { edges.add(ce); edgeDict.add(ce.getId());}
                                edges.add(ce);
                            }
                        }
                        logger.debug("successfully created internal dataset, persisting...");

                        dataSet.setEdges(edges);
                        dataSet.setNodes(nodes);
                        dataSet.setNodesCount(nodes.size());
                        dataSet.setEdgesCount(edges.size());
                        dataSet.setName(upload.getName());
                        String result = getActiveStorage().addDataSet(dataSet);
                        if (!Strings.isNullOrEmpty(result)) {
                            logger.error("new dataset was not stored");
                            return ResponseEntity.status(500).body("{\"error\":\"DataSet could not be stored because of internal error\"}");
                        }
                    }
                    logger.debug("new dataset successfully stored");
                    return ResponseEntity.ok().build();
                }catch (Exception ex){
                    logger.error("Saving dataset failed: {}", ex.getMessage());
                }

                //if we come this far there is something wring with the upload/parser
                return ResponseEntity.status(500).body("{\"error\":\"Please check if the file has the appropriate format\"}");

            } catch (FormatNotSupportedException e) {
                return ResponseEntity.status(500).body("{\"error\":\"The uploaded data does not match the specified format (" + e.getMessage() + ")\"}");
            } catch (IOException e) {
                return ResponseEntity.status(500).body("{\"error\":\"Error while processing the uploaded file: " + e.getMessage() + "\"}");
            }
        }else{
            logger.error("received an invalid upload");
            return ResponseEntity.status(500).body("{\"error\":\"Please make sure all required fields are provided\"}");
        }
    }

    @PostMapping("/delete/{datasetid}")
    public @ResponseBody
    ResponseEntity handleDataSetDelete(@PathVariable("datasetid") String datasetid) {
        logger.debug("deleting dataset {}", datasetid);

        getActiveStorage().deleteDataSet(datasetid);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/merge")
    public @ResponseBody
    ResponseEntity handleDataSetMerge(DataSetMerge mergeInfos) {
        if(!mergeInfos.idValid()){
            logger.error("Not all required fields provided for merging");
            return ResponseEntity.status(500).body("{\"error\":\"Please fill out all fields!\"}");
        }

        logger.debug("merging datasets {} and {} (extension:{})", mergeInfos.getFirst(), mergeInfos.getSecond(), mergeInfos.isExtend());

        DataSet first = getActiveStorage().getDataSet(mergeInfos.getFirst());
        DataSet second = getActiveStorage().getDataSet(mergeInfos.getSecond());

        if(first == null || second == null){
            logger.error("One or more datasets could not be found");
            return ResponseEntity.status(500).body("{\"error\":\"One or both DataSets you specified could not be found!\"}");
        }

        logger.debug("starting merging datasets");
        List<String> forbiddenAttributes = new ArrayList<>();
        if(mergeInfos.isExtend()){
            if(first.getEdges().size() != second.getEdges().size()){
                logger.error("Extension not possible because the two selected datasets contain different amounts of edges!");
                return ResponseEntity.status(500).body("{\"error\":\"Extension not possible because the two selected datasets contain different amounts of edges!\"}");
            }
        }else{
            //only for regular merges -> do not merge existing metrics since theyre not valid after merging
            metrics.forEach(metric -> forbiddenAttributes.add(metric.getShortcut()));
        }
        DataSet merged = GSHelper.mergeDatasets(first, second, forbiddenAttributes);
        merged.setName(mergeInfos.getName());
        merged.setCreated(new Date());
        merged.setNodesCount(merged.getNodes().size());
        merged.setEdgesCount(merged.getEdges().size());
        logger.debug("successfully finished merging datasets");
        logger.debug("saving merged dataset to database");
        getActiveStorage().addDataSet(merged);
        logger.debug("finished processing!");

        return ResponseEntity.ok().build();
    }

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        logger.debug("Part Header = {0}", partHeader);
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

}
