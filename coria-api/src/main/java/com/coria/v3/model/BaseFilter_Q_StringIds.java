package com.coria.v3.model;

import java.util.List;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type with a string q for keyword search and an array of IDs to pick/filter multiple entities.
 */
public class BaseFilter_Q_StringIds extends BaseFilter_Q {


    List<String> ids;

    /**
     * @return The list of IDs the user has set to white-filter.
     */
    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
