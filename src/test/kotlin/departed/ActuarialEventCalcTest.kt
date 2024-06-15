package departed

import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import util.RandomizerFactory
import yearlyDetailFixture

class ActuarialEventCalcTest : ShouldSpec({

    val person = personFixture()
    val minDeathYear = 2030
    val defaultDeathYear = 2050

    val certainProbability = ChanceOfActurialEventCalc { _, _ -> 2.0 }
    val noProbability = ChanceOfActurialEventCalc { _,_ -> -1.0 }

    should("didEventOccur returns true if random value is less than change of death and year is greater than min year") {
        RandomizerFactory.setSuppressRandom(false)

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, 1.0, noProbability)
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, 1.0, certainProbability)
            .didEventOccur(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, 1.0, certainProbability)
            .didEventOccur(yearlyDetailFixture(minDeathYear -1)).shouldBeFalse()
    }

    should("always ignore random if random is supressed on return true only if year >= defaultYear") {
        RandomizerFactory.setSuppressRandom(true)

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, 1.0, certainProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear -1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, 1.0, noProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear -1)).shouldBeFalse()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, 1.0, certainProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear +1)).shouldBeTrue()

        ActuarialEventCalc(person, minDeathYear, defaultDeathYear, 1.0, noProbability)
            .didEventOccur(yearlyDetailFixture(defaultDeathYear +1)).shouldBeTrue()
    }

    afterTest {
        RandomizerFactory.setSuppressRandom(true)
    }
})
