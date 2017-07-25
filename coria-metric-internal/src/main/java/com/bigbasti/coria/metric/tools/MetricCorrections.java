package com.bigbasti.coria.metric.tools;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class MetricCorrections {
    private static Logger logger = LoggerFactory.getLogger(MetricCorrections.class);

    public static DataSet correctClusteringCoefficients(DataSet dataset){
        for(CoriaNode node : dataset.getNodes()){
            double cc = Double.parseDouble(node.getAttribute("clco"));
            double degree = Double.parseDouble(node.getAttribute("ndeg"));

            double correctedCc = cc + (degree * cc) / 4;
            logger.debug("Corrected CC: {}->{}", cc, correctedCc);
            node.setAttribute("clco_corrected", String.valueOf(correctedCc));
        }
        return dataset;
    }

    public static DataSet correctAverageNeighbourDegree(DataSet dataset){

        return null;
    }

    public static DataSet correctIteratedAverageNeighbourDegree(DataSet dataset){

        return null;
    }

//    private double getMedian(List<Double> values){
//        Arrays.sort(numArray);
//        double median;
//        if (numArray.length % 2 == 0) {
//            median = ((double) numArray[numArray.length / 2] + (double) numArray[numArray.length / 2 - 1]) / 2;
//        } else {
//            median = (double) numArray[numArray.length / 2];
//        }
//    }
}
