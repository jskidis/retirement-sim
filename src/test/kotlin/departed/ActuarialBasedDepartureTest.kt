package departed

import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import util.RandomizerFactory
import yearlyDetailFixture

class ActuarialBasedDepartureTest : ShouldSpec({

    val person = personFixture()
    val minDeathYear = 2030
    val defaultDeathYear = 2050

    val certainDeathProbability = ChanceOfDeathCalc { _, _ -> 2.0 }
    val noDeathProbability = ChanceOfDeathCalc { _,_ -> -1.0 }

    should("determineDeparted returns true if random value is less than change of death and year is greater than min year") {
        RandomizerFactory.setSuppressRandom(false)

        ActuarialBasedDeparture(person, minDeathYear, defaultDeathYear, noDeathProbability)
            .determineDeparted(yearlyDetailFixture(minDeathYear +1)).shouldBeFalse()

        ActuarialBasedDeparture(person, minDeathYear, defaultDeathYear, certainDeathProbability)
            .determineDeparted(yearlyDetailFixture(minDeathYear +1)).shouldBeTrue()

        ActuarialBasedDeparture(person, minDeathYear, defaultDeathYear, certainDeathProbability)
            .determineDeparted(yearlyDetailFixture(minDeathYear -1)).shouldBeFalse()
    }

    should("always ignore random if random is supressed on return true only if year >= defaultYear") {
        RandomizerFactory.setSuppressRandom(true)

        ActuarialBasedDeparture(person, minDeathYear, defaultDeathYear, certainDeathProbability)
            .determineDeparted(yearlyDetailFixture(defaultDeathYear -1)).shouldBeFalse()

        ActuarialBasedDeparture(person, minDeathYear, defaultDeathYear, noDeathProbability)
            .determineDeparted(yearlyDetailFixture(defaultDeathYear -1)).shouldBeFalse()

        ActuarialBasedDeparture(person, minDeathYear, defaultDeathYear, certainDeathProbability)
            .determineDeparted(yearlyDetailFixture(defaultDeathYear +1)).shouldBeTrue()

        ActuarialBasedDeparture(person, minDeathYear, defaultDeathYear, noDeathProbability)
            .determineDeparted(yearlyDetailFixture(defaultDeathYear +1)).shouldBeTrue()
    }

    afterTest {
        RandomizerFactory.setSuppressRandom(true)
    }
})
