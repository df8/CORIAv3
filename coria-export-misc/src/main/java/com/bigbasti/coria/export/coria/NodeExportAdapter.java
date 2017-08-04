package com.bigbasti.coria.export.coria;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.export.ExportAdapter;
import com.bigbasti.coria.export.ExportResult;
import com.bigbasti.coria.graph.CoriaEdge;
import com.bigbasti.coria.graph.CoriaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NodeExportAdapter implements ExportAdapter {
    private Logger logger = LoggerFactory.getLogger(NodeExportAdapter.class);

    @Override
    public String getIdentification() {
        return "nodes-export-adapter";
    }

    @Override
    public String getName() {
        return "Nodes Export Adapter";
    }

    @Override
    public String getDescription() {
        return "<p><strong>This export adapter exports all nodes from a dataset identified by their AS-ID</strong></p>" +
                "<p>The exported data will be in <code>CSV</code> format. You can specify which separator character to use in the <code>separator</code> text field on the left</p>" +
                "<p>You can specify different fields (which should be included in the exported CSV) and different order in the input field <code>layout</code> on the left</p>" +
                "<p>The fallback/default output of the export adapter will be this: <code>ASID, NAME, RiskScore</code></p>" +
                "<p><strong>NOTE:</strong> If no separator is specified a <code>,</code> (comma) will be used as a fallback!</p>" +
                "<p><strong>NOTE:</strong> No CSV Header / Title row will be created</p>" +
                "<p><strong>NOTE:</strong> If you wish to use <code>tab</code> as separator, use <code>[tab]</code> as seperator value</p><hr/>" +
                "<p><strong>How-To specify layout</strong></p>" +
                "<p>Besides the default fields <code>asid, asname, riskscore</code> you can order any other Node-Attribute available on the dataset to be exported! Which attributes are available can be seen on the details view of the dataset (the metric shortcut represents the attribute). You can also use additional attributes like <code>geo_country, ndeg_relative, bc_corrected</code></p>" +
                "<p><strong>Example #1:</strong> <code>layout</code> is set to <code>asid, name, ndeg, bc, geo</code><br/></p>" +
                    "this will generate lines which could look like this: <code>17686,ACCELIA,7,2.4354322,true</code> (note that <code>geo</code> is the only attribute of boolean type)</p><hr/>" +
                "<p><strong>Example #2:</strong> <code>layout</code> is set to <code>asid, geo_city, geo_latitude, geo_longitude</code><br/></p>" +
                    "this will generate lines which could look like this: <code>17686,Tokyo,35.6895,139.69171</code></p><hr/>" +
                "<p><strong>Example #3:</strong> <code>layout</code> is set to <code>asid, iand, iand_relative, iand_normalized</code><br/></p>" +
                    "this will generate lines which could look like this: <code>17686,45,1.7655855,1.654</code></p><hr/>" +
                "<p><strong>Example #4:</strong> <code>layout</code> is set to <code>asid, geo_city, iand_relative, iand_normalizet</code><br/></p>" +
                    "this will generate lines which could look like this: <code>17686,,1.7655855,</code> <br/>Note the value for <code>geo_city</code> is missing, because this attribute is not available on that dataset<br/>Note the value for <code>iand_normalizet</code> is also missing because the attribute is spelled wrong (ends with t instead of d)</p><hr/>" +
                "<p><strong>Available attributes other than the main attributes of the metrics (like ndeg, bc, clco, etc...)</strong></p>" +
                "<p><ul><li>geo</li><ul><li>geo_city</li><li>geo_continent</li><li>geo_country</li><li>geo_latitude</li><li>geo_longitude</li></ul>" +
                "       <li>pos</li>" +
                "       <li>[every metric shortcut] (Example: <code>iand</code>)</li><ul><li>_relative (Example: <code>iand_relative</code>)</li><li>_corrected (Example: <code>iand_corrected</code>)</li><li>_normalized (Example: <code>iand_normalized</code>)</li></ul></ul></p>" +
                "<div class=\"alert alert-warning\" role=\"alert\">Keep in mind that not all attributes are always available on each dataset. Which attributes are available always depends on which metrics have been successfully executed!<br/>If the attribute you specified is not available it will not be printed in the output resulting an a not existing value (see example #4)<br/>Also, attributes that have typos in them an thus are not known to CORIA will also be handled like the value is not existent! (see example #4)</div></p>";
    }

    @Override
    public Map<String, String> getAdditionalFields() {
        HashMap<String, String> fields = new HashMap<>();
        fields.put("separator", "text");
        fields.put("layout", "text");
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

        String [] layoutParams = new String[]{"asid", "name", "riskscore"};
        if(params != null && params.size() > 0){
            if(params.containsKey("layout")){
                String strLayout = ((String)params.get("layout")).trim().replaceAll(" ", "").toLowerCase();
                layoutParams = strLayout.split(",");
            }
        }
        logger.debug("using layout: {}", layoutParams);
        logger.debug("using separator: {}", separator);

        StringBuilder builder = new StringBuilder();
        for(CoriaNode node : dataset.getNodes()){
            int counter = 0;
            for(String param : layoutParams){
                if(param.equals("asid")){
                    builder.append(node.getAsid());
                }else if(param.equals("name")){
                    builder.append(node.getName());
                }else if(param.equals("riskscore")){
                    builder.append(node.getRiscScore());
                }else{
                    String val = "";
                    for(String key : node.getAttributes().keySet()){
                        if(key.equals(param)){
                            val = node.getAttribute(key);
                        }
                    }
                    builder.append(val);
                }
                if(counter < layoutParams.length-1) {
                    builder.append(separator);
                }
                counter++;
            }
            builder.append(System.lineSeparator());
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
        return "NodesExportAdapter{" +
                "id: " + getIdentification() +
                ", name: " + getName() +
                "}";
    }
}
