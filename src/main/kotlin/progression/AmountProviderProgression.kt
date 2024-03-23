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

interface AmountProviderProgression<T> :
    PrevYearProgression<T>, AmountProvider, AmountToRecProvider<T> {

    override fun determineFromPrev(prevYear: YearlyDetail?): T =
        createRecord(determineAmount(prevYear), yearFromPrevYearDetail(prevYear))
}

