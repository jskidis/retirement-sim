package progression

import YearlyDetail

interface Progression<T> {
    fun determineNext(prevYear: YearlyDetail?): T
    fun isValid(): Boolean = true
}

interface NullableProgression<T> {
    fun determineNextIf(prevYear: YearlyDetail?): T?
}

