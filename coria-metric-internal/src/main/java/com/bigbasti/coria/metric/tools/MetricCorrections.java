package com.bigbasti.coria.metric.tools;

import com.bigbasti.coria.dataset.DataSet;
import com.bigbasti.coria.graph.CoriaNode;
import com.bigbasti.coria.metric.gs.GSHelper;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

/**
 * Class static methods for correction of statistic values
 */
public class MetricCorrections {
    private static Logger logger = LoggerFactory.getLogger(MetricCorrections.class);

    public static DataSet correctClusteringCoefficients(DataSet dataset){
        for(CoriaNode node : dataset.getNodes()){
            double cc = Double.parseDouble(node.getAttribute("clco"));
            double degree = Double.parseDouble(node.getAttribute("ndeg"));

            double correctedCc = cc + (degree * cc) / 4;
            logger.trace("Corrected CC: {}->{}", cc, correctedCc);
            node.setAttribute("clco_corrected", String.valueOf(correctedCc));
        }
        return dataset;
    }

    public static DataSet correctAverageNeighbourDegree(DataSet dataset){
        logger.debug("starting correction of average neighbour degree");
        Instant starts = Instant.now();

        Graph g = GSHelper.createGraphFromDataSet(dataset);

        for(CoriaNode node : dataset.getNodes()){
            int neighboursCount = Integer.parseInt(node.getAttribute("ndeg"));
            double avgNdg = Double.parseDouble(node.getAttribute("and"));

            //prepare array for median computation
            DescriptiveStatistics stat = new DescriptiveStatistics();
            Iterator<Node> iterator = g.getNode(node.getAsid()).getNeighborNodeIterator();
            while(iterator.hasNext()){
                Node gsNode = iterator.next();
                double ccVal = gsNode.getDegree();
                stat.addValue(ccVal);
            }
            double median = getMedian(stat.getSortedValues());
            double standardDeviation = stat.getStandardDeviation();

            if(avgNdg == 0 || neighboursCount == 0 || standardDeviation == 0){
                node.setAttribute("and_corrected", String.valueOf(avgNdg));
            }else{
                double corrected = avgNdg + (((median - avgNdg) / standardDeviation) / neighboursCount) * avgNdg;
                node.setAttribute("and_corrected", String.valueOf(corrected));
            }
            logger.trace("Corrected AND: {}->{}", avgNdg, node.getAttribute("and_corrected"));
        }

        Instant ends = Instant.now();
        logger.debug("finished correction of average neighbour degree ({})", Duration.between(starts, ends));
        return dataset;
    }

    public static DataSet correctIteratedAverageNeighbourDegree(DataSet dataset){
        logger.debug("starting correction of iterated average neighbour degree");
        Instant starts = Instant.now();

        Graph g = GSHelper.createGraphFromDataSet(dataset);

        for(CoriaNode node : dataset.getNodes()){
            double avgINdg = Double.parseDouble(node.getAttribute("iand"));

            int nodesCount = 0;
            DescriptiveStatistics stat = new DescriptiveStatistics();
            Iterator<Node> neighborNodes = g.getNode(node.getAsid()).getNeighborNodeIterator();
            while(neighborNodes.hasNext()){
                Node firstLevelNode = neighborNodes.next();
                Iterator<Node> secodLevelNodes = firstLevelNode.getNeighborNodeIterator();
                while(secodLevelNodes.hasNext()){
                    Node secondLevelNode = secodLevelNodes.next();
                    stat.addValue(secondLevelNode.getDegree());
                    nodesCount++;
                }
            }

            double median = getMedian(stat.getSortedValues());
            double standardDeviation = stat.getStandardDeviation();

            if(avgINdg == 0 || nodesCount == 0 || standardDeviation == 0){
                node.setAttribute("iand_corrected", String.valueOf(avgINdg));
            }else{
                double corrected = avgINdg + (((median - avgINdg) / standardDeviation) / nodesCount) * avgINdg;
                node.setAttribute("iand_corrected", String.valueOf(corrected));
            }
            logger.trace("Corrected IAND: {}->{}", avgINdg, node.getAttribute("iand_corrected"));
        }

        Instant ends = Instant.now();
        logger.debug("finished correction of iterated average neighbour degree ({})", Duration.between(starts, ends));
        return dataset;
    }

    private static double getMedian(double [] numArray){
        double median;
        if (numArray.length % 2 == 0) {
            median = ((double) numArray[numArray.length / 2] + (double) numArray[numArray.length / 2 - 1]) / 2;
        } else {
            median = (double) numArray[numArray.length / 2];
        }
        return median;
    }
}
