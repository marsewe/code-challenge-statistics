package com.marsewe.codechallenge.statistics.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

/**
 * Manage real-time statistics from the transactions of the last MAX_AGE_SECONDS.
 */
@Service
public class StatisticsService {


    protected static final int MAX_AGE_SECONDS = 60;
    private final ConcurrentSkipListMap<Instant, Double> measurementsMap = new ConcurrentSkipListMap<>();

    /**
     * Check whether transaction is too old.
     * @param instant  timestamp of the transaction.
     * @return  true is transaction is older then MAX_AGE_SECONDS.
     */
    public static boolean isTransactionTooOld(Instant instant) {
        return Instant.now().minusSeconds(MAX_AGE_SECONDS).isAfter(instant);
    }


    /**
     * Add a transaction to the measurements.
     * @param timestamp timestamp of the transaction.
     * @param amount amount of the transaction.
     */
    @Async
    public void addTransaction(Instant timestamp, Double amount) {
        measurementsMap.put(timestamp, amount);
        trim();
    }


    /**
     * Removes all transactions from measurements which are older the MAX_AGE_SECONDS.
     */
    public void trim() {
        measurementsMap.headMap(Instant.now().minusSeconds(MAX_AGE_SECONDS)).clear();
    }


    /**
     * Retrieve Map with Measurements
     * @return measurementsMap.
     */
    public Map<Instant, Double> getMeasurementsMap() {
        return measurementsMap;
    }


    /**
     * Provide statistics about the transactions of the last MAX_AGE_SECONDS.
     * @return statistics-data
     */
    public DoubleSummaryStatistics getStatistics() {
        trim();
        Collection<Double> recordedValues = measurementsMap.values();
        DoubleSummaryStatistics doubleSummaryStatistics = recordedValues.parallelStream().mapToDouble(d->d).summaryStatistics();
        return doubleSummaryStatistics;
    }
}
