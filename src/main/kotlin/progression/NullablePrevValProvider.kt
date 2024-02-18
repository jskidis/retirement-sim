package progression

import Amount
import YearlyDetail

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

    override fun determineNext(prevYear: YearlyDetail?): T {
        val nextValue: Amount =
            if (prevYear == null) initialValue()
            else previousValue(prevYear)
                ?.let { nextValue(it, prevYear) }
                ?: gapFillValue(prevYear)
        return createRecord(nextValue)
    }
}
