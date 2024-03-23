package progression

import YearlyDetail

interface PrevRecProviderProgression<T> :
    PrevYearProgression<T> {

    fun initialRec(): T
    fun previousRec(prevYear: YearlyDetail): T?
    fun nextRecFromPrev(prevYear: YearlyDetail): T
    fun nextRecFromPrev(prevRec: T, prevYear: YearlyDetail): T

    override fun determineFromPrev(prevYear: YearlyDetail?): T =
        if (prevYear == null) initialRec()
        else previousRec(prevYear)
            ?.let { nextRecFromPrev(it, prevYear) }
            ?: nextRecFromPrev(prevYear)
}

