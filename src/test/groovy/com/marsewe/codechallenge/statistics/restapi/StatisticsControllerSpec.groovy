package com.marsewe.codechallenge.statistics.restapi

import com.marsewe.codechallenge.statistics.service.StatisticsService
import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import spock.lang.Specification

import java.time.Instant

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup

/**
 * Unit-tests for StatisticController
 */


class StatisticsControllerAddTransactionsSpec extends Specification {


    StatisticsController statisticsController = new StatisticsController()
    MockMvc mvc = standaloneSetup(statisticsController).build()


    def "transaction older then 60 seconds returns 204"() {
        MockHttpServletResponse response

        given: "time stamp two minutes ago"
        long unixTimeStamp = Instant.now().minusSeconds(120).toEpochMilli()

        when: "transaction-data posted"
        response = this.mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .param("amount", "12.3")
                .param("timestamp", unixTimeStamp.toString())).andReturn().response

        then: "204 returned."
        response.status == HttpStatus.NO_CONTENT.value()

    }


    def "transaction with negative timestamp returns 204"() {
        MockHttpServletResponse response

        given: "negative timestamp"
        long unixTimeStamp = -435344646

        when: "transaction-data posted"
        response = this.mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .param("amount", "345.6")
                .param("timestamp", unixTimeStamp.toString())).andReturn().response

        then: "204 returned."
        response.status == HttpStatus.NO_CONTENT.value()

    }


    def "transaction with timestamp within the last 60 seconds is considered"() {

        MockHttpServletResponse response
        StatisticsService statisticsService = Mock(StatisticsService)
        statisticsController.setStatisticsService(statisticsService)

        given: "transaction data, time stamp 2 seconds ago"
        Instant timeStamp = Instant.now().minusSeconds(2)
        Double amount = 12.3d

        when: "transaction-data posted"
        response = this.mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .param("amount", amount.toString())
                .param("timestamp", timeStamp.toEpochMilli().toString())).andReturn().response

        then: "data added succesfully"
        1 * statisticsService.addTransaction(timeStamp, amount)
        response.status == HttpStatus.CREATED.value()
    }

}


class StatisticsControllerGetStatisticsSpec extends Specification {

    StatisticsController statisticsController = new StatisticsController()
    MockMvc mvc = standaloneSetup(statisticsController).build()


    def "no transactions posted so far"() {
        MockHttpServletResponse response

        setup:
        StatisticsService statisticsService = Mock()
        statisticsController.statisticsService = statisticsService
        statisticsService.getStatistics() >> new DoubleSummaryStatistics()

        when: "called without existing data"
        response = this.mvc.perform(MockMvcRequestBuilders.get("/statistics")).andReturn().response

        then: "response with non-null defaults "
        def content = new JsonSlurper().parseText(response.contentAsString)

        response.status == HttpStatus.OK.value()

        with(content) {
            sum == 0
            avg == 0
            max == Double.NEGATIVE_INFINITY.toString()
            min == Double.POSITIVE_INFINITY.toString()
            count == 0
        }
    }


    def "transactions posted, statistics exist"() {
        MockHttpServletResponse response

        setup:
        DoubleSummaryStatistics doubleSummaryStatistics = new DoubleSummaryStatistics()
        for (double d = 0; d <= 1000; d += 10) {
            doubleSummaryStatistics.accept(d)
        }
        StatisticsService statisticsService = Mock()
        statisticsService.getStatistics() >> doubleSummaryStatistics
        statisticsController.statisticsService = statisticsService

        when: "called with existing data"
        response = this.mvc.perform(MockMvcRequestBuilders.get("/statistics")).andReturn().response

        then: "response with statistics returned "
        def content = new JsonSlurper().parseText(response.contentAsString)
        response.status == HttpStatus.OK.value()
        with(content) {
            sum == 50500
            avg == 500
            max == 1000
            min == 0
            count == 101
        }
    }

}
