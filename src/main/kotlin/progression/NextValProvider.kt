package progression

import Amount
import YearlyDetail

interface NextValProvider {
    fun initialValue(): Amount
    fun nextValue(prevYear: YearlyDetail): Amount
}

interface NextValProviderProgression<T>
    : Progression<T>, NextValProvider, AmountToRecProvider<T> {

    override fun determineNext(prevYear: YearlyDetail?): T = createRecord(
        if (prevYear == null) initialValue()
        else nextValue(prevYear)
    )
}
