package progression

import Amount
import YearlyDetail
import currentDate

interface NextValProvider {
    fun initialValue(): Amount
    fun nextValue(prevYear: YearlyDetail): Amount
}

interface NextValProviderProgression<T>
    : Progression<T>, NextValProvider, AmountToRecProvider<T> {

    override fun determineNext(prevYear: YearlyDetail?): T =
        if (prevYear == null) createRecord(initialValue(), currentDate.year)
        else createRecord(nextValue(prevYear), prevYear.year +1)
}
