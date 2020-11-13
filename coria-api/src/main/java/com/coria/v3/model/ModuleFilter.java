package com.coria.v3.model;

import com.coria.v3.metrics.ModuleBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type to filter a list of entities based on keywords and IDs.
 */
public class ModuleFilter extends BaseFilter_Q_StringIds implements Serializable {
    private final static Logger logger = LoggerFactory.getLogger(ModuleFilter.class);

    /**
     * @param module instance of @{@link ModuleBase} to check against a filter predicate
     * @return Returns true if the the filter predicate accepts the provided ModuleBase instance
     */
    public boolean evaluate(ModuleBase module) {
        //logger.debug("ModuleFilter.evaluate() q={} ids=[{}]", this.getQ(), String.join(", ", this.getIds()));
        return (this.getIds() == null || this.getIds().contains(module.getId())) && (
                this.getQ() == null || (module.getId().toLowerCase().contains(this.getQ()) ||
                        module.getName().toLowerCase().contains(this.getQ()) ||
                        module.getDescription().toLowerCase().contains(this.getQ())));
    }

    @Override
    public String toString() {
        return "ModuleFilter{" +
                "q='" + q + '\'' +
                ", ids=" + ids +
                '}';
    }
}
