package util

import Year
import YearMonth

data class DateRange(
    val start: YearMonth = YearMonth(2000, 0),
    val end: YearMonth = YearMonth(3000, 0)
) {
    fun pctInYear(year: Year): UnitInterval {
        val pctFromStart = UnitInterval(year + 1 - start.toDec()).value
        val pctFromEnd = UnitInterval(end.toDec() - year).value
        return UnitInterval(pctFromStart + pctFromEnd - 1)
    }
}