package util

import Year
import YearlyDetail
import java.text.DecimalFormat
import java.time.LocalDate

val moneyFormat = DecimalFormat("$##,###,###")
val twoDecimalFormat = DecimalFormat("#0.00")
val fourDecimalFormat = DecimalFormat(".0000")
val currentDate = LocalDate.now()

fun yearFromPrevYearDetail(prevYear: YearlyDetail?) : Year =
    if (prevYear == null) currentDate.year
    else prevYear.year +1


fun strWhenNotZero(isZero: Boolean, str: String): String = if (isZero) "" else str

