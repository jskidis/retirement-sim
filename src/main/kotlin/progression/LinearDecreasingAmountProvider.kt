package progression

import Amount
import Year
import YearlyDetail

abstract class LinearDecreasingAmountProvider(
    val startAmount: Amount,
    val startDecYear: Year,
    val numYears: Int,
) : NextAmountProvider, AmountAdjuster {

    override fun initialAmount() = startAmount

    override fun nextAmount(prevYear: YearlyDetail): Amount =
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

