package asset

import Amount
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import tax.BracketBasedTaxCalcFixture
import tax.BracketCase
import tax.TaxableAmounts
import tax.baseTaxConfigFixture
import yearlyDetailFixture

class TilNextBracketRothConvTest : ShouldSpec({
    val bracketStart = 200000.0
    val bracketEnd = 300000.0
    val bracketCase = BracketCase(pct = .25, bracketStart, bracketEnd)
    val topBracketCase = bracketCase.copy(end = Amount.MAX_VALUE)

    val fedTaxable = 100000.0
    val fedLTGTaxable = 10000.0
    val taxableAmounts = TaxableAmounts("Person", fedTaxable, fedLTGTaxable)

    val currYear = yearlyDetailFixture()

    should("amountToConvert returns difference between top of the current bracket minus taxable amounts") {
        val taxCalcConfig = baseTaxConfigFixture(
            fed = BracketBasedTaxCalcFixture(currBracket = bracketCase))

        val expectedResults = bracketEnd - fedTaxable - fedLTGTaxable
        val amountCalc = TilNextBracketRothConv()
        amountCalc.amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(expectedResults)
    }

    should("amountToConvert returns 0 if the bracket is the top bracket (open ended)") {
        val taxCalcConfig = baseTaxConfigFixture(
            fed = BracketBasedTaxCalcFixture(currBracket = topBracketCase))

        val amountCalc = TilNextBracketRothConv()
        amountCalc.amountToConvert(currYear, taxableAmounts, taxCalcConfig).shouldBeZero()
    }
})
