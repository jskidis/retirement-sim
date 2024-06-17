package util

import Name
import YearlyDetail

class PersonRandomFixture(val value: Double) : PersonRandom {
    override fun getPersonRandom(person: Name, prevYear: YearlyDetail?): Double = value
}
