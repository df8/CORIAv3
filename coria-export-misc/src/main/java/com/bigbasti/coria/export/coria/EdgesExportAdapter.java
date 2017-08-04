package com.bigbasti.coria.export.coria;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.export.ExportAdapter;
import com.bigbasti.coria.export.ExportResult;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metrics.MetricInfo;
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
public class EdgesExportAdapter implements ExportAdapter {
    private Logger logger = LoggerFactory.getLogger(EdgesExportAdapter.class);

    @Override
    public String getIdentification() {
        return "raw-edges-export-adapter";
    }

    @Override
    public String getName() {
        return "Raw Edges Export Adapter";
    }

    @Override
    public String getDescription() {
        return "<p><strong>This export adapter exports all edges from a dataset identified by their AS-ID</strong></p>" +
                "<p>The exported data will be in <code>CSV</code> format. You can specify which separator character to use in the <code>separator</code> text field on the left</p>" +
                "<p><strong>NOTE:</strong> If no separator is specified a <code>,</code> (comma) will be used as a fallback!</p>" +
                "<p><strong>NOTE:</strong> No CSV Header / Title row will be created</p>" +
                "<p><strong>NOTE:</strong> If you wish to use <code>tab</code> as separator, use <code>[tab]</code> as seperator value</p>";
    }

    @Override
    public Map<String, String> getAdditionalFields() {
        HashMap<String, String> fields = new HashMap<>();
        fields.put("separator", "text");
        return fields;
    }

    /**
     * Converts the given dataset to a CSV with [separator] as separatir char
     * If no separator (or invalid format) is provided, a comma is used as fallback
     * @param dataset the dataset to be exportet
     * @param params additional params for the export adapter
     * @return String with the desired format
     */
    @Override
    public ExportResult exportDataSet(DataSet dataset, Map<String, Object> params) {
        ExportResult result = new ExportResult();
        logger.debug("starting export of dataset");

        String separator = ",";
        if(params != null && params.size() > 0){
            if(params.containsKey("separator")){
                separator = (String)params.get("separator");
                if(separator.equals("[tab]")){
                    separator = "\t";
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        logger.debug("using separator: {}", separator);
        for(CoriaEdge edge : dataset.getEdges()){
            builder.append(edge.getSourceNode()).append(separator).append(edge.getDestinationNode()).append(System.lineSeparator());
        }

        String output = builder.toString();
        logger.debug("finished export successfully length:{}", output.length());

        result.setContentType("text/csv;charset=utf-8;");
        result.setExportResult(output);
        result.setFileName(dataset.getName().replace(" ", "_")+".csv");
        return result;
    }

    @Override
    public String toString() {
        return "EdgesExportAdapter{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
