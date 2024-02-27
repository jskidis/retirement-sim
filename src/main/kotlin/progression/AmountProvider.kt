package progression

import Amount
import Year
import YearlyDetail
import util.yearFromPrevYearDetail

interface AmountProvider {
    fun determineAmount(prevYear: YearlyDetail?): Amount
}

interface AmountToRecProvider<T> {
    fun createRecord(value: Amount, year: Year): T
}

interface NullableAmountProviderProgression<T> :
    NullableProgression<T>, AmountProvider, AmountToRecProvider<T> {

    override fun determineNextIf(prevYear: YearlyDetail?): T? {
        val amount = determineAmount(prevYear)
        return if (amount == 0.0) null
        else createRecord(amount, yearFromPrevYearDetail(prevYear))
    }

}

