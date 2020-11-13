package com.coria.v3.export;

import com.coria.v3.utility.Slugify;

/**
 * Created by David Fradin, 2020
 */
public class ExportModuleParameter {
    String name;
    String[] options;

    public ExportModuleParameter(String name, String[] options) {
        this.name = name;
        this.options = options;
    }

    public String getId() {
        return Slugify.toSlug(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String[] options) {
        this.options = options;
    }
}
