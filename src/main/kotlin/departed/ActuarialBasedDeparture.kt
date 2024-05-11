package departed

import Year
import YearlyDetail
import config.Person
import util.RandomizerFactory

class ActuarialBasedDeparture(
    val person: Person,
    val minYear: Year,
    val defaultYear: Year,
    val probabilityCalc: ChanceOfDeathCalc = ActuarialLifeMap,
) : DepartureConfig {

    override fun determineDeparted(currYear: YearlyDetail): Boolean {
        val year = currYear.year
        return if (year < minYear) false
        else if (RandomizerFactory.suppressRandom()) year >= defaultYear
        else {
            val rndValue = RandomizerFactory.getPersonRandom(person.name, currYear)
            val probability = probabilityCalc.getChanceOfDeath(
                age = year - person.birthYM.year, person.actuarialGender)
            rndValue < probability
        }
    }
}