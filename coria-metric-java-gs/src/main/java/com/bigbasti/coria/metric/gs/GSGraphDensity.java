package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.metrics.MetricInfo;
import com.bigbasti.coria.metrics.MetricModule;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Sebastian Gross
 */
@Component
public class GSGraphDensity implements MetricModule {
    private Logger logger = LoggerFactory.getLogger(GSGraphDensity.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-graph-density";
    }

    @Override
    public String getDescription() {
        return "The density is the number of links in the graph divided by the total number of possible links.";
    }

    @Override
    public String getName() {
        return "Graph Density";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "gden";
    }

    @Override
    public String getProvider() {
        return "GraphStream";
    }

    @Override
    public MetricInfo.MetricType getType() {
        return MetricInfo.MetricType.DATASET;
    }

    @Override
    public DataSet performCalculations(DataSet dataset) {
        logger.debug("calculating {} for dataset {}", getName(), dataset.getId());
        try {
            logger.debug("trying to create a temp graph with provided data...");
            Graph g = GSHelper.createGraphFromDataSet(dataset);
            logger.debug("successful finished graph creation");

            logger.debug("starting metric computation...");
            double andeg = Toolkit.density(g);
            dataset.setAttribute(getShortcut(), String.valueOf(andeg));
            logger.debug("finished metric computation - result: {}", String.valueOf(andeg));

            return dataset;
        }catch(Exception ex){
            logger.error("Error while executing calculation: {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "GSGraphDensity{id: " + getIdentification() + ", name: " + getName() + "}";
    }
}
