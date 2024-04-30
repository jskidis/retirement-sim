package departed

import Name
import Year
import YearlyDetail
import toJsonStr

data class DepartedRec(val person: Name, val year: Year) {
    override fun toString(): String = toJsonStr()
}

fun interface DepartureConfig {
    fun determineDeparted(currYear: YearlyDetail): Boolean 
}

class NeverDepartConfig : DepartureConfig {
    override fun determineDeparted(currYear: YearlyDetail): Boolean = false
}

class YearBasedDeparture(val departureYear: Year) : DepartureConfig {
    override fun determineDeparted(currYear: YearlyDetail): Boolean = currYear.year >= departureYear
}
