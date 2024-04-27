package inflation

import YearlyDetail
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import util.RandomizerFactory
import util.currentDate
import yearlyDetailFixture

class StickyInflationProgressionTest : ShouldSpec({
    val year = currentDate.year + 1
    val initialStickiness = 0.5
    val prevYearRatio = 0.75
    val progression = StickyInflationProgression(initialStickiness, prevYearRatio)

    fun genPrevYearRec(prevInfAdj: Double, prevYearRnd: Double): YearlyDetail {
        val inflation = inflationRecFixture(rndAdj = prevInfAdj)
        val rndValues = mapOf(RandomizerFactory.GaussKeys.INFLATION.toString() to prevYearRnd)
        return yearlyDetailFixture(year = year, inflation = inflation, randomValues = rndValues)
    }

    should("getInflRandom takes 75% of previous randomized and adjustment and 25 of current") {
        var prevYear = genPrevYearRec(prevInfAdj = 1.00, prevYearRnd = 0.50)
        progression.determineNext(prevYear).rndAdj
            .shouldBe(1.00 * prevYearRatio + 0.50 * (1-prevYearRatio))

        prevYear = genPrevYearRec(prevInfAdj = -0.40, prevYearRnd = -1.50)
        progression.determineNext(prevYear).rndAdj
            .shouldBe(-0.40 * prevYearRatio + -1.50 * (1-prevYearRatio))

        prevYear = genPrevYearRec(prevInfAdj = -0.60, prevYearRnd = 2.00)
        progression.determineNext(prevYear).rndAdj
            .shouldBe(-0.60 * prevYearRatio + 2.00 * (1-prevYearRatio))
    }

    should("getInflRandom should return initialStickiness if prevYear is null") {
        progression.determineNext(null).rndAdj.shouldBe(initialStickiness)
    }
})
