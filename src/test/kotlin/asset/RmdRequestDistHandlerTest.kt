package asset

import YearMonth
import config.Person
import config.personFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class RmdRequestDistHandlerTest : FunSpec({

    test("generateDistribution doesn't create distribution if rmd pct is 0") {
        val balance = 1000.0
        val person = personFixture(name = "Person", birthYM = YearMonth(1999, 0))
        val handler = RmdRequestDistFixture(person, 0.0)

        handler.generateDistribution(balance, 2024).shouldBeNull()
    }

    test("generateDistribution creates distribution if rmd pct is > 0") {
        val pct = 0.10
        val balance = 1000.0
        val person = personFixture(name = "Person", birthYM = YearMonth(1999, 0))
        val handler = RmdRequestDistFixture(person, pct)

        val result = handler.generateDistribution(balance, 2024)
        result.shouldNotBeNull()
        result.amount.shouldBe(balance * pct)
        result.name.shouldBe(RequiredDistHandler.CHANGE_NAME)
        result.isCarryOver.shouldBeFalse()
        result.isReqDist.shouldBeTrue()
    }
})

class RmdRequestDistFixture(person: Person, val rmdPct: Double)
    : RmdRequestDistHandler(person) {

    override fun getRmdPct(age: Int): Double = rmdPct
}
