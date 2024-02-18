package progression

import Amount
import Year
import YearlyDetail

abstract class LinearDecreasingAmountProgression<RecT>(
    val startAmount: Amount,
    val startDecYear: Year,
    val numYears: Int,
) : NextValProviderProgression<RecT>, AmountAdjuster {

    override fun initialValue() = startAmount

    override fun nextValue(prevYear: YearlyDetail): Amount =
        adjustAmount(nominalNextValue(prevYear), prevYear)

    fun nominalNextValue(prevYear: YearlyDetail): Amount =
        LinearDecreasingAmount.calcAmount(
            prevYear.year +1, startAmount, startDecYear, numYears)
}

object LinearDecreasingAmount {

    fun calcAmount(year: Year, startAmount: Amount, startDecYear: Year, numYears: Int): Amount =
        if (startDecYear > year) startAmount
        else startAmount * Math.max(0, numYears - (year - startDecYear)) / numYears
}

