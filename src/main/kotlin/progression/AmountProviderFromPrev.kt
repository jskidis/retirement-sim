package progression

import Amount
import YearlyDetail

interface AmountProviderFromPrev : NextAmountProvider {
    fun previousAmount(prevYear: YearlyDetail): Amount?
    fun nextAmountFromPrev(prevAmount: Amount, prevYear: YearlyDetail): Amount

    override fun determineAmount(prevYear: YearlyDetail?): Amount =
        if (prevYear == null) initialAmount()
        else previousAmount(prevYear)
            ?.let { nextAmountFromPrev(it, prevYear) }
            ?: nextAmount(prevYear)
}
