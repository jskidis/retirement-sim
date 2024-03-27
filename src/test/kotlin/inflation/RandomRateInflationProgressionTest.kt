package inflation

import Rate
import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import kotlin.math.pow

class RandomRateInflationProgressionTest : ShouldSpec({
    val mockRndVal = 2.0
    val stdMean: Rate = 0.03
    val medMean: Rate = 0.04
    val chainMean: Rate = 0.025
    val wageMean: Rate = 0.035
    val stdSD: Rate = 0.015
    val medSD: Rate = 0.010
    val chainSD: Rate = 0.20
    val wageSD: Rate = 0.20

    val progression = RandomRateInflationProgressFixture(mockRndVal,
        stdMean, stdSD, medMean, medSD, chainMean, chainSD, wageMean, wageSD
    )

    should(
        "determine next with no previous data: " +
            "rates are based on mean and stddev (and mock rand), compound values are 1.0") {

        val initialValue = progression.determineNext(null)

        initialValue.std.rate.shouldBeWithinPercentageOf(stdMean + stdSD * mockRndVal, .001)
        initialValue.med.rate.shouldBeWithinPercentageOf(medMean + medSD * mockRndVal, .001)
        initialValue.chain.rate.shouldBeWithinPercentageOf(chainMean + chainSD * mockRndVal, .001)
        initialValue.wage.rate.shouldBeWithinPercentageOf(wageMean + wageSD * mockRndVal, .001)

        initialValue.std.cmpdStart.shouldBe(1.0)
        initialValue.med.cmpdStart.shouldBe(1.0)
        initialValue.chain.cmpdStart.shouldBe(1.0)
        initialValue.wage.cmpdStart.shouldBe(1.0)

        initialValue.std.cmpdEnd.shouldBe(1.0 + initialValue.std.rate)
        initialValue.med.cmpdEnd.shouldBe(1.0 + initialValue.med.rate)
        initialValue.chain.cmpdEnd.shouldBe(1.0 + initialValue.chain.rate)
        initialValue.wage.cmpdEnd.shouldBe(1.0 + initialValue.wage.rate)
    }

    should(
        "deetermine next with previous data: " +
            "rates are based on mean and stddev (and mock rand), " +
            "compound values should are  compound times (1.00 based) current rate ") {

        val initialValue = progression.determineNext(null)
        val nextValue = progression.determineNext(YearlyDetail(2000, initialValue))

        nextValue.std.rate.shouldBeWithinPercentageOf(stdMean + stdSD * mockRndVal, .001)
        nextValue.med.rate.shouldBeWithinPercentageOf(medMean + medSD * mockRndVal, .001)
        nextValue.chain.rate.shouldBeWithinPercentageOf(chainMean + chainSD * mockRndVal, .001)
        nextValue.wage.rate.shouldBeWithinPercentageOf(wageMean + wageSD * mockRndVal, .001)

        nextValue.std.cmpdStart.shouldBeWithinPercentageOf((1.0 + nextValue.std.rate), .001)
        nextValue.med.cmpdStart.shouldBeWithinPercentageOf((1.0 + nextValue.med.rate), .001)
        nextValue.chain.cmpdStart.shouldBeWithinPercentageOf((1.0 + nextValue.chain.rate), .001)
        nextValue.wage.cmpdStart.shouldBeWithinPercentageOf((1.0 + nextValue.wage.rate), .001)

        nextValue.std.cmpdEnd.shouldBeWithinPercentageOf((1.0 + nextValue.std.rate).pow(2), .001)
        nextValue.med.cmpdEnd.shouldBeWithinPercentageOf((1.0 + nextValue.med.rate).pow(2), .001)
        nextValue.chain.cmpdEnd.shouldBeWithinPercentageOf(
            (1.0 + nextValue.chain.rate).pow(2),
            .001)
        nextValue.wage.cmpdEnd.shouldBeWithinPercentageOf((1.0 + nextValue.wage.rate).pow(2), .001)
    }
})

class RandomRateInflationProgressFixture(
    val mockRndVal: Double,
    stdMean: Rate, stdSD: Rate,
    medMean: Rate, medSD: Rate,
    chainMean: Rate, chainSD: Rate,
    wageMean: Rate, wageSD: Rate,
) : RandomRateInflationProgression(
    stdMean, stdSD, medMean, medSD, chainMean, chainSD, wageMean, wageSD
) {
    override fun getInflRandom(prevYear: YearlyDetail?): Double = mockRndVal
}

