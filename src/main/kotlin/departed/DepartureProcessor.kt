package departed

import YearlyDetail
import config.SimConfig

object DepartureProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?, currYear: YearlyDetail): List<DepartedRec> {
        val newlyDeparted = config.nonDepartedMembers(prevYear).filter {
            it.departureConfig().determineDeparted(currYear)
        }.map {
            DepartedRec(it.name(), currYear.year)
        }
        return (prevYear?.departed ?: listOf()) + newlyDeparted
    }
}