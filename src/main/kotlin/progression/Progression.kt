package progression

import Amount
import Year
import YearlyDetail

interface Progression<T> {
    fun determineNext(prevYear: YearlyDetail?): T
    fun isValid(): Boolean = true
}

interface AmountToRecProvider<T> {
    fun createRecord(value: Amount, year: Year): T
}

