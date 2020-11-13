package com.coria.v3.metrics.python;

import com.coria.v3.interop.FSTools;
import com.coria.v3.metrics.MetricAlgorithmImplementation;
import com.coria.v3.metrics.MetricAlgorithmImplementationFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by David Fradin, 2020
 */
@Component
public class PythonMetricAlgorithmImplementationFactory implements MetricAlgorithmImplementationFactory {
    protected final static Logger logger = LoggerFactory.getLogger(PythonMetricAlgorithmImplementationFactory.class);
    @Value("classpath:metric-config/python-metrics-configuration.xml")
    private Resource pythonMetricsConfigurationFile;

    static FSTools fsTools;

    public static FSTools getFSTools() {
        return fsTools;
    }

    @Autowired
    protected void setFsTools(FSTools fsTools) {
        PythonMetricAlgorithmImplementationFactory.fsTools = fsTools;
    }


    @Override
    public List<? extends MetricAlgorithmImplementation> getList() {
        try {
            logger.debug("Python script path: " + PythonMetricAlgorithmImplementation.getScriptPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<MetricAlgorithmImplementation> result = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(pythonMetricsConfigurationFile.getInputStream(), UTF_8)) {
            String strData = FileCopyUtils.copyToString(isr);
            XmlMapper xmlMapper = new XmlMapper();
            ImportedMetricAlgorithmImplementationList importedMetricAlgorithmImplementationList = xmlMapper.readValue(strData, ImportedMetricAlgorithmImplementationList.class);

            return importedMetricAlgorithmImplementationList.metricAlgorithmImplementations;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Helper class to import the python-metrics-configuration.xml file correctly
     */
    @JacksonXmlRootElement(localName = "metric-modules")
    protected static class ImportedMetricAlgorithmImplementationList {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "metric-module")
        public List<PythonMetricAlgorithmImplementation> metricAlgorithmImplementations;
    }
}
