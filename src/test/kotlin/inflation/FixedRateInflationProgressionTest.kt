package inflation

import Rate
import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import kotlin.math.pow

class FixedRateInflationProgressionTest : ShouldSpec({
    val stdRate: Rate = 0.03
    val medRate: Rate = 0.04
    val wageRate: Rate = 0.035
    val progression = FixedRateInflationProgression(stdRate, medRate, wageRate)

    should(
        "determine next with no previous data " +
            "rates are specified fixed rate values and compound are 1.0") {

        val initialValue = progression.determineNext(null)
        initialValue.std.rate.shouldBe(stdRate)
        initialValue.med.rate.shouldBe(medRate)
        initialValue.wage.rate.shouldBe(wageRate)

        initialValue.std.cmpdStart.shouldBe(1.0)
        initialValue.med.cmpdStart.shouldBe(1.0)
        initialValue.wage.cmpdStart.shouldBe(1.0)

        initialValue.std.cmpdEnd.shouldBe(1.0 + stdRate)
        initialValue.med.cmpdEnd.shouldBe(1.0 + medRate)
        initialValue.wage.cmpdEnd.shouldBe(1.0 + wageRate)
    }

    should(
        "determine next with no previous data and using default rates : " +
            "provides appropriate defaults and compound should be 1.0") {

        val initialValue = progression.determineNext(null)
        initialValue.std.rate.shouldBe(stdRate)
        initialValue.med.rate.shouldBeGreaterThan(stdRate)
        initialValue.wage.rate.shouldBeGreaterThan(stdRate)
    }

    should(
        "deetermine next with previous data: " +
            "rates are fixed rates, " +
            "compound values are previous compound times (1.00 based) current rate ") {
        val initialValue = progression.determineNext(null)
        val nextValue = progression.determineNext(YearlyDetail(2000, initialValue))
        nextValue.std.rate.shouldBe(stdRate)
        nextValue.med.rate.shouldBe(medRate)
        nextValue.wage.rate.shouldBe(wageRate)

        nextValue.std.cmpdStart.shouldBeWithinPercentageOf(1.0 + stdRate, .001)
        nextValue.med.cmpdStart.shouldBeWithinPercentageOf(1.0 + medRate, .001)
        nextValue.wage.cmpdStart.shouldBeWithinPercentageOf(1.0 + wageRate, .001)

        nextValue.std.cmpdEnd.shouldBeWithinPercentageOf((1.0 + stdRate).pow(2), .001)
        nextValue.med.cmpdEnd.shouldBeWithinPercentageOf((1.0 + medRate).pow(2), .001)
        nextValue.wage.cmpdEnd.shouldBeWithinPercentageOf((1.0 + wageRate).pow(2), .001)
    }
})