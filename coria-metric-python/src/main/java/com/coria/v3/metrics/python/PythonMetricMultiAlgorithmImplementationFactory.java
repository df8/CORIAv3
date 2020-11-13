package com.coria.v3.metrics.python;

import com.coria.v3.metrics.MetricMultiAlgorithmImplementation;
import com.coria.v3.metrics.MetricMultiAlgorithmImplementationFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by David Fradin, 2020
 */
@Component
public class PythonMetricMultiAlgorithmImplementationFactory implements MetricMultiAlgorithmImplementationFactory {

    @Override
    public List<? extends MetricMultiAlgorithmImplementation> getList() {
        return List.of(
                new PythonMetricMultiAlgorithmImplementation("multi--python3-c-cuda--rapids-cugraph"),
                new PythonMetricMultiAlgorithmImplementation("multi--python3--networkx")
        );
    }
}
