package asset

import YearMonth
import config.Person
import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class RmdRequiredDistHandlerTest : ShouldSpec({

    should("generateDistribution doesn't create distribution if rmd pct is 0") {
        val balance = 1000.0
        val person = personFixture(birthYM = YearMonth(1999, 0))
        val handler = RmdCashFlowEventFixture(person, 0.0)

        handler.generateCashFlowTribution(balance, 2024).shouldBeNull()
    }

    should("generateDistribution creates distribution if rmd pct is > 0") {
        val pct = 0.10
        val balance = 1000.0
        val person = personFixture(birthYM = YearMonth(1949, 0))
        val handler = RmdCashFlowEventFixture(person, pct)

        val result = handler.generateCashFlowTribution(balance, 2024)
        result.shouldNotBeNull()
        result.amount.shouldBe(-balance * pct)
        result.name.shouldBe(RmdCashFlowEventHandler.CHANGE_NAME)
        result.isCarryOver.shouldBeFalse()
        result.isCashflowEvent.shouldBeTrue()
    }
})

class RmdCashFlowEventFixture(person: Person, val rmdPct: Double)
    : RmdCashFlowEventHandler(person) {

    override fun getRmdPct(age: Int): Double = rmdPct
}
