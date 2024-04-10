package asset

import RecIdentifier
import YearMonth
import config.Person
import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import util.currentDate
import yearlyDetailFixture

class RmdRequiredDistHandlerTest : ShouldSpec({
    val year = currentDate.year +1
    val balance = 1000.0
    val assetIdent = RecIdentifier(name = "Asset", person = "Person")
    val assetRec = assetRecFixture(year, assetIdent, balance)

    should("generateCashFlowTribution doesn't create distribution if rmd pct is 0") {
        val person = personFixture(birthYM = YearMonth(1999, 0))
        val handler = RmdCashFlowEventFixture(person, 0.0)
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec))

        handler.generateCashFlowTribution(assetRec, currYear).shouldBeNull()
    }

    should("generateCashFlowTribution creates distribution if rmd pct is > 0") {
        val pct = 0.10
        val person = personFixture(birthYM = YearMonth(1949, 0))
        val handler = RmdCashFlowEventFixture(person, pct)
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec))

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.amount.shouldBe(-balance * pct)
        result.name.shouldBe(RmdCashFlowEventHandler.CHANGE_NAME)
        result.isCarryOver.shouldBeFalse()
        result.cashflow.shouldBe(balance * pct)
    }
})

class RmdCashFlowEventFixture(person: Person, val rmdPct: Double)
    : RmdCashFlowEventHandler(person, NonWageTaxableProfile()) {

    override fun getRmdPct(age: Int): Double = rmdPct
}
