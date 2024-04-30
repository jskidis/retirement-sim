package progression

import Amount
import Year
import YearlyDetail
import util.SCurveCalc

abstract class SCurveDecreasingAmountProvider(
    val startAmount: Amount,
    val startDecYear: Year,
    val numYears: Int,
) : NextAmountProvider, AmountAdjuster {

    override fun initialAmount() = startAmount

    override fun nextAmount(prevYear: YearlyDetail): Amount =
        adjustAmount(nominalNextValue(prevYear), prevYear)

    fun nominalNextValue(prevYear: YearlyDetail): Amount =
        SCurveCalc.calcValue(
            index = (prevYear.year + 1).toDouble(),
            indexRange = startDecYear.toDouble() to (startDecYear + numYears).toDouble(),
            valueRange = startAmount to 0.0
        )
}

