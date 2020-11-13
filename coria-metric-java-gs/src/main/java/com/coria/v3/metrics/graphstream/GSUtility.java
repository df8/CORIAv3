package com.coria.v3.metrics.graphstream;

import com.coria.v3.dbmodel.NodeEntity;
import com.coria.v3.dbmodel.NodeMetricResultEntity;
import com.coria.v3.repository.RepositoryManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by David Fradin, 2020
 */
public class GSUtility {
    /**
     * TODO description
     *
     * @param repositoryManager
     * @param metricId
     * @return
     */
    public static Map<NodeEntity, Double> getNodeMetricResultsMap(RepositoryManager repositoryManager, UUID metricId) throws Exception {
        var nodeDegreeMap = repositoryManager
                .getNodeMetricResultRepository()
                .findAllByMetric_Id(metricId)
                .stream()
                .collect(Collectors.toMap(NodeMetricResultEntity::getNode, NodeMetricResultEntity::getValue));
        if (nodeDegreeMap.size() == 0) {
            throw new Exception("node-degree--default is not available.");
        }
        return nodeDegreeMap;
    }

    /**
     * Calculates the median of an ArrayList in average O(n).
     * The functions median() and nth() in this file were taken from:
     * https://stackoverflow.com/questions/11955728/how-to-calculate-the-median-of-an-array
     * See answer https://stackoverflow.com/a/28822243/4520273
     *
     * @param coll an ArrayList of Comparable objects
     * @return the median of coll
     *****************/
    public static <T extends Number> double median(ArrayList<T> coll, Comparator<T> comp) {
        double result;
        int n = coll.size() / 2;

        if (coll.size() % 2 == 0)  // even number of items; find the middle two and average them
            result = (nth(coll, n - 1, comp).doubleValue() + nth(coll, n, comp).doubleValue()) / 2.0;
        else                      // odd number of items; return the one in the middle
            result = nth(coll, n, comp).doubleValue();

        return result;
    } // median(coll)


    /*****************
     * @param coll a collection of Comparable objects
     * @param n  the position of the desired object, using the ordering defined on the list elements
     * @return the nth smallest object
     *******************/

    public static <T> T nth(ArrayList<T> coll, int n, Comparator<T> comp) {
        T result, pivot;
        ArrayList<T> underPivot = new ArrayList<>(), overPivot = new ArrayList<>(), equalPivot = new ArrayList<>();

        // choosing a pivot is a whole topic in itself.
        // this implementation uses the simple strategy of grabbing something from the middle of the ArrayList.

        pivot = coll.get(n / 2);

        // split coll into 3 lists based on comparison with the pivot

        for (T obj : coll) {
            int order = comp.compare(obj, pivot);

            if (order < 0)        // obj < pivot
                underPivot.add(obj);
            else if (order > 0)   // obj > pivot
                overPivot.add(obj);
            else                  // obj = pivot
                equalPivot.add(obj);
        } // for each obj in coll

        // recurse on the appropriate list

        if (n < underPivot.size())
            result = nth(underPivot, n, comp);
        else if (n < underPivot.size() + equalPivot.size()) // equal to pivot; just return it
            result = pivot;
        else  // everything in underPivot and equalPivot is too small.  Adjust n accordingly in the recursion.
            result = nth(overPivot, n - underPivot.size() - equalPivot.size(), comp);

        return result;
    } // nth(coll, n)

}