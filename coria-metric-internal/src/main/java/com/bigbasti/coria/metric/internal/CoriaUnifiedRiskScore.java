package com.bigbasti.coria.metric.internal;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metric.gs.GSHelper;
import com.bigbasti.coria.metric.tools.MetricCorrections;
import com.bigbasti.coria.metric.tools.MetricNormalizations;
import com.bigbasti.coria.metrics.Metric;
import com.bigbasti.coria.metrics.MetricInfo;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Sebastian Gross
 */
@Component
public class CoriaUnifiedRiskScore implements Metric{
    private Logger logger = LoggerFactory.getLogger(CoriaUnifiedRiskScore.class);

    @Override
    public String getIdentification() {
        return "java-coria-unified-risk-score";
    }

    @Override
    public String getDescription() {
        return "<p>1. Executes correction of <code>Clustering Coefficients, Average Neighbour Degree</code> and <code>Iterated Average Neighbour Degree</code> algorithms</p>" +
                "<p>2. Executes normalization of all metrics listed below</p>" +
                "<p>3. Calculates Risk Score based on the following weighting: <br/>" +
                "<ul><li><code>Node Degree</code>: 0.25</li>" +
                "<li><code>Average Neighbour Degree</code>: 0.15</li>" +
                "<li><code>Iterated Neighbour Degree</code>: 0.1</li>" +
                "<li><code>Betweenness Centrality</code>: 0.25</li>" +
                "<li><code>Average Shortest Path Length</code>: 0.25</li></ul>" +
                "<strong>NOTE:</strong> All the metrics above must be executed before this metric!</p>" +
                "<p>This Metric is based on the Master Thesis 'Internet Resilience and Connectivity Risks for Online Businesses' by Annika Baumann</p>";
    }

    @Override
    public String getName() {
        return "Unified Risk Score";
    }

    @Override
    public String getTechnology() {
        return "Java";
    }

    @Override
    public String getShortcut() {
        return "urs";
    }

    @Override
    public String getProvider() {
        return "CORIA";
    }

    @Override
    public MetricInfo.MetricType getType() {
        return MetricInfo.MetricType.NODE;
    }

    @Override
    public DataSet performCalculations(DataSet dataset) {
        logger.debug("calculating {} for dataset {}", getName(), dataset.getId());
        Map<String, Double> requiredMetrics = new HashMap<>();
        requiredMetrics.put("ndeg", 0.25);
        requiredMetrics.put("and", 0.15);
        requiredMetrics.put("iand", 0.1);
        requiredMetrics.put("bc", 0.25);
        requiredMetrics.put("aspl", 0.25);
        try {
            logger.debug("checking if all preconditions are met");
            //select first node to check if it has all the needed metrics
            //it's safe to say that if the first node has them, they all have them
            CoriaNode testNode = dataset.getNodes().get(0);
            boolean fail = false;
            for(String metric : requiredMetrics.keySet()) {
                if (testNode.getAttribute(metric) == null) {
                    fail = true;
                }
            }
            if(fail){
                logger.error("not all preconditions for risk calculation are met!");
                throw new RuntimeException("Not all required metrics were executed. Please execute the metrics described in the Unified Risk Score description first.");
            }

            logger.debug("executing correction of values");
            dataset = MetricCorrections.correctAverageNeighbourDegree(dataset);
            dataset = MetricCorrections.correctClusteringCoefficients(dataset);
            dataset = MetricCorrections.correctIteratedAverageNeighbourDegree(dataset);

            logger.debug("executing normalization of metrics");
            for(String metric : requiredMetrics.keySet()){
                dataset = MetricNormalizations.normalizeMinMax(dataset, metric);
            }

            logger.debug("calculating score for each node");

            for(CoriaNode node : dataset.getNodes()){
                double urc = 0;
                for(String metric : requiredMetrics.keySet()){
                    urc += Double.parseDouble(node.getAttribute(metric + "_normalized")) * requiredMetrics.get(metric);
                }
                node.setRiscScore(String.valueOf(urc));
                node.setAttribute(getShortcut(), String.valueOf(urc));
            }

            logger.debug("calculating relative metric values");


            logger.debug("finished metric computation");

            return dataset;
        }catch(Exception ex){
            logger.error("Error while executing calculation: {}", ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public String toString() {
        return "CoriaUnifiedRiskScore{id: " + getIdentification() + ", name: " + getName() + "}";
    }
}
