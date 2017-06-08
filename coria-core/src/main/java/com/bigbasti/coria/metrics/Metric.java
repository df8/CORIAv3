package com.bigbasti.coria.metrics;

import com.bigbasti.coria.dataset.DataSet;

/**
 * Created by Sebastian on 01.06.2017.
 */
public interface Metric {
    String getIdentification();
    String getDescription();
    String getName();
    String getTechnology();
    String getShortcut();
    String getProvider();
    MetricInfo.MetricType getType();

    DataSet performCalculations(DataSet dataset);
}
