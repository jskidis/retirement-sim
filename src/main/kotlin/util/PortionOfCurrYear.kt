package util

import Year

object PortionOfCurrYear {
    fun calc(year: Year): Double {
        return when {
            currentDate.year == year -> currentDate.dayOfYear / 365.0
            else -> 0.0
        }
    }
}