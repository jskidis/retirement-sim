package progression

import YearlyDetail

interface Progression<T> {
    fun determineNext(prevYear: YearlyDetail?): T
    fun isValid(): Boolean = true
}
