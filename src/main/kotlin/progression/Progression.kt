package progression

import YearlyDetail

interface Progression<T> {
    fun determineNext(year: YearlyDetail?): T
    fun usesCurrYear(): Boolean
    fun isValid(): Boolean = true
}

interface PrevYearProgression<T> : Progression<T> {
    fun determineFromPrev(prevYear: YearlyDetail?): T

    override fun determineNext(year: YearlyDetail?): T = determineFromPrev(year)
    override fun usesCurrYear(): Boolean = false
}

interface CurrYearProgression<T> : Progression<T> {
    fun determineFromCurr(currYear: YearlyDetail): T

    override fun determineNext(year: YearlyDetail?): T =
        determineFromCurr(
            year ?: throw RuntimeException("Non null current year needed for this progression"))

    override fun usesCurrYear(): Boolean = false
}
