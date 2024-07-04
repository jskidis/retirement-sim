package progression

import Amount
import YearlyDetail

interface AmountProvider {
    fun determineAmount(prevYear: YearlyDetail?): Amount
}

interface AmountToRecProvider<T> {
    fun createRecord(value: Amount, prevYear: YearlyDetail?): T
}

interface AmountProviderProgression<T> :
    Progression<T>, AmountProvider, AmountToRecProvider<T> {

    override fun determineNext(prevYear: YearlyDetail?): T =
        createRecord(determineAmount(prevYear), prevYear)
}

