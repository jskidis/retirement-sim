package progression

import Amount
import YearlyDetail

class AmountAdjusterFixture(val prevYearMultiplier: Double)
    : AmountAdjuster {

    override fun adjustAmount(value: Amount, prevYear: YearlyDetail) =
        value * prevYearMultiplier
}

class GapAmountAdjusterFixture(val prevYearMultiplier: Double, val gapFillerMultiplier: Double)
    : AmountAdjusterWithGapFiller {

    override fun adjustAmount(value: Amount, prevYear: YearlyDetail) =
        value * prevYearMultiplier

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail) =
        value * gapFillerMultiplier
}

