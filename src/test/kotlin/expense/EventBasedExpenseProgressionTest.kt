package expense

import RecIdentifier
import departed.ActuarialEvent
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class EventBasedExpenseProgressionTest : ShouldSpec({
    val year = currentDate.year + 1
    val ident = RecIdentifier(name = "EventExpense", person = "Person")
    val amount = 10000.0

    val cmpdInflation = 2.0
    val inflationRec = inflationRecFixture(
        stdRAC = InflationRAC(
            rate = .03, cmpdStart = cmpdInflation - .03, cmpdEnd = cmpdInflation))

    val eventOccurs = ActuarialEvent {_ -> true}
    val eventDoesntOccur = ActuarialEvent {_ -> false}

    should("determineAmount returns 0 if prevYear is null") {
        EventBasedExpenseProgression(ident = ident, amount = amount, eventCalc = eventOccurs)
            .determineAmount(null).shouldBeZero()
    }

    should("determineAmount returns 0 if event doesn't occur") {
        val prevYear = yearlyDetailFixture(year, inflationRec)
        EventBasedExpenseProgression(ident = ident, amount = amount, eventCalc = eventDoesntOccur)
            .determineAmount(prevYear).shouldBeZero()
    }

    should("determineAmount returns amount times compound inflation if event occur") {
        val prevYear = yearlyDetailFixture(year, inflationRec)
        EventBasedExpenseProgression(
            ident = ident, amount = amount, eventCalc = eventOccurs)
            .determineAmount(prevYear).shouldBe(amount * cmpdInflation)
    }
})
