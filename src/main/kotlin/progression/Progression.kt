package progression

import Amount
import YearlyDetail

interface Progression<T> {
    fun determineNext(prevYear: YearlyDetail?): T
    fun isValid(): Boolean = true
}

interface AmountToRecProvider<T> {
    fun createRecord(value: Amount): T
}

