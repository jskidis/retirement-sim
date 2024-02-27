package progression

import Amount
import YearlyDetail

interface NullablePrevValProvider : AmountProvider {
    fun initialValue(): Amount
    fun previousValue(prevYear: YearlyDetail): Amount?
    fun nextValue(prevVal: Amount, prevYear: YearlyDetail): Amount
    fun gapFillValue(prevYear: YearlyDetail): Amount

    override fun determineAmount(prevYear: YearlyDetail?): Amount =
        if (prevYear == null) initialValue()
        else previousValue(prevYear)
            ?.let { nextValue(it, prevYear) }
            ?: gapFillValue(prevYear)
}
