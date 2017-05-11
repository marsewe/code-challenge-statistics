package com.marsewe.codechallenge.statistics.restapi;


import com.marsewe.codechallenge.statistics.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoints for adding transaction-data to the statistics and for retrieving the statistics.
 */
@RestController
public class StatisticsController {


    @Autowired
    private StatisticsService statisticsService;

    /**
     * Record a transaction.
     *
     * @param amount    transaction amount
     * @param timestamp timestamp in milliseconds using unix time format and UTC time zone.
     * @return 201 in case of success, 204 if transaction is too old to be considered.
     */
    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public ResponseEntity addTransactions(
            @RequestParam("amount") final double amount,
            @RequestParam("timestamp") final long timestamp) {

        Instant transactionInstant = Instant.ofEpochMilli(timestamp);

        if (StatisticsService.isTransactionTooOld(transactionInstant)) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            statisticsService.addTransaction(transactionInstant, amount);
            return new ResponseEntity(HttpStatus.CREATED);
        }

    }

    /**
     * Retrieve transaction-statistics of the last 60 seconds.
     * @return e.g. {"sum": 1000, "avg": 100, "max": 200, "min": 50, "count": 10 }
     */
    @RequestMapping(value = "/statistics", method = RequestMethod.GET)
    public Map<String, Number> getStatistics() {
        DoubleSummaryStatistics summaryStatistics = statisticsService.getStatistics();
        Map<String, Number> result = new HashMap<>();

        result.put("sum", summaryStatistics.getSum());
        result.put("avg", summaryStatistics.getAverage());
        result.put("max", summaryStatistics.getMax());
        result.put("min", summaryStatistics.getMin());
        result.put("count", summaryStatistics.getCount());

        return result;
    }


    protected StatisticsService getStatisticsService() {
        return statisticsService;
    }

    protected void setStatisticsService(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }
}
