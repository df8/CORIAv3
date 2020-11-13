package com.coria.v3.parser;

import java.io.Serializable;

/**
 * Created by Sebastian Gross, 2017
 */
public class FormatNotSupportedException extends Throwable implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public FormatNotSupportedException(String message) {
        super(message);
    }
}
