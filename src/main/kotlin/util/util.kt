package util

import Year
import YearlyDetail
import currentDate

fun yearFromPrevYearDetail(prevYear: YearlyDetail?) : Year =
    if (prevYear == null) currentDate.year
    else prevYear.year +1
