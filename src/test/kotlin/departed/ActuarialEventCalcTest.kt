package departed

import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import util.PersonRandomFixture
import util.RandomizerFactory
import yearlyDetailFixture

class ActuarialEventCalcTest : ShouldSpec({

    val person = personFixture()
    val minDeathYear = 2030
    val defaultDeathYear = 2050

    val certainProbability = ChanceOfActurialEventCalc { _, _ -> 2.0 }
    val noProbability = ChanceOfActurialEventCalc { _,_ -> -1.0 }

    val probabilityPct = 0.2
    val partialProbability = ChanceOfActurialEventCalc { _, _ -> probabilityPct }

    should("didEventOccur returns true if random value is less than chance of event and year is greater than min year") {
        RandomizerFactory.setSuppressRandom(false)

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, probabilityCalc = noProbability)
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, probabilityCalc = certainProbability)
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, probabilityCalc = certainProbability)
            .didEventOccur(yearlyDetailFixture(minDeathYear -1)).shouldBeFalse()
    }

    should("didEventOccur always ignore random if random is suppressed on return true only if year >= defaultYear") {
        RandomizerFactory.setSuppressRandom(true)

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, probabilityCalc = certainProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear -1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, probabilityCalc = noProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear -1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, probabilityCalc = certainProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear +1)).shouldBeTrue()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, probabilityCalc = noProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear +1)).shouldBeTrue()
    }

    should("didEventOccur return false is random value is greater than probability, true if it less so long as minimum year is surpassed") {
        RandomizerFactory.setSuppressRandom(false)

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            probabilityCalc = partialProbability, personRandomGen = PersonRandomFixture(0.5))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            probabilityCalc = partialProbability, personRandomGen = PersonRandomFixture(0.1))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()
    }

    should("didEventOccur return false is random value is greater than probability times multiplier, true if it less so long as minimum year is surpassed") {
        RandomizerFactory.setSuppressRandom(false)

        val multiplier = 2.0
        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            multiplier = multiplier, probabilityCalc = partialProbability,
            personRandomGen = PersonRandomFixture(probabilityPct * multiplier + .01))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            multiplier = multiplier, probabilityCalc = partialProbability,
            personRandomGen = PersonRandomFixture(probabilityPct * multiplier - .01))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            multiplier = multiplier, probabilityCalc = partialProbability,
            personRandomGen = PersonRandomFixture(probabilityPct -0.1))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()
    }

    should("didEventOccur return false is random value is greater than probability times multiplier or is less than probability times start multiplier, " +
        "otherwise true so long as minimum year is surpassed") {
        RandomizerFactory.setSuppressRandom(false)

        val startMultiplier = 0.5
        val multiplier = 2.0
        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            multiplier = multiplier, startMultiplier = startMultiplier,
            probabilityCalc = partialProbability,
            personRandomGen = PersonRandomFixture(probabilityPct * multiplier + .01))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            multiplier = multiplier, startMultiplier = startMultiplier,
            probabilityCalc = partialProbability,
            personRandomGen = PersonRandomFixture(probabilityPct * startMultiplier - .01))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            multiplier = multiplier, probabilityCalc = partialProbability,
            personRandomGen = PersonRandomFixture(0.2 * multiplier - .01))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear,
            multiplier = multiplier, probabilityCalc = partialProbability,
            personRandomGen = PersonRandomFixture(probabilityPct -.01))
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()
    }

    afterTest {
        RandomizerFactory.setSuppressRandom(true)
    }
})
