package util

import Year

object PortionOfYearPast {
    fun calc(year: Year): Double {
        return when {
            currentDate.year < year -> 0.0
            currentDate.year > year -> 1.0
            else -> currentDate.dayOfYear / 365.0
        }
    }
}