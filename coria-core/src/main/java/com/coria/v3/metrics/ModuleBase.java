package com.coria.v3.metrics;

/**
 * Created by Sebastian Gross, 2017 (coria-core/src/main/java/com/bigbasti/coria/parser/ImportModule.java)
 * Modified by David Fradin, 2020:
 * - Created a common interface for ExportModule, ImportModule, MetricAlgorithmImplementation and CudaDeviceInfo classes.
 */
public interface ModuleBase {
    /**
     * Defines an internal unique id for this module
     *
     * @return id of the parser
     */
    String getId();

    /**
     * User-friendly name to be displayed in the frontend.
     *
     * @return name of parser
     */
    String getName();

    /**
     * User manual and technical reference in HTML format to be displayed in the frontend.
     *
     * @return HTML Description of the parser
     */
    String getDescription();
}
