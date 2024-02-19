package util

import Year
import currentDate

object PortionOfYearPast {
    fun calc(year: Year): Double {
        return when {
            currentDate.year < year -> 0.0
            currentDate.year > year -> 1.0
            else -> (365 - currentDate.dayOfYear) / 365.0
        }
    }
}