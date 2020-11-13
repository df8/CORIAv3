package com.coria.v3.model;

/**
 * Created by David Fradin, 2020
 * Defines a GraphQL input type with a string q for keyword search.
 */
public class BaseFilter_Q {
    String q;

    /**
     * @return User's input string for the keyword search.
     */
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }
}
