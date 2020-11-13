package com.coria.v3.metrics;

import java.util.List;

/**
 * Created by David Fradin, 2020
 */
public interface MetricAlgorithmImplementationFactory {
    List<? extends MetricAlgorithmImplementation> getList() throws Exception;
}
