package com.bigbasti.coria.config;

import com.bigbasti.coria.db.DataStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Sebastian Gross
 * Shuts down open threads and pools when application is shutting down
 */
@Component
public class ContextClosedHandler implements ApplicationListener<ContextClosedEvent> {

    @Autowired
    List<DataStorage> storageList;

    @Autowired
    ThreadPoolTaskExecutor executor;
//    @Autowired
//    ThreadPoolTaskScheduler scheduler;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
//        scheduler.shutdown();
        executor.shutdown();
        storageList.forEach(DataStorage::dispose);
    }
}
