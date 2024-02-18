package expense

import Amount
import Rate
import Year
import YearMonth
import YearlyDetail
import progression.AmountAdjusterWithGapFiller

class AgeBasedExpenseAdjuster(val birthYM: YearMonth) : AmountAdjusterWithGapFiller {

    override fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount =
        value * AgeBasedAdjustment.calc(birthYM, prevYear.year + 1)

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount = value
}

object AgeBasedAdjustment {
    fun calc(birthYM: YearMonth, year: Year): Rate {
        val age = year - birthYM.toDec()
        return when {
            age <= 61.0 -> 1.00
            age <= 65.0 -> (65.0 - age) * .0025 + .99
            age <= 75.0 -> (75.0 - age) * .0010 + .98
            else -> (age - 75.0) * .0010 + .98
        }
    }
}