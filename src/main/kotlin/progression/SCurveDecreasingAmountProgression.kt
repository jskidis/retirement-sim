package progression

import Amount
import Year
import YearlyDetail

abstract class SCurveDecreasingAmountProgression<RecT>(
    val startAmount: Amount,
    val startDecYear: Year,
    val numYears: Int,
) : NextValProviderProgression<RecT>,
    AmountAdjuster {

    override fun initialValue() = startAmount

    override fun nextValue(prevYear: YearlyDetail): Amount =
        adjustAmount(nominalNextValue(prevYear), prevYear)

    fun nominalNextValue(prevYear: YearlyDetail): Amount =
        SCurveDecreasingAmount.calcAmount(prevYear.year + 1, startAmount, startDecYear, numYears)
}

object SCurveDecreasingAmount {

    fun calcAmount(year: Year, startAmount: Amount, startDecYear: Year, numYears: Int): Amount =
        if (startDecYear > year) startAmount
        else {
            val remainingYearPct =
                1.0 * Math.max(0, numYears - (year - startDecYear)) / numYears

            val curvePct: Double =
                if (remainingYearPct > .5) Math.sin(Math.PI * (remainingYearPct - .5)) / 2.0 + 0.5
                else 0.5 - (Math.cos(Math.PI * remainingYearPct) / 2.0)

            startAmount * curvePct
        }
}

