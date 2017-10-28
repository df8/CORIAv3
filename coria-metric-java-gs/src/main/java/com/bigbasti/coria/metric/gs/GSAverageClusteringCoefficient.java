package com.bigbasti.coria.metric.gs;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.metrics.MetricModule;
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
public class GSAverageClusteringCoefficient implements MetricModule{
    private Logger logger = LoggerFactory.getLogger(GSAverageClusteringCoefficient.class);

    @Override
    public String getIdentification() {
        return "java-graphstream-average-clustering-coefficient";
    }

    @Override
    public String getDescription() {
        return "calculates the average clustering coefficient for the graph";
    }

    @Override
    public String getName() {
        return "Average Clustering Coefficient";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "aclco";
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
            double acc = Toolkit.averageClusteringCoefficient(g);
            dataset.setAttribute(getShortcut(), String.valueOf(acc));
            logger.debug("finished metric computation - result: {}", String.valueOf(acc));

            return dataset;
        }catch(Exception ex){
            logger.error("Error while executing calculation: {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "GSAverageClusteringCoefficient{id: " + getIdentification() + ", name: " + getName() + "}";
    }
}
