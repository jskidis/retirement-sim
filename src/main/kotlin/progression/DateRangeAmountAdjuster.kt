package progression

import Amount
import YearlyDetail
import util.DateRange

open class DateRangeAmountAdjuster(val dateRange: DateRange): AmountAdjusterWithGapFiller {
    override fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount =
        value * dateRange.pctInYear(prevYear.year +1).value

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount =
        adjustAmount(value, prevYear)
}