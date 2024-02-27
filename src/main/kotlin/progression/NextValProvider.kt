package progression

import Amount
import YearlyDetail

interface NextValProvider : AmountProvider {
    fun initialValue(): Amount
    fun nextValue(prevYear: YearlyDetail): Amount

    override fun determineAmount(prevYear: YearlyDetail?) =
        if (prevYear == null) initialValue()
        else nextValue(prevYear)
}
