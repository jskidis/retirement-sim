package progression

import Amount
import YearlyDetail

interface NextAmountProvider : AmountProvider {
    fun initialAmount(): Amount
    fun nextAmount(prevYear: YearlyDetail): Amount

    override fun determineAmount(prevYear: YearlyDetail?) =
        if (prevYear == null) initialAmount()
        else nextAmount(prevYear)
}
