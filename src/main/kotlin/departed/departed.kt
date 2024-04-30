package departed

import Name
import Year
import YearlyDetail

data class DepartedRec(val person: Name, val year: Year)

fun interface DepartedDetermination {
    fun determineDeparted(currYear: YearlyDetail): Boolean
}
