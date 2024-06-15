package departed

import Rate
import Year
import YearlyDetail
import config.Person

class ActuarialBasedDeparture(
    person: Person,
    minYear: Year,
    defaultYear: Year,
    multiplier: Rate = 1.0,
    probabilityCalc: ChanceOfActurialEventCalc = ActuarialLifeMap,
) : ActuarialEventCalc(person, minYear, defaultYear, multiplier, probabilityCalc),
    DepartureConfig {

    override fun determineDeparted(currYear: YearlyDetail): Boolean = didEventOccur(currYear)
}