package com.coria.v3.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type to filter a list of ImportModule entities based on a single ImportResource type (e.g. Dataset, ASLocation, or other).
 */
public class ImportModuleFilter extends ModuleFilter {
    private final static Logger logger = LoggerFactory.getLogger(ImportModuleFilter.class);

    String importResource;

    public String getImportResource() {
        return importResource;
    }

    public void setImportResource(String importResource) {
        this.importResource = importResource;
    }

    @Override
    public String toString() {
        return "ImportModuleFilter{" +
                "importResource='" + importResource + '\'' +
                ", q='" + q + '\'' +
                ", ids=" + ids +
                '}';
    }
}
