package com.coria.v3.export;

import com.coria.v3.dbmodel.DatasetEntity;
import com.coria.v3.metrics.ModuleBase;

import java.util.Map;

/**
 * Created by Sebastian Gross, 2017 (coria-core/src/main/java/com/bigbasti/coria/export/ExportModule.java)
 * Modified by David Fradin, 2020: Refactoring, support for parameters
 */
public abstract class ExportModule implements ModuleBase {

    /**
     * Performs the export operation on the given dataset and returns the object containing the exported Data
     *
     * @param dataset the dataset to be exported
     * @param params  additional params for the export adapter
     * @return {@link ExportResult}
     */
    public abstract ExportResult exportDataset(DatasetEntity dataset, Map<String, String> params);

    protected final String id;
    protected final String name;
    protected final String description;
    protected final ExportModuleParameter[] parameters;


    public ExportModule(String id, String name, String description, ExportModuleParameter[] parameters) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parameters = parameters;
    }

    public ExportModuleParameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
