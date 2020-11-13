package com.coria.v3.metrics;

import java.util.List;

/**
 * Created by David Fradin, 2020
 */
public interface MetricMultiAlgorithmImplementationFactory {
    List<? extends MetricMultiAlgorithmImplementation> getList();
}
