package com.coria.v3.model;

/**
 * Created by David Fradin, 2020
 * A technical query Metadata component required by the React-Admin frontend engine.
 * The frontend requests this alongside with the regular request to adapt views.
 */
public class ListMetadata {
    private long count;

    public ListMetadata(long count) {
        this.count = count;
    }

    /**
     * @return Number of entries suitable for the selected query parameters.
     */
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
