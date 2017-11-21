package com.bigbasti.coria.export.coria;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.export.ExportModule;
import com.bigbasti.coria.export.ExportResult;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.MetricInfo;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
public class CoriaExportModule implements ExportModule {
    private Logger logger = LoggerFactory.getLogger(CoriaExportModule.class);

    @Override
    public String getIdentification() {
        return "coria-export-full-dataset-coria";
    }

    @Override
    public String getName() {
        return "CORIA DataSet Export Module";
    }

    @Override
    public String getDescription() {
        return "<p><strong>This export module exports a whole DataSet including its name and all meta information inside it</strong></p>" +
                "<p>The exportet data can be in <code>XML</code> or <code>JSON</code> format. You can choose the format in the input field named 'format' on the left. (If it is left blank or an unsupported value is entered JSON format will be used)</p>" +
                "<p>This format is designed to be used to transfer a dataset between different instances of CORIA</p>" +
                "<p><strong>NOTE:</strong> Although the data is extracted, the internal IDs of the objects will not be exported. They will be regenerated while importing the DataSet</p>";
    }

    @Override
    public Map<String, String> getAdditionalFields() {
        HashMap<String, String> fields = new HashMap<>();
        fields.put("format", "text");
        return fields;
    }

    /**
     * Converts the given dataset to XML or JSON depending on additional parameter "format"
     * If no format (or invalid format) is provided, JSON is used as fallback
     * @param dataset the dataset to be exportet
     * @param params additional params for the export adapter
     * @return String with the desired format
     */
    @Override
    public ExportResult exportDataSet(DataSet dataset, Map<String, Object> params) {
        ExportResult result = new ExportResult();
        logger.debug("starting export of dataset");

        SupportedFormats format = SupportedFormats.JSON;
        if(params != null && params.size() > 0){
            if(params.containsKey("format")){
                if(((String)params.get("format")).toLowerCase().equals("xml")){
                    format = SupportedFormats.XML;
                }
            }
        }

        logger.debug("export format is: {}", format.name());
        logger.debug("removing ids from dataset...");
        for(CoriaNode node : dataset.getNodes()){
            node.setId("");
        }
        for(CoriaEdge edge : dataset.getEdges()){
            edge.setId("");
        }
        for(MetricInfo info : dataset.getMetricInfos()){
            info.setId("");
        }
        dataset.setId("");


        String output = "";
        try {
            if (format == SupportedFormats.JSON) {
                result.setContentType("application/json;charset=utf-8;");
                GsonBuilder gsb = new GsonBuilder();
                gsb.disableHtmlEscaping();
                gsb.serializeNulls();
                gsb.serializeSpecialFloatingPointValues();
                Gson gson = gsb.create();
                output = gson.toJson(dataset);
                result.setExportResult(new String(output.getBytes("UTF8"), "UTF8"));
                result.setFileName(dataset.getName().replace(" ", "_")+".json");
            }
            if (format == SupportedFormats.XML) {
                result.setContentType("application/xml;charset=utf-8;");
                output = new XmlMapper().writeValueAsString(dataset);
                result.setExportResult(output);
                result.setFileName(dataset.getName().replace(" ", "_")+".xml");
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }

        logger.debug("finished export successfully length:{}", output.length());

        return result;
    }

    private String jaxbObjectToXML(DataSet object) throws JAXBException {
        String xmlString = "";
        try {
            JAXBContext context = JAXBContext.newInstance(DataSet.class);
            Marshaller m = context.createMarshaller();

            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To format XML

            StringWriter sw = new StringWriter();
            m.marshal(object, sw);
            xmlString = sw.toString();

        } catch (JAXBException e) {
            logger.error("error while creating xml for dataset: {}", e.getMessage());
            e.printStackTrace();
            throw e;
        }

        return xmlString;
    }

    @Override
    public String toString() {
        return "CoriaExportAdapter{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }

    enum SupportedFormats {
        XML,
        JSON
    }
}
