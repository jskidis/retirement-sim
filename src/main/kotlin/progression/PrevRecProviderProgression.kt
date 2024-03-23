package progression

import YearlyDetail

interface PrevRecProviderProgression<T> :
    Progression<T> {

    fun initialRec(): T
    fun previousRec(prevYear: YearlyDetail): T?
    fun nextRec(prevYear: YearlyDetail): T
    fun nextRec(prevRec: T, prevYear: YearlyDetail): T

    override fun determineNext(prevYear: YearlyDetail?): T =
        if (prevYear == null) initialRec()
        else {
            val prevRec = previousRec(prevYear)
            if (prevRec == null) nextRec(prevYear)
            else nextRec(prevRec, prevYear)
        }
}

