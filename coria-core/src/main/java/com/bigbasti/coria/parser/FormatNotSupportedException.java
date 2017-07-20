package com.bigbasti.coria.parser;

import java.io.Serializable;

/**
 * Created by Sebastian Gross
 */
public class FormatNotSupportedException extends Throwable implements Serializable {
    public FormatNotSupportedException(String message) {
        super(message);
    }
}
