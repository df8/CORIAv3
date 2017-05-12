package com.bigbasti.coria.aslinks;

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

/**
 * Parser for ASLink Files provided by CAIDA
 * Created by Sebastian Gross
 */
@Component
public class ASLinksEdgeImporter implements InputParser {

    private Logger logger = LoggerFactory.getLogger(ASLinksEdgeImporter.class);

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
        return "Parses the AS-Link files which can be downloaded from CAIDA here:";
    }

    @Override
    public String getExpectedFormat() {
        return "Uncompressed Tab-Separated textfile";
    }

    public Object getParsedObjects(Object data) throws FormatNotSupportedException {
        ArrayList<CoriaEdge> importedEdges = new ArrayList<>();

        logger.debug("starting import of data");

        String strData = "";
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

        logger.debug("data format accepted - begin parsing");

        for(String line : strData.split("\n")){
            if(!line.startsWith("#")){                                  //ignore lines which comments
                if(line.startsWith("I") || line.startsWith("D")){       //only use direct and indirect links
                    String [] parts = line.split("\t");
                    if(parts.length >= 3){                              //ignore lines with not enough data
                        if(parts[0].length() == 1 && !parts[1].contains(" ") && !parts[2].contains(" ")){
                            //no whitespaces allowed in names
                            importedEdges.add(new CoriaEdge("", new CoriaNode(parts[1]), new CoriaNode(parts[2])));
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

        logger.debug("parsing finished, parsed " + importedEdges.size() + " edges");

        return importedEdges;
    }
}