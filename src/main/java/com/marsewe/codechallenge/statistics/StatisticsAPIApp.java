package com.marsewe.codechallenge.statistics;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Small app for transaction-statistics.
 */
@EnableAsync
@SpringBootApplication
public class StatisticsAPIApp extends AsyncConfigurerSupport {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsAPIApp.class, args);
    }


    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("AddTransaction-");
        executor.initialize();
        return executor;
    }
}
