package com.bigbasti.coria.export.coria;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.export.ExportAdapter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
public class CoriaExportAdapter implements ExportAdapter {
    private Logger logger = LoggerFactory.getLogger(CoriaExportAdapter.class);

    @Override
    public String getIdentification() {
        return "coria-export-full-dataset-coria";
    }

    @Override
    public String getName() {
        return "CORIA DataSet Export Adapter";
    }

    @Override
    public String getDescription() {
        return "<p><strong>This export adapter exports a whole DataSet including its name and all meta information inside it</strong></p>" +
                "<p>The exportet data can be in <code>XML</code> or <code>JSON</code> format. You can choose the format in the input field named 'format' on the left. (If it is left blank or an unsupported value is entered JSON format will be used)</p>" +
                "<p>This format is designed to be used to transfer a dataset between different instances of CORIA</p>";
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
    public Object exportDataSet(DataSet dataset, Map<String, Object> params) {
        String output = "";
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

        try {
            if (format == SupportedFormats.JSON) {
                output = new GsonBuilder().create().toJson(dataset);
            }
            if (format == SupportedFormats.XML) {
                output = new XmlMapper().writeValueAsString(dataset);
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }

        logger.debug("finished export successfully length:{}", output.length());

        return output;
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
