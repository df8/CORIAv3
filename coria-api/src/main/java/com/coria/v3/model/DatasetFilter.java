package com.coria.v3.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type to filter a list of Dataset entities based on keyword search (q) and an array of IDs to pick/filter multiple entities.
 */
public class DatasetFilter extends BaseFilter_Q implements Serializable {
    List<UUID> ids;

    /**
     * @return The list of IDs the user has set to white-filter.
     */
    public List<UUID> getIds() {
        return ids;
    }

    @SuppressWarnings("unused")
    public void setIds(List<UUID> ids) {
        this.ids = ids;
    }

}
