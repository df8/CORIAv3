package com.bigbasti.coria.config;

/**
 * Created by Sebastian Gross
 */
public class AppContext {
    private static AppContext ourInstance = new AppContext();

    public static AppContext getInstance() {
        return ourInstance;
    }

    private AppContext() {
    }

    private String databaseProvider;

    public static AppContext getOurInstance() {
        return ourInstance;
    }

    public static void setOurInstance(AppContext ourInstance) {
        AppContext.ourInstance = ourInstance;
    }

    public String getDatabaseProvider() {
        return databaseProvider;
    }

    public void setDatabaseProvider(String databaseProvider) {
        this.databaseProvider = databaseProvider;
    }

    @Override
    public String toString() {
        return "AppContext{" +
                "databaseProvider='" + databaseProvider + '\'' +
                '}';
    }
}
