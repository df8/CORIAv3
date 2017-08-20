package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Sebastian Gross
 * Offers convenience methods for working with Graphstream
 */
public class GSHelper {
    public static Graph createGraphFromDataSet(DataSet dataset){
        Logger logger = LoggerFactory.getLogger(GSHelper.class);
        Graph g = new DefaultGraph("temp");
        g.setStrict(false);
        g.setAutoCreate(true); //automatically create nodes based on edges
        for (CoriaEdge edge : dataset.getEdges()) {
            try {
                logger.trace("Edge: " + edge);
                Edge e = g.addEdge(edge.getSourceNode() + "->" + edge.getDestinationNode(), edge.getSourceNode(), edge.getDestinationNode());
                for(Map.Entry<String, String> att : edge.getAttributes().entrySet()){
                    e.addAttribute(att.getKey(), att.getValue());
                }
            } catch (Exception ex) {
                logger.error("failed creating edge for CoriaEdge {}", edge);
                logger.error(ex.getMessage());
                return null;
            }
        }
        return g;
    }

    public static DataSet mergeDatasets(DataSet first, DataSet second, List<String> forbiddenAttributes){
        Logger logger = LoggerFactory.getLogger(GSHelper.class);
        Graph mergedGraph = createGraphFromDataSet(first);
        logger.debug("creating merged internal graph...");
        for (CoriaEdge edge : second.getEdges()) {
            try {
                logger.trace("Edge: " + edge);
                Edge e = mergedGraph.addEdge(edge.getSourceNode() + "->" + edge.getDestinationNode(), edge.getSourceNode(), edge.getDestinationNode());
            } catch (Exception ex) {
                logger.error("failed creating edge for CoriaEdge {}", edge);
                logger.error(ex.getMessage());
                return null;
            }
        }
        logger.debug("graph successfully created, building merged dataset...");
        DataSet merged = new DataSet();

        logger.debug("merging nodes...");

        for(Node node : mergedGraph.getEachNode()){
            CoriaNode cn = new CoriaNode();
            Optional<CoriaNode> optFirstNode = first.getNodes().stream().filter(coriaNode -> coriaNode.getAsid().equals(node.getId())).findFirst();
            CoriaNode fromFirst = null;
            if(optFirstNode.isPresent()){
                fromFirst = optFirstNode.get();
            }
            CoriaNode fromSecond = null;
            Optional<CoriaNode> optSecondNode = second.getNodes().stream().filter(coriaNode -> coriaNode.getAsid().equals(node.getId())).findFirst();
            if(optSecondNode.isPresent()){
                fromSecond = optSecondNode.get();
            }

            if(fromFirst != null && fromSecond == null){
                //1. Node is only in first -> take all information from first
                cn.setName(fromFirst.getName());
                cn.setAsid(fromFirst.getAsid());
//                cn.setRiscScore(fromFirst.getRiscScore());        //this value is not valid after merging
                cn.setAttributes(filterAttributes(fromFirst.getAttributes(), forbiddenAttributes));
            }else if(fromSecond != null && fromFirst == null){
                //2. Node is only in first -> take all information from first
                cn.setName(fromSecond.getName());
                cn.setAsid(fromSecond.getAsid());
//                cn.setRiscScore(fromSecond.getRiscScore());       //this value is not valid after merging
                cn.setAttributes(filterAttributes(fromSecond.getAttributes(), forbiddenAttributes));
            }else if(fromFirst != null && fromSecond != null){
                //3. Node is found in both DataSets -> try merging
                cn.setAsid(fromFirst.getAsid()); //this one is the same on both sets

                if(fromFirst.getName().equals(fromFirst.getAsid())){
                    //first dataset has no name for as -> check the second
                    if(fromSecond.getName().equals(fromSecond.getAsid())){
                        //the second has also no name for as -> use asid as name
                        cn.setName(fromFirst.getAsid());
                    }else{
                        //the second has a separate name for as -> use this
                        cn.setName(fromSecond.getName());
                    }
                }else{
                    //first has name for as -> use it
                    cn.setName(fromFirst.getName());
                }

                cn.setAttributes(
                        syncAttributes(
                                filterAttributes(fromFirst.getAttributes(), forbiddenAttributes),
                                filterAttributes(fromSecond.getAttributes(), forbiddenAttributes)));

            }else{
                //something is wrong here!
                logger.warn("ooops ¯\\_(ツ)_/¯");
            }
            merged.getNodes().add(cn);
        }

        logger.debug("merging edges...");

        for(Edge edge : mergedGraph.getEachEdge()){
            CoriaEdge ce = new CoriaEdge();
            CoriaEdge fromFirst = first.getEdges().stream().filter(coriaEdge -> coriaEdge.getName().equals(edge.getId())).findFirst().get();
            CoriaEdge fromSecond = second.getEdges().stream().filter(coriaEdge -> coriaEdge.getName().equals(edge.getId())).findFirst().get();

            if(fromFirst != null && fromSecond == null){
                //1. Edge is only in first -> take all information from first
                ce.setName(fromFirst.getName());
                ce.setAttributes(filterAttributes(fromFirst.getAttributes(), forbiddenAttributes));
            }else if(fromSecond != null && fromFirst == null){
                //2. Edge is only in first -> take all information from first
                ce.setName(fromSecond.getName());
                ce.setAttributes(filterAttributes(fromSecond.getAttributes(), forbiddenAttributes));
            }else if(fromFirst != null && fromSecond != null){
                //3. Edge is found in both DataSets -> try merging
                ce.setName(fromSecond.getName());

                ce.setAttributes(
                        syncAttributes(
                                filterAttributes(fromFirst.getAttributes(), forbiddenAttributes),
                                filterAttributes(fromSecond.getAttributes(), forbiddenAttributes)));

            }else{
                //something is wrong here!
            }
            merged.getEdges().add(ce);
        }
        logger.debug("finished merging");

        return merged;
    }

    private static Map<String, String> filterAttributes(Map<String, String> attributes, List<String> forbidden){
        Map<String, String> filtered = new HashMap<>();
        for(String key : attributes.keySet()){
            boolean forb = false;
            for(String f : forbidden){
                if(f.equals(key)){          //filter attributes
                    forb = true;
                }
                if(f.startsWith(key)){      //filter sub attributes
                    forb = true;
                }
            }
            if(!forb){
                filtered.put(key, attributes.get(key));
            }
        }
        return filtered;
    }

    private static Map<String, String> syncAttributes(Map<String, String> first, Map<String, String> second){
        Map<String, String> synced = new HashMap<>();

        for(String firstKey : first.keySet()){
            if(second.containsKey(firstKey)){
                //both contain the same key -> take from second
                synced.put(firstKey, second.get(firstKey));
            }else{
                //second doesn't contain this key -> take from first
                synced.put(firstKey, first.get(firstKey));
            }
        }

        for(String secondKey : second.keySet()){
            if(!synced.containsKey(secondKey)){
                //since first doesn't contain this key (or it would already be in syned) we take this key
                synced.put(secondKey, second.get(secondKey));
            }
        }
        return synced;
    }
}
