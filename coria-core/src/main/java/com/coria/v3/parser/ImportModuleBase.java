package com.coria.v3.parser;

import com.coria.v3.metrics.ModuleBase;

/**
 * Created by Sebastian Gross, 2017 (coria-core/src/main/java/com/bigbasti/coria/parser/ImportModule.java)
 * Modified by David Fradin, 2020: Simplified, added more abstraction levels and refactored into other classes to reduce code redundancy.
 */
public class ImportModuleBase implements ModuleBase {
    protected final String id;
    protected final String name;
    protected final String description;

    public ImportModuleBase(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
