package com.bigbasti.coria.metric.tools;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;

public class MetricNormalizations {
    private static Logger logger = LoggerFactory.getLogger(MetricNormalizations.class);

    public static DataSet normalizeMinMax(DataSet dataSet, String metric){
        logger.debug("starting normalzation of {}", metric);
        Instant starts = Instant.now();

        double xMin = Double.MAX_VALUE;
        double xMax = Double.MIN_VALUE;

        for(CoriaNode node : dataSet.getNodes()){
            double metricVal = Double.parseDouble(node.getAttribute(metric));
            if(metricVal > xMax){xMax = metricVal;}
            if(metricVal < xMin){xMin = metricVal;}
        }

        logger.debug("Normalization min:{} max:{}", xMin, xMax);

        for(CoriaNode node : dataSet.getNodes()){
            double normalized = 0;
            if(xMin == xMax){
                normalized = 1.0;
            }else{
                double metricVal = Double.parseDouble(node.getAttribute(metric));
                normalized = (metricVal - xMin) / (xMax - xMin);
            }
            node.setAttribute(metric + "_normalized", String.valueOf(normalized));
            logger.debug("Normalized {}->{}", node.getAttribute(metric), normalized);
        }

        Instant ends = Instant.now();
        logger.debug("finished normalization of {} ({})", metric, Duration.between(starts, ends));
        return dataSet;
    }
}
