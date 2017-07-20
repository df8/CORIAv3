package com.bigbasti.coria.db;

import java.io.Serializable;

/**
 * Created by Sebastian Gross
 */
public class StorageStatus implements Serializable {
    /**
     * Indicates if the Storage is ready to use for the application
     * -> Database is setup and ready to connect
     */
    private boolean readyToUse;

    /**
     * A message for the user to display when there is a problem
     * with the storage - or some other type of status message
     */
    private String message;

    public StorageStatus(boolean readyToUse, String message) {
        this.readyToUse = readyToUse;
        this.message = message;
    }

    public boolean isReadyToUse() {
        return readyToUse;
    }

    public void setReadyToUse(boolean readyToUse) {
        this.readyToUse = readyToUse;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "StorageStatus{" +
                "readyToUse=" + readyToUse +
                ", message='" + message + '\'' +
                '}';
    }
}
