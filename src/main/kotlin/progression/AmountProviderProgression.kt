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
    fun createRecord(value: Amount, prevYear: YearlyDetail?): T =
        createRecord(value, prevYear?.year ?: yearFromPrevYearDetail(prevYear))
}

interface AmountProviderProgression<T> :
    Progression<T>, AmountProvider, AmountToRecProvider<T> {

    override fun determineNext(prevYear: YearlyDetail?): T =
        createRecord(determineAmount(prevYear), prevYear)
}

