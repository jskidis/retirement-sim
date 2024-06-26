package departed

import config.configFixture
import config.householdConfigFixture
import config.personConfigFixture
import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class DepartureProcessorTest : ShouldSpec({
    val year = currentDate.year + 1
    val person1 = personFixture("Person 1")
    val person2 = personFixture("Person 2")

    should("process doesn't return any departed if no already departed and no new departed") {
        val config = configFixture(
            householdConfig = householdConfigFixture(
                householdMembers = listOf(
                    personConfigFixture(
                        person = person1,
                        departureConfig = YearBasedDeparture(year + 10)
                    ),
                    personConfigFixture(
                        person = person2,
                        departureConfig = YearBasedDeparture(year + 10)
                    )
                )
            ))

        val result = DepartureProcessor.process(config,
            prevYear = null,
            currYear = yearlyDetailFixture(year = year))

        result.shouldBeEmpty()
    }

    should("process should return newly departed only new departed this year and no already departed") {
        val config = configFixture(
            householdConfig = householdConfigFixture(
                householdMembers = listOf(
                    personConfigFixture(
                        person = person1,
                        departureConfig = YearBasedDeparture(year)
                    ),
                    personConfigFixture(
                        person = person2,
                        departureConfig = YearBasedDeparture(year + 10)
                    )
                )
            ))

        val result = DepartureProcessor.process(config,
            prevYear = yearlyDetailFixture(year = year -1, departed = listOf()),
            currYear = yearlyDetailFixture(year = year))

        result.shouldHaveSize(1)
        result[0].person.shouldBe(person1.name)
        result[0].year.shouldBe(year)
    }

    should("process should return newly departed and previously departed") {
        val config = configFixture(
            householdConfig = householdConfigFixture(
                householdMembers = listOf(
                    personConfigFixture(
                        person = person1,
                        departureConfig = YearBasedDeparture(year)
                    ),
                    personConfigFixture(
                        person = person2,
                        departureConfig = YearBasedDeparture(year - 10)
                    )
                )
            ))

        val result = DepartureProcessor.process(config,
            prevYear = yearlyDetailFixture(year = year -1, departed = listOf(
                DepartedRec(person2.name, year - 10))
            ),
            currYear = yearlyDetailFixture(year = year))

        result.shouldHaveSize(2)
        result[0].person.shouldBe(person2.name)
        result[0].year.shouldBe(year - 10)
        result[1].person.shouldBe(person1.name)
        result[1].year.shouldBe(year)
    }

})
