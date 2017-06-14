package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Sebastian Gross
 */
@Component
public class GSAverageNodeDegree implements Metric{
    private Logger logger = LoggerFactory.getLogger(GSAverageNodeDegree.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-average-node-degree";
    }

    @Override
    public String getDescription() {
        return "Calculates the value of the average degree of the graph. A node with a loop edge has degree two.";
    }

    @Override
    public String getName() {
        return "Average Node Degree";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "andeg";
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
            double andeg = Toolkit.averageDegree(g);
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
        return "GSAverageNodeDegree{id: " + getIdentification() + ", name: " + getName() + "}";
    }
}
