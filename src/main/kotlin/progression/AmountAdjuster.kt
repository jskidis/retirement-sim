package progression

import Amount
import YearlyDetail

interface AmountAdjuster {
    fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount
}

open class ChainedAmountAdjuster(val adjusters: List<AmountAdjuster>) : AmountAdjuster {
    override fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount =
        adjusters.fold(value) { acc, adjuster ->
            adjuster.adjustAmount(acc, prevYear)
        }
}

interface AmountAdjusterWithGapFiller : AmountAdjuster {
    fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount
}

class ChainedAmountAdjusterWithGapFiller(val gapAdjusters: List<AmountAdjusterWithGapFiller>)
    : ChainedAmountAdjuster(gapAdjusters), AmountAdjusterWithGapFiller {

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount =
        gapAdjusters.fold(value) { acc, adjuster ->
            adjuster.adjustGapFillValue(acc, prevYear)
        }
}

