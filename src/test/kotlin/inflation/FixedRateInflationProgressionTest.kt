package inflation

import Rate
import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import kotlin.math.pow

class FixedRateInflationProgressionTest : ShouldSpec({
    val stdRate: Rate = 0.03
    val medRate: Rate = 0.04
    val chainRate: Rate = 0.025
    val wageRate: Rate = 0.035
    val progression = FixedRateInflationProgression(stdRate, medRate, chainRate, wageRate)

    should(
        "determine next with no previous data: " +
            "rates should be specified fixed rate values and compound should be 1.0") {

        val initialValue = progression.determineNext(null)
        initialValue.std.rate.shouldBe(stdRate)
        initialValue.med.rate.shouldBe(medRate)
        initialValue.chain.rate.shouldBe(chainRate)
        initialValue.wage.rate.shouldBe(wageRate)

        initialValue.std.cmpdStart.shouldBe(1.0)
        initialValue.med.cmpdStart.shouldBe(1.0)
        initialValue.chain.cmpdStart.shouldBe(1.0)
        initialValue.wage.cmpdStart.shouldBe(1.0)

        initialValue.std.cmpdEnd.shouldBe(1.0 + stdRate)
        initialValue.med.cmpdEnd.shouldBe(1.0 + medRate)
        initialValue.chain.cmpdEnd.shouldBe(1.0 + chainRate)
        initialValue.wage.cmpdEnd.shouldBe(1.0 + wageRate)
    }

    should(
        "determine next with no previous data and using default rates : " +
            "should provide appropriate defaults and compound should be 1.0") {

        val initialValue = progression.determineNext(null)
        initialValue.std.rate.shouldBe(stdRate)
        initialValue.med.rate.shouldBeGreaterThan(stdRate)
        initialValue.chain.rate.shouldBeLessThan(stdRate)
        initialValue.wage.rate.shouldBeGreaterThan(stdRate)
    }

    should(
        "deetermine next with previous data: " +
            "rates should be fixed rates, " +
            "compound values should be previous compound times (1.00 based) current rate ") {
        val initialValue = progression.determineNext(null)
        val nextValue = progression.determineNext(YearlyDetail(2000, initialValue))
        nextValue.std.rate.shouldBe(stdRate)
        nextValue.med.rate.shouldBe(medRate)
        nextValue.chain.rate.shouldBe(chainRate)
        nextValue.wage.rate.shouldBe(wageRate)

        nextValue.std.cmpdStart.shouldBeWithinPercentageOf(1.0 + stdRate, .001)
        nextValue.med.cmpdStart.shouldBeWithinPercentageOf(1.0 + medRate, .001)
        nextValue.chain.cmpdStart.shouldBeWithinPercentageOf(1.0 + chainRate, .001)
        nextValue.wage.cmpdStart.shouldBeWithinPercentageOf(1.0 + wageRate, .001)

        nextValue.std.cmpdEnd.shouldBeWithinPercentageOf((1.0 + stdRate).pow(2), .001)
        nextValue.med.cmpdEnd.shouldBeWithinPercentageOf((1.0 + medRate).pow(2), .001)
        nextValue.chain.cmpdEnd.shouldBeWithinPercentageOf((1.0 + chainRate).pow(2), .001)
        nextValue.wage.cmpdEnd.shouldBeWithinPercentageOf((1.0 + wageRate).pow(2), .001)
    }
})