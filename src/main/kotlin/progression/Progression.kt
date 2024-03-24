package progression

import YearlyDetail

interface Progression<T> {
    fun determineNext(prevYear: YearlyDetail?): T
}

interface CYProgression<T> {
    fun determineNext(currYear: YearlyDetail): T
}
