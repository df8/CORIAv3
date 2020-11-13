package com.coria.v3.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Sebastian Gross, 2017
 * Shuts down open threads and pools when application is shutting down
 */
@Component
public class ContextClosedHandler implements ApplicationListener<ContextClosedEvent> {


    List<ThreadPoolTaskExecutor> executorList;


    @Autowired
    void setExecutorList(List<ThreadPoolTaskExecutor> executorList) {
        this.executorList = executorList;
    }


//    @Autowired
//    ThreadPoolTaskScheduler scheduler;

    @Override
    public void onApplicationEvent(@NotNull ContextClosedEvent event) {
//        scheduler.shutdown();
        executorList.forEach(ThreadPoolTaskExecutor::shutdown);
    }
}
