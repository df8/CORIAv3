package com.coria.v3.export.coria;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.export.ExportModule;
import com.coria.v3.export.ExportModuleParameter;
import com.coria.v3.export.ExportResult;
import com.coria.v3.utility.Slugify;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sebastian Gross, 2017 (coria-export-misc/src/main/java/com/bigbasti/coria/export/coria/CoriaExportModule.java)
 * Modified by David Fradin, 2020:
 * - Adapted to JPA
 * - Changed description
 */

@Component
public class CoriaExportModule extends ExportModule {
    private final Logger logger = LoggerFactory.getLogger(CoriaExportModule.class);
    static final String[] supportedFileFormats = new String[]{"XML", "JSON"};

    public CoriaExportModule() {
        super("coria-xml-json-export-module",
                "CORIA XML/JSON Export Module",
                "<p>The <strong>CORIA XML/JSON Export Module</strong> is designed to export a whole dataset into a single <code>XML</code> or <code>JSON</code> file.</strong></p>" +
                        "<p>You can use this format as a means for backup, to transfer the dataset to a different instance of CORIA or to process dataset in custom external scripts.</p>" +
                        "<p>Among the exported data will be</p><ul>" +
                        "<li>Dataset name</li>" +
                        "<li>Nodes including node attributes and references to AS organisations</li>" +
                        "<li>Edges including edge attributes and references to AS locations</li>" +
                        "<li>Computed metrics including all metric results</li>" +
                        "</ul>",
                new ExportModuleParameter[]{new ExportModuleParameter("File Format", supportedFileFormats)});
    }

    /**
     * Converts the given dataset to XML or JSON depending on additional parameter "format"
     * If no format (or invalid format) is provided, JSON is used as fallback
     *
     * @param dataset the dataset to be exported
     * @param params  additional params for the export adapter
     * @return String with the desired format
     */
    @Override
    public ExportResult exportDataset(DatasetEntity dataset, Map<String, String> params) {
        ExportResult result = new ExportResult();
        logger.debug("starting export of dataset");

        if (params == null)
            params = new HashMap<>();
        String format = params.getOrDefault("file-format", "JSON");
        if (!Arrays.asList(supportedFileFormats).contains(format))
            format = "JSON";
        logger.debug("using format: {}", format);

        result.setFileName("coria-export-" + Slugify.toSlug(dataset.getName()) + "." + format);
        String output = "";
        try {
            switch (format) {
                case "JSON":
                    result.setContentType("application/json;charset=utf-8;");
                    output = new ObjectMapper().writeValueAsString(dataset);
                    result.setExportResult(output);
                    break;
                case "XML":
                    result.setContentType("application/xml;charset=utf-8;");
                    XmlMapper xmlMapper = new XmlMapper();
                    output = xmlMapper.writeValueAsString(dataset);
                    result.setExportResult(output);
                    break;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }

        logger.debug("finished export successfully length:{}", output.length());

        return result;
    }

    @Override
    public String toString() {
        return "CoriaExportAdapter{" +
                "id: " + getId() +
                ", name: " + getName() +
                "}";
    }
}
