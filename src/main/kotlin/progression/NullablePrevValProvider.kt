package progression

import Amount
import YearlyDetail
import currentDate

interface NullablePrevValProvider {
    fun initialValue(): Amount
    fun previousValue(prevYear: YearlyDetail): Amount?
    fun gapFillValue(prevYear: YearlyDetail): Amount
}

interface NullablePrevNextValProvider {
    fun nextValue(prevVal: Amount, prevYear: YearlyDetail): Amount
}

interface NullablePrevValProviderProgression<T> :
    Progression<T>, NullablePrevValProvider,
    NullablePrevNextValProvider, AmountToRecProvider<T> {

    override fun determineNext(prevYear: YearlyDetail?): T =
        if (prevYear == null) createRecord(initialValue(), currentDate.year)
        else createRecord(
            value = previousValue(prevYear)
                ?.let { nextValue(it, prevYear) }
                ?: gapFillValue(prevYear),
            year = prevYear.year + 1
        )
}
