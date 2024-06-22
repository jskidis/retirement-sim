package progression

import Amount
import YearlyDetail
import util.DateRange

open class DateRangeAmountAdjuster(
    val dateRange: DateRange,
) : AmountAdjusterWithGapFiller {

    override fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount {
        val pctInPrevYear = dateRange.pctInYear(prevYear.year).value
        val pctInCurrYear = dateRange.pctInYear(prevYear.year + 1).value
        return value * pctInCurrYear /
            if (pctInPrevYear == 0.0) 1.0
            else pctInPrevYear
    }

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount =
        value * dateRange.pctInYear(prevYear.year + 1).value
}