package departed

import Rate
import Year
import YearlyDetail
import config.Person
import util.RandomizerFactory

open class ActuarialEventCalc (
    val person: Person,
    val minYear: Year,
    val defaultYear: Year,
    val multiplier: Rate = 1.0,
    val probabilityCalc: ChanceOfActurialEventCalc = ActuarialLifeMap,
)  {
    fun didEventOccur(currYear: YearlyDetail): Boolean {
        val year = currYear.year
        return if (year < minYear) false
        else if (RandomizerFactory.suppressRandom()) year >= defaultYear
        else {
            val rndValue = RandomizerFactory.getPersonRandom(person.name, currYear)
            val probability = probabilityCalc.getChanceOfEvent(
                age = year - person.birthYM.year, person.actuarialGender)
            rndValue < probability * multiplier
        }
    }
}