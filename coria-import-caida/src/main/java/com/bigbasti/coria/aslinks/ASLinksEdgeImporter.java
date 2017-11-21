package com.bigbasti.coria.aslinks;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.parser.ImportModule;
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
public class ASLinksEdgeImporter implements ImportModule {

    private Logger logger = LoggerFactory.getLogger(ASLinksEdgeImporter.class);

    ArrayList<CoriaEdge> importedEdges = new ArrayList<>();
    ArrayList<CoriaNode> importedNodes = new ArrayList<>();

    @Override
    public String getIdentification() {
        return "caida-as-links-parser";
    }

    @Override
    public String getName() {
        return "CAIDA AS-Links Parser";
    }

    @Override
    public String getDescription() {
        return "<p>All uploaded files must match the format described below</p>\n" +
                "<p>Each line represents one link, lines starting with <code>#</code> are ignored! Only lines are processed which begin with <code>I</code> or <code>D</code></p>\n" +
                "<p>One example line could look like this: <code>I\t10010\t23775\t2\t9\t12\t15\t28\t29</code> where the parameters are separated by <code>tab</code> characters and represent the following information (in the same order) <code>I   from_AS   to_AS   gap_length   monitor_key1   monitor_key2 ...</code></p>\n" +
                "<p>compatible files can be obtained from caida.org - for example here: <a href=\"http://data.caida.org/datasets/topology/ark/ipv4/as-links/team-1/2017/\" target=\"blank\">http://data.caida.org/datasets/topology/ark/ipv4/as-links/team-1/2017/</a></p>\n" +
                "<p>The expected file format is <code>.txt</code> - so please extract the <code>.gz</code> file from caida.org before uploading</p>";
    }

    @Override
    public String getExpectedFormat() {
        return "Uncompressed Tab-Separated textfile";
    }

    @Override
    public Map<String, String> getAdditionalFields() {
        return new HashMap<>();
    }

    public void parseInformation(Object data, Map<String, Object> params) throws FormatNotSupportedException {
        logger.debug("starting import of data");

        String strData = getStringFromData(data);

        logger.debug("data format accepted - begin parsing");

        this.importedNodes = new ArrayList<>();
        this.importedEdges = new ArrayList<>();
        parseAsLinks(strData);

        logger.debug("parsing finished, parsed " + importedEdges.size() + " edges");
    }

    private void parseAsLinks(String strData) {
        List<String> nodeDict = new ArrayList<>();
        for(String line : strData.split("\n")){
            if(line.contains("\r")){line = line.replaceAll("\r","");}
            if(!line.startsWith("#")){                                  //ignore lines which comments
                if(line.startsWith("I") || line.startsWith("D")){       //only use direct and indirect links
                    String [] parts = line.split("\t");
                    if(parts.length >= 3){                              //ignore lines with not enough data
                        if(parts[0].length() == 1 && !parts[1].contains(" ") && !parts[2].contains(" ")){
                            //no whitespaces allowed in names
                            String [] fromParts = parts[1].split(",");
                            String [] toParts = parts[2].split(",");

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
                    logger.trace("ignoring line because of invalid prefix: " + line);
                }
            }else{
                logger.trace("ignoring line because it is a comment: " + line);
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
    public List<CoriaEdge> getParsedEdges() {
        return importedEdges;
    }

    @Override
    public List<CoriaNode> getParsedNodes() {
        return importedNodes;
    }

    @Override
    public DataSet getDataSet() {
        return null;
    }

    @Override
    public ImportType getImportType() {
        return ImportType.NODES_AND_EDGES;
    }

    @Override
    public String toString() {
        return "ASLinksEdgeImporter{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
