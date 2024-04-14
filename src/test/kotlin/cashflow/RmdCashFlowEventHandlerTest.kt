package cashflow

import RecIdentifier
import YearMonth
import asset.assetRecFixture
import config.Person
import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import yearlyDetailFixture

class RmdCashFlowEventHandlerTest : ShouldSpec({
    val year = 2024
    val yearInFuture = 2035
    val balance = 1000.0
    val assetIdent = RecIdentifier(name = "Asset", person = "Person")
    val assetRec = assetRecFixture(year, assetIdent, balance)

    should("generateCashFlowTribution doesn't create distribution if rmd pct is 0") {
        val person = personFixture(birthYM = YearMonth(1999, 0))
        val handler = RmdCashFlowEventFixture(person, 0.0)
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec))

        handler.generateCashFlowTribution(assetRec, currYear).shouldBeNull()
    }

    should("generateCashFlowTribution creates distribution if rmd pct is > 0 and age above rmd min") {
        val pct = 0.10
        val person = personFixture(birthYM = YearMonth(year - 75, 0))
        val handler = RmdCashFlowEventFixture(person, pct)
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec))

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.amount.shouldBe(-balance * pct)
        result.name.shouldBe(RmdCashFlowEventHandler.CHANGE_NAME)
        result.isCarryOver.shouldBeFalse()
        result.cashflow.shouldBe(balance * pct)
    }

    should("generateCashFlowTribution doesn't create distribution is below rmd min, even if pct is returned") {
        val pct = 0.10
        val person = personFixture(birthYM = YearMonth(year - 72, 0))
        val handler = RmdCashFlowEventFixture(person, pct)
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec))

        handler.generateCashFlowTribution(assetRec, currYear).shouldBeNull()
        handler.generateCashFlowTribution(assetRec, currYear.copy(year + 1)).shouldNotBeNull()

        val personFuture = personFixture(birthYM = YearMonth(yearInFuture - 74, 0))
        val handlerFuture = RmdCashFlowEventFixture(personFuture, pct)
        val futureYear = yearlyDetailFixture(year = yearInFuture, assets = listOf(assetRec))

        handlerFuture.generateCashFlowTribution(assetRec, futureYear).shouldBeNull()
        handlerFuture.generateCashFlowTribution(assetRec,
            futureYear.copy(yearInFuture + 1)).shouldNotBeNull()
    }
})

class RmdCashFlowEventFixture(person: Person, val rmdPct: Double)
    : RmdCashFlowEventHandler(
    person = person,
    taxabilityProfile = NonWageTaxableProfile(),
    rmdPctLookup = RmdPctLookup { _ -> rmdPct }
)
