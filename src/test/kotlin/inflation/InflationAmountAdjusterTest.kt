package inflation

import Amount
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import yearlyDetailFixture

class InflationAmountAdjusterTest : ShouldSpec({

    val inflationRAC = InflationRAC(rate = .01, cmpdStart = 1.00, cmpdEnd = 1.01)
    val progression = InflationAmountAdjusterFixture(inflationRAC)
    val prevYear = yearlyDetailFixture()
    val value: Amount = 100.0

    should("adjustAmount returns prev value times current inflation rate") {
        progression.adjustAmount(value, prevYear)
            .shouldBeWithinPercentageOf(value * (1.0 + inflationRAC.rate), .001)
    }

    should("adjustGapFillValue") {
        progression.adjustGapFillValue(value, prevYear)
            .shouldBeWithinPercentageOf(value * inflationRAC.cmpdEnd, .001)
    }
})

class InflationAmountAdjusterFixture(val inflationRAC: InflationRAC)
    : InflationAmountAdjuster,
    CmpdInflationProvider by CmpdInflationProviderFixture(inflationRAC)

class CmpdInflationProviderFixture(val inflationRAC: InflationRAC): BaseCmpdInflationProvider {
    override fun getInflationType(): INFL_TYPE = INFL_TYPE.STD
    override fun getRAC(inflationRec: InflationRec) = inflationRAC
}
