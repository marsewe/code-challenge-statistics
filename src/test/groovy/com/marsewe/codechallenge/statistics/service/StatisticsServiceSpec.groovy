package com.marsewe.codechallenge.statistics.service

import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

import static org.hamcrest.Matchers.closeTo
import static spock.util.matcher.HamcrestSupport.expect

/**
 * Unit-tests for StatisticsService.
 */



@Unroll
class StatisticsServiceTransactionAgeSpec extends Specification {

    def "transaction #a is too old: #b"() {
        expect:
        StatisticsService.isTransactionTooOld(a) == b

        where:
        a                              | b
        Instant.now()                  | false
        Instant.now().minusSeconds(59) | false
        Instant.now().minusSeconds(60) | true
        Instant.now().minusSeconds(61) | true
    }

}


class StatisticsServiceMeasurementsTrimSpec extends Specification {

    StatisticsService statisticsService = new StatisticsService()
    Instant now = Instant.now()
    Instant twoMinutesAgo = Instant.now().minusSeconds(120)
    Instant fiftyFiveSecondsAgo = Instant.now().minusSeconds(55)


    def "trim should remove the outdated transactions"() {

        given: "three transactions recorded, one of them outdated"
        statisticsService.addTransaction(now, 12.0)
        statisticsService.addTransaction(fiftyFiveSecondsAgo, 76.4)
        statisticsService.addTransaction(twoMinutesAgo, 24.0)


        when: "trim is called to reduce set of transactions to those of the last 60 seconds"
        statisticsService.trim()

        then: "outdated transaction should have been removed, other kept"
        statisticsService.getMeasurementsMap().containsKey(now)
        statisticsService.getMeasurementsMap().containsKey(fiftyFiveSecondsAgo)
        !statisticsService.getMeasurementsMap().containsKey(twoMinutesAgo)
        statisticsService.getMeasurementsMap().size() == 2
    }


}


class StatisticsServiceGetStatisticsSpec extends Specification {


    StatisticsService statisticsService = new StatisticsService()


    def "no transaction posted within last 60 seconds"() {
        when: "statistics retrieved"
        DoubleSummaryStatistics statistics = statisticsService.getStatistics()

        then: "meaningful non-null defaults for statistics"
        with(statistics) {
            count == 0
            sum == 0
            max == Double.NEGATIVE_INFINITY
            min == Double.POSITIVE_INFINITY
            average == 0
        }


    }

    def "sum, avg, min, max, count should be calculated correctly"() {

        given: "three transactions"
        statisticsService.addTransaction(Instant.now().minusMillis(2), 40.2)
        statisticsService.addTransaction(Instant.now().minusMillis(1), 20.1)
        statisticsService.addTransaction(Instant.now(), 60.3)

        when: "statistics retrieved"
        DoubleSummaryStatistics statistics = statisticsService.getStatistics()

        then:
        with(statistics) {
            count == 3
            expect average, closeTo(40.2d, 0.00001d)
            max == 60.3
            min == 20.1
            expect sum, closeTo(120.6d, 0.00001d)
        }
    }


    def "transactions older then 60 seconds should not be considered"() {

        given: "three transactions, two of them too old"
        statisticsService.addTransaction(Instant.now().minusSeconds(61), 40.2)
        statisticsService.addTransaction(Instant.now().minusSeconds(1221), 20.1)
        statisticsService.addTransaction(Instant.now(), 60.3)

        when: "statistics retrieved"
        DoubleSummaryStatistics statistics = statisticsService.getStatistics()

        then: "only the one younger then 60 seconds considered"
        with(statistics) {
            count == 1
            expect average, closeTo(60.3d, 0.00001d)
            max == 60.3
            min == 60.3
            expect sum, closeTo(60.3d, 0.00001d)
        }
    }
}