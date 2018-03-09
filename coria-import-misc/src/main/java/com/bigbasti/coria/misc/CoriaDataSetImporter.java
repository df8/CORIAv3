package com.bigbasti.coria.misc;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.parser.FormatNotSupportedException;
import com.bigbasti.coria.parser.ImportModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

@Component
public class CoriaDataSetImporter implements ImportModule {

    private Logger logger = LoggerFactory.getLogger(CoriaDataSetImporter.class);

    List<CoriaEdge> importedEdges = new ArrayList<>();
    List<CoriaNode> importedNodes = new ArrayList<>();
    DataSet dataset = new DataSet();

    @Override
    public String getIdentification() {
        return "coria-dataset-xml-json-importer";
    }

    @Override
    public String getName() {
        return "CORIA Full DataSet Importer";
    }

    @Override
    public String getDescription() {
        return "<p>This Importer can be used to import a DataSet, which was previously exported by the <code>CORIA DataSet Export Adapter</code></p>\n" +
                "<p>The content of the file must be either in <code>XML</code> or <code>JSON</code> format.</p>" +
                "<p><strong>NOTE:</strong> Even if the DatSet file contains a name for the Set, it will be overwritten with the name you specify here in the Name textfield!</p>";
    }

    @Override
    public String getExpectedFormat() {
        return "Uncompressed textfile";
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

    @Override
    public DataSet getDataSet() {
        return dataset;
    }

    @Override
    public ImportType getImportType() {
        return ImportType.DATASET;
    }

    public void parseInformation(Object data, Map<String, Object> params) throws FormatNotSupportedException {
        logger.debug("starting import of data");

        cleanSession();

        String strData = getStringFromData(data);

        logger.debug("data format accepted - begin parsing");

        DataSet newSet;
        if(strData.startsWith("<")) {
            logger.debug("parsing file as XML");
            try {
                newSet = new XmlMapper().readValue(strData, DataSet.class);
            } catch (IOException e) {
                logger.error("error while deserializing xml: {}", e.getMessage());
                e.printStackTrace();
                throw new FormatNotSupportedException(e.getMessage());
            }
        }else{
            logger.debug("parsing file as JSON");
            if(strData.startsWith("\"")){
                //remove leading and tailing "
                strData = strData.substring(1, strData.length()-1);
                strData = strData.replaceAll("\\\\", "");
            }
            newSet = new Gson().fromJson(strData, DataSet.class);
        }
        dataset = newSet;
        importedEdges = dataset.getEdges();
        importedNodes = dataset.getNodes();

        if(dataset.getAttributes() == null){
            dataset.setAttributes(new TreeMap<>());
        }
        if(dataset.getNotificationEmails() == null){
            dataset.setNotificationEmails(new ArrayList<>());
        }

        logger.debug("parsing finished, parsed " + importedEdges.size() + " edges");
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

    private void cleanSession(){
        this.importedEdges = new ArrayList<>();
        this.importedNodes = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "CoriaDataSetImporter{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
