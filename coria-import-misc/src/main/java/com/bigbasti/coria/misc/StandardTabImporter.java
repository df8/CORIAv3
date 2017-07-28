package com.bigbasti.coria.misc;

import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.parser.InputParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser for ASLink Files provided by CAIDA
 * Created by Sebastian Gross
 */
@Component
public class StandardTabImporter implements InputParser {

    private Logger logger = LoggerFactory.getLogger(StandardTabImporter.class);

    ArrayList<CoriaEdge> importedEdges = new ArrayList<>();
    ArrayList<CoriaNode> importedNodes = new ArrayList<>();

    @Override
    public String getIdentification() {
        return "caida-misc-default-tab-parser";
    }

    @Override
    public String getName() {
        return "Standard tab separated Importer";
    }

    @Override
    public String getDescription() {
        return "<p>This Importer can be used on all data files containing at least <code>two columns</code> of data separated by <code>tabs</code></p>\n" +
                "<p>Each line represents one link (edge) containing the source node and the destination node as the first and second parameter separated by a tab</p>\n" +
                "<p>One example line could look like this: <code>123    321</code> where 123 sould be the source node and 321 the destination node!</p>\n" +
                "<p>All additional data after the first two rows as well as all rows not matching the format will be ignored!</p>\n" +
                "<p>The expected file format is <code>.txt</code> - so please extract archives before uploading</p>";
    }

    @Override
    public String getExpectedFormat() {
        return "Uncompressed Tab-Separated textfile";
    }

    @Override
    public Map<String, String> getAdditionalFields() {
        return new HashMap<>();
    }

    @Override
    public List<CoriaEdge> getParsedEdges() {
        return importedEdges;
    }

    @Override
    public List<CoriaNode> getParsedNodes() {
        return importedNodes;
    }

    public void parseInformation(Object data, Map<String, Object> params) throws FormatNotSupportedException {
        logger.debug("starting import of data");

        String strData = getStringFromData(data);

        logger.debug("data format accepted - begin parsing");

        parseAsLinks(strData);

        logger.debug("parsing finished, parsed " + importedEdges.size() + " edges");
    }

    private void parseAsLinks(String strData) {
        String splitExpr  = "\n";
        if(strData.contains("\r\n")){
            splitExpr = "\r\n";
        }

        List<String> nodeDict = new ArrayList<>();
        for(String line : strData.split(splitExpr)){
            if(line.contains("\t")){
                String [] parts = line.split("\t");
                if(parts.length >= 2){                              //ignore lines with not enough data
                    if(!parts[0].contains(" ") && !parts[1].contains(" ")){
                        //no whitespaces allowed in names
                        String [] fromParts = parts[0].split(",");
                        String [] toParts = parts[1].split(",");

                        for(String fPart : fromParts){
                            for(String tPart : toParts){
                                importedEdges.add(new CoriaEdge("", fPart, tPart));
                                if(!nodeDict.contains(fPart)) {
                                    importedNodes.add(new CoriaNode(fPart));
                                    nodeDict.add(fPart);
                                }
                                if(!nodeDict.contains(tPart)) {
                                    importedNodes.add(new CoriaNode(tPart));
                                    nodeDict.add(tPart);
                                }
                            }
                        }
                    }else{
                        logger.trace("ignoring line because of invalid whitespace [in name|prefix]: " + line);
                    }
                }else{
                    logger.trace("ignoring line because it contains not enough values: " + line);
                }
            }else{
                logger.trace("ignoring line because it is not separeted by tab: " + line);
            }
        }
    }

    private String getStringFromData(Object data) throws FormatNotSupportedException {
        String strData;
        if(data instanceof byte[]){
            //we got an byte array -> convert to string before parsing
            strData = new String(((byte[])data), Charset.defaultCharset());
        }else if(data instanceof String){
            //data is in string format -> no need to convert
            strData = (String)data;
        }else{
            //the given format is not supported -> exception
            logger.error("The provided format is not supported, please provide data in String od byte[] format!");
            throw new FormatNotSupportedException("The provided format is not supported, please provide data in String or byte[] format!");
        }
        return strData;
    }

    @Override
    public String toString() {
        return "StandardTabImporter{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
